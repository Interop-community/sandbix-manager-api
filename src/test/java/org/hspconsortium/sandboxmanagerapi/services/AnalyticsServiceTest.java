package org.hspconsortium.sandboxmanagerapi.services;

import io.swagger.models.auth.In;
import org.hspconsortium.sandboxmanagerapi.controllers.UnauthorizedException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirTransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.hspconsortium.sandboxmanagerapi.services.impl.AnalyticsServiceImpl;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;
import static springfox.documentation.builders.RequestHandlerSelectors.any;

public class AnalyticsServiceTest {
    MockHttpServletRequest request = new MockHttpServletRequest();
    private FhirTransactionRepository fhirTransactionRepository = mock(FhirTransactionRepository.class);
    private AnalyticsServiceImpl analyticsService = new AnalyticsServiceImpl(fhirTransactionRepository);
    private UserService userService = mock(UserService.class);
    private SandboxService sandboxService = mock(SandboxService.class);
    private AppService appService = mock(AppService.class);
    private RuleService ruleService = mock(RuleService.class);
    private SandboxActivityLogService sandboxActivityLogService = mock(SandboxActivityLogService.class);
    private RestTemplate restTemplate = mock(RestTemplate.class);

    private User user;
    private User user2;
    private UserRole userRole1;
    private UserRole userRole2;
    private UserRole userRole3;
    private List<UserRole> userRoles;
    private Sandbox sandbox;
    private Sandbox sandbox2;
    private HashMap<String, Integer> sandboxApps;
    private List<Sandbox> sandboxes;
    private List<App> appList;
    private App app1;
    private App app2;
    private HashMap<String, String> transactionInfo;
    private List<String> schemaNames;


    @Before
    public void setup() {
        analyticsService.setUserService(userService);
        analyticsService.setSandboxService(sandboxService);
        analyticsService.setAppService(appService);
        analyticsService.setRuleService(ruleService);
        analyticsService.setSandboxActivityLogService(sandboxActivityLogService);
        analyticsService.setRestTemplate(restTemplate);

        user = new User();
        user.setSbmUserId("userID");
        user.setId(1);
        user.setEmail("Kay@interopion.com");

        user2 = new User();
        user2.setSbmUserId("userID-2");
        user2.setId(2);
        user2.setEmail("Bray@interopion.com");

        app1 = new App();
        app2 = new App();
        appList = new ArrayList<>();
        appList.add(app1);
        appList.add(app2);

//        DataSet dataSetApp;
//        dataSetApp.

        sandbox = new Sandbox();
        sandbox2 = new Sandbox();
        sandbox.setSandboxId("1");
        sandbox2.setSandboxId("2");
        sandbox.setCreatedBy(user);
        sandbox2.setCreatedBy(user);
//        sandbox.setApps(appList);

        userRole1 = new UserRole();
        userRole1.setUser(user);
        userRole2 = new UserRole();
        userRole2.setUser(user);
        userRole3 = new UserRole();
        userRole3.setUser(user);

        userRoles = new ArrayList<>();

        userRole1.setRole(Role.ADMIN);
        userRoles.add(userRole1);
        sandbox.setUserRoles(userRoles);
        sandbox2.setUserRoles(userRoles);

        userRole2.setRole(Role.MANAGE_USERS);
        userRoles.add(userRole2);
        sandbox.setUserRoles(userRoles);
        sandbox2.setUserRoles(userRoles);

        userRole3.setRole(Role.USER);
        userRoles.add(userRole3);
        sandbox.setUserRoles(userRoles);
        sandbox2.setUserRoles(userRoles);

//        sandbox.setUserRoles(userRoles);
//        sandbox2.setUserRoles(userRoles);

        sandboxes = new ArrayList<>();
        sandboxes.add(sandbox);
        sandboxes.add(sandbox2);
        user.setSandboxes(sandboxes);

        sandboxApps = new HashMap<>();
        sandboxApps.put("A",1);
        sandboxApps.put("B",2);
        sandboxApps.put("C",3);

        transactionInfo = new HashMap<>();
        transactionInfo.put("tenant", sandbox.getSandboxId());
        transactionInfo.put("secured", "true");
        transactionInfo.put("userId", user.getSbmUserId());
        transactionInfo.put("method", "A");
        transactionInfo.put("url", "http://abc.com");
        transactionInfo.put("resource", "Practitioner");
        transactionInfo.put("domain", "abcd");
        transactionInfo.put("ip_address", "111.111.111");
        transactionInfo.put("response_code", "1");

        schemaNames = new ArrayList<>();
        schemaNames.add("1");
        schemaNames.add("2");

    }

//    @Test
//    public void countSandboxesByUserTest(){
//        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
//        when(analyticsService.countSandboxesByUser(user.getSbmUserId())).thenReturn(1);
//    }

    @Test
    public void countAppsPerSandboxByUserSizeTest(){
        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxes);
        for(Sandbox sandbox: sandboxes) {
            when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(appList);
        }
        assertEquals(2, analyticsService.countAppsPerSandboxByUser(user).size());
    }

    @Test
    public void countAppsPerSandboxByUserKeyValueTest(){
        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxes);
        for(Sandbox sandbox: sandboxes) {
            when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(appList);
        }
        Integer numApps = analyticsService.countAppsPerSandboxByUser(user).get("1");
        Integer n = 2;
        assertEquals(n, numApps);
    }

    @Test
    public void countUsersPerSandboxByUserTest() {
        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxes);
        final Map<String, Integer> myResult = analyticsService.countUsersPerSandboxByUser(user);
        final Map<String, Integer> expected = new HashMap<String, Integer>() {
            {
                put("1", 1);
                put("2", 1);
            }
        };
        assertEquals(expected, myResult);
    }

    @Test
    public void handleFhirTransactionTest() {
        when(sandboxService.findBySandboxId("1")).thenReturn(sandbox);
        when(ruleService.checkIfUserCanPerformTransaction(sandbox,transactionInfo.get("method").toString(), "")).thenReturn(true);
        analyticsService.handleFhirTransaction(user, transactionInfo, "");
    }

    @Test(expected = UnauthorizedException.class)
    public void handleFhirTransactionRuleServiceFalseTest() {
        when(sandboxService.findBySandboxId("1")).thenReturn(sandbox);
        when(ruleService.checkIfUserCanPerformTransaction(sandbox,transactionInfo.get("method").toString(), "")).thenReturn(false);
        analyticsService.handleFhirTransaction(user, transactionInfo, "");
    }

    @Test
    public void handleFhirTransactionRuleServiceTrueTest() {
        when(sandboxService.findBySandboxId("1")).thenReturn(sandbox);
        when(ruleService.checkIfUserCanPerformTransaction(sandbox,transactionInfo.get("method").toString(), "")).thenReturn(true);
        analyticsService.handleFhirTransaction(user, transactionInfo, "");
    }

    @Test
    public void handleFhirTransactionUserNullTest() {
        when(sandboxService.findBySandboxId("1")).thenReturn(sandbox);
        when(ruleService.checkIfUserCanPerformTransaction(sandbox,transactionInfo.get("method").toString(), "")).thenReturn(true);
        analyticsService.handleFhirTransaction(null, transactionInfo, "");
    }

    @Test
    public void countTransactionsByPayerTest() {
        List<FhirTransaction> fhirTransaction = new ArrayList<>();
        FhirTransaction ft1 = new FhirTransaction();
        FhirTransaction ft2 = new FhirTransaction();
        FhirTransaction ft3 = new FhirTransaction();

        fhirTransaction.add(ft1);
        fhirTransaction.add(ft2);
        fhirTransaction.add(ft3);

        when(fhirTransactionRepository.findByPayerUserId(1)).thenReturn(fhirTransaction);
        Integer n = analyticsService.countTransactionsByPayer(user);
        Integer n2 = new Integer(3);
        assertEquals(n, n2);
    }

    @Test
    public void retrieveTotalMemoryByUserTest() {
        HashMap<String, Double> sandboxMemorySizes = new HashMap<>();
        sandboxMemorySizes.put("1", 1.5);
        sandboxMemorySizes.put("2", 3.5);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer " + request);
        HttpEntity<List<String>> httpEntity = new HttpEntity(schemaNames, requestHeaders);
        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxes);
        //TODO: ASK JACOB: how to mock this call
       // when(sandboxService.getSystemSandboxApiURL()).thenReturn("");
//        when(restTemplate.exchange(sandboxService.getSystemSandboxApiURL() + "/memory/user",
//                HttpMethod.PUT, httpEntity, HashMap.class).getBody()).thenReturn(sandboxMemorySizes);
        Double totalMemory = analyticsService.retrieveTotalMemoryByUser(user, "");
        Double n = new Double(5.0);
        assertEquals(totalMemory, n);

    }

    @Test
    public void retrieveMemoryInSchemasTest() {
        //TODO: same problem as above
    }

    @Test
    public void activeUserCountTest() {

    }
}
