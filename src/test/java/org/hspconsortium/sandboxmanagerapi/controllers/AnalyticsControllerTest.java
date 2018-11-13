/**
 * @authors: Jacob Crump, Shilpy Sharma
 *
 */

package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AnalyticsController.class, secure = false)
@ContextConfiguration(classes = AnalyticsController.class)
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private UserService userService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private AppService appService;

    @MockBean
    private RuleService ruleService;

    @MockBean
    private UserPersonaService userPersonaService;

    @MockBean
    private UserAccessHistoryService userAccessHistoryService;

    @MockBean
    private SandboxActivityLogService sandboxActivityLogService;

    @MockBean
    private AuthorizationService authorizationService;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    private Sandbox sandbox;
    private User user;
    private FhirTransaction fhirTransaction;
    private UserPersona userPersona;
    private HashMap<String, Object> stringObjectHashMap;
    private Integer stringObjectSize;

    @Before
    public void setup() {
        sandbox = new Sandbox();
        user = new User();
        fhirTransaction = new FhirTransaction();
        userPersona = new UserPersona();
        user.setSbmUserId("90342.ASDFJWFA");
        sandbox.setVisibility(Visibility.PUBLIC);
        sandbox.setSandboxId("sandbox");
        fhirTransaction.setFhirResource("Patient");
        fhirTransaction.setId(1);
        fhirTransaction.setSandboxId(1);
        userPersona.setPersonaUserId("examplePersona@SSTest");
        stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("asdf", 1.0);
        stringObjectHashMap.put("asdf1", 1.0);
        stringObjectHashMap.put("asdf2", 1.0);
        stringObjectHashMap.put("asdf3", 1.0);
        stringObjectHashMap.put("asdf4", 1.0);
        stringObjectHashMap.put("asdf5", 1.0);
        stringObjectHashMap.put("asdf6", 1.0);
        stringObjectHashMap.put("asdf7", 1.0);
        stringObjectSize = stringObjectHashMap.size();
    }

    @Test
    public void countSandboxesByUserTest() throws Exception {
        List<Sandbox>  userCreatedSandboxes = new ArrayList<>();
        userCreatedSandboxes.add(sandbox);
        int numberOfSandboxes = userCreatedSandboxes.size();

        doNothing().when(authorizationService).checkUserAuthorization(any(), any());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(sandboxService.findByPayerId(user.getId())).thenReturn(userCreatedSandboxes);

        mvc
                .perform(
                        get("/analytics/sandboxes?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string(Integer.toString(numberOfSandboxes)));
    }

    @Test(expected=NestedServletException.class)
    public void countSandboxesByUserNullUserTest() throws Exception {
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);

        mvc
                .perform(
                        get("/analytics/sandboxes?userId=" + user.getSbmUserId()));
    }

    @Test
    public void countUsersBySandboxTest() throws Exception {
        HashMap<String, Integer> sandboxIdAndUniqueUserCount = new HashMap<>();
        sandboxIdAndUniqueUserCount.put(sandbox.getSandboxId(), 1);
        String json = json(sandboxIdAndUniqueUserCount);
        doNothing().when(authorizationService).checkUserAuthorization(any(), any());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.countUsersPerSandboxByUser(user)).thenReturn(sandboxIdAndUniqueUserCount);

        mvc
                .perform(
                        get("/analytics/users?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void countUsersBySandboxNullUserTest() throws Exception {
        HashMap<String, Integer> sandboxIdAndUniqueUserCount = new HashMap<>();
        sandboxIdAndUniqueUserCount.put(sandbox.getSandboxId(), 1);
        String json = json(sandboxIdAndUniqueUserCount);
        doNothing().when(authorizationService).checkUserAuthorization(any(), any());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);

        mvc
                .perform(
                        get("/analytics/users?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void countAppsBySandboxTest() throws Exception {
        HashMap<String, Integer> sandboxIdAndAppsCount = new HashMap<>();
        sandboxIdAndAppsCount.put(sandbox.getSandboxId(), 1);
        String json = json(sandboxIdAndAppsCount);
        doNothing().when(authorizationService).checkUserAuthorization(any(), any());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.countAppsPerSandboxByUser(user)).thenReturn(sandboxIdAndAppsCount);

        mvc
                .perform(
                        get("/analytics/apps?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void countAppsBySandboxNullUserTest() throws Exception {
        HashMap<String, Integer> sandboxIdAndAppsCount = new HashMap<>();
        sandboxIdAndAppsCount.put(sandbox.getSandboxId(), 1);
        String json = json(sandboxIdAndAppsCount);
        doNothing().when(authorizationService).checkUserAuthorization(any(), any());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);

        mvc
                .perform(
                        get("/analytics/apps?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void memoryUsedByUserTest() throws Exception {
        Double memoryUsedInMB = 0.0;
        String json = json(memoryUsedInMB);

        doNothing().when(authorizationService).checkUserAuthorization(any(), any());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.retrieveTotalMemoryByUser(user, "")).thenReturn(memoryUsedInMB);

        mvc
                .perform(
                        get("/analytics/memory?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void memoryUsedByUserNullUserTest() throws Exception {
        Double memoryUsedInMB = 0.0;
        String json = json(memoryUsedInMB);

        doNothing().when(authorizationService).checkUserAuthorization(any(), any());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);

        mvc
                .perform(
                        get("/analytics/memory?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void handleFhirTransactionTest() throws Exception {
        HashMap<String, String> transactionInfo = new HashMap<>();
        transactionInfo.put("tenant", sandbox.getSandboxId());
        transactionInfo.put("secured", "true");
        transactionInfo.put("userId", user.getSbmUserId());

        String json = json(transactionInfo);
        String ft = json(fhirTransaction);

        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(userPersonaService.findByPersonaUserId(userPersona.getPersonaUserId())).thenReturn(userPersona);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.handleFhirTransaction(user, transactionInfo,"")).thenReturn(fhirTransaction);

        mvc
                .perform(
                        post("/analytics/transaction")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(ft));
    }

    @Test
    public void handleFhirTransactionIfSecuredFalseTest() throws Exception {
        HashMap<String, String> transactionInfo = new HashMap<>();
        transactionInfo.put("tenant", sandbox.getSandboxId());
        transactionInfo.put("secured", "false");
        transactionInfo.put("userId", user.getSbmUserId());

        String json = json(transactionInfo);
        String ft = json(fhirTransaction);

        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.handleFhirTransaction(null, transactionInfo,"")).thenReturn(fhirTransaction);

        mvc
                .perform(
                        post("/analytics/transaction")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(ft));
    }

    @Test
    public void handleFhirTransactionUserNullTest() throws Exception {
        HashMap<String, String> transactionInfo = new HashMap<>();
        transactionInfo.put("tenant", sandbox.getSandboxId());
        transactionInfo.put("secured", "true");
        transactionInfo.put("userId", user.getSbmUserId());

        String json = json(transactionInfo);
        String ft = json(fhirTransaction);

        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.handleFhirTransaction(null, transactionInfo,"")).thenReturn(fhirTransaction);

        mvc
                .perform(
                        post("/analytics/transaction")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(ft));
    }

//    @Test
//    public void handleFhirTransactionPersonaNullTest() throws Exception {
//        HashMap<String, String> transactionInfo = new HashMap<>();
//        transactionInfo.put("tenant", sandbox.getSandboxId());
//        transactionInfo.put("secured", "true");
//        transactionInfo.put("userId", user.getSbmUserId());
//
//        String json = json(transactionInfo);
//        String ft = json(fhirTransaction);
//
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
//        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
//        when(userPersonaService.findByPersonaUserId(userPersona.getPersonaUserId())).thenReturn(null);
//        doThrow(UnauthorizedException.class).when(authorizationService).checkIfPersonaAndHasAuthority(sandbox, userPersona);
//
//        mvc
//                .perform(post("/analytics/transaction"))
//                .andExpect(content().);
//    }

//    @Test
//    public void handleFhirTransactionIfContainsTenantTest() throws Exception {
//        HashMap<String, String> transactionInfo = new HashMap<>();
//        transactionInfo.put("tenant", sandbox.getSandboxId());
//        transactionInfo.put("secured", "true");
//        transactionInfo.put("userId", user.getSbmUserId());
//
//        String[] sandboxesAllUsersCanAccess = new String[]{"a", "b", "c", "d"};
//
//        String json = json(transactionInfo);
//        String ft = json(fhirTransaction);
//
//
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
//        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
////        doThrow(new Exception()).when(authorizationService).checkSystemUserCanMakeTransaction(sandbox, user);
////        when(userPersonaService.findByPersonaUserId(userPersona.getPersonaUserId())).thenReturn(userPersona);
//        when(authorizationService.getBearerToken(any())).thenReturn("");
//        when(analyticsService.handleFhirTransaction(null, transactionInfo,"")).thenReturn(null);
//
//        mvc
//                .perform(
//                        post("/analytics/transaction")
//                                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                                .content(json))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(ft));
//    }

    @Test
    public void getSandboxStatisticsTest() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.getSandboxStatistics("2")).thenReturn("");

        mvc
                .perform(
                        get("/analytics?interval=2"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andExpect(content().string(""));
    }

    @Test(expected =  NestedServletException.class)
    public void getSandboxStatisticsNullUserTest() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(analyticsService.getSandboxStatistics("2")).thenReturn("");

        mvc
                .perform(
                        get("/analytics?interval=2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string(""));
    }

    @Test
    public void transactionStatsTest() throws Exception {
        String json = json(stringObjectHashMap);
        Integer n = 1;

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.transactionStats(2, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/transactions?interval=2&n=" + n))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void transactionStatsNullUserTest() throws Exception {
        String json = json(stringObjectHashMap);
        Integer n = 1;

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(analyticsService.transactionStats(2, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/transactions?interval=2&n=" + n))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void transactionStatsNValueArraySizeTest() throws Exception {
        Integer n = stringObjectSize;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.transactionStats(2, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/transactions?interval=2&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void transactionStatsNValueLessThanArraySizeTest() throws Exception {
        Integer n = stringObjectSize - 5;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.transactionStats(2, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/transactions?interval=2&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void transactionStatsNValueMoreThanArraySizeTest() throws Exception {
        Integer n = stringObjectSize + 10;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.transactionStats(2, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/transactions?interval=2&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void transactionStatsNegativeNValueTest() throws Exception {
        Integer n = -15;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.transactionStats(2, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/transactions?interval=2&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void transactionStatsDifferentIntervalZeroTest() throws Exception {
        Integer n = 6;
        Integer interval = 0;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.transactionStats(interval, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/transactions?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void transactionStatsDifferentIntervalThousandTest() throws Exception {
        Integer n = 6;
        Integer interval = 1000;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.transactionStats(interval, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/transactions?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void transactionStatsDifferentIntervalTenThousandTest() throws Exception {
        Integer n = 6;
        Integer interval = 10000;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(analyticsService.transactionStats(interval, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/transactions?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void sandboxMemoryStatsTest() throws Exception {
        Integer n = stringObjectSize;
        Integer interval = 1000;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.sandboxMemoryStats(interval, n, "")).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/sandboxMemory?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void sandboxMemoryStatsNullUserTest() throws Exception {
        Integer n = stringObjectSize;
        Integer interval = 1000;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.sandboxMemoryStats(interval, n, "")).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/sandboxMemory?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void sandboxMemoryStatsNValueArraySizeTest() throws Exception {
        Integer n = stringObjectSize;
        Integer interval = 1000;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.sandboxMemoryStats(interval, n, "")).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/sandboxMemory?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void sandboxMemoryStatsNValueLessArraySizeTest() throws Exception {
        Integer n = stringObjectSize - 10;
        Integer interval = 1000;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.sandboxMemoryStats(interval, n, "")).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/sandboxMemory?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void sandboxMemoryStatsNValueNullTest() throws Exception {
        Integer interval = 1000;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.sandboxMemoryStats(interval, null, "")).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/sandboxMemory?interval=" + interval) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void usersPerSandboxStatsTest() throws Exception {
        Integer interval = 1000;
        Integer n = -1;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.usersPerSandboxStats(interval, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/usersPerSandbox?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void usersPerSandboxStatsNullUserTest() throws Exception {
        Integer interval = 1000;
        Integer n = -1;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn("");
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);

        mvc
                .perform(
                        get("/analytics/overallStats/usersPerSandbox?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void sandboxesPerUserStatsTest() throws Exception {
        Integer interval = 1000;
        Integer n = -1;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.sandboxesPerUserStats(interval, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/sandboxesPerUser?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void sandboxesPerUserStatsNullUserTest() throws Exception {
        Integer interval = 1000;
        Integer n = -1;
        String json = json(stringObjectHashMap);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(analyticsService.sandboxesPerUserStats(interval, n)).thenReturn(stringObjectHashMap);

        mvc
                .perform(
                        get("/analytics/overallStats/sandboxesPerUser?interval=" + interval +"&n=" + n) )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
