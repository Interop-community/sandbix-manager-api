package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)
//@WebMvcTest(value = DataManagerController.class, secure = false)
//@ContextConfiguration(classes = DataManagerController.class)
public class DataManagerControllerTest {

//    @Autowired
//    private MockMvc mvc;
//
//    private HttpMessageConverter mappingJackson2HttpMessageConverter;
//
//    @MockBean
//    private SandboxService sandboxService;
//
//    @MockBean
//    private UserService userService;
//
//    @MockBean
//    private SandboxActivityLogService sandboxActivityLogService;
//
//    @MockBean
//    private DataManagerService dataManagerService;
//
//    @MockBean
//    private AuthorizationService authorizationService;
//
//    @Autowired
//    void setConverters(HttpMessageConverter<?>[] converters) {
//
//        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
//                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
//                .findAny()
//                .orElse(null);
//
//        assertNotNull("the JSON message converter must not be null",
//                this.mappingJackson2HttpMessageConverter);
//    }
//    private Sandbox sandbox;
//    private User user;
//    private SandboxActivityLog sandboxActivityLog;
//
//    @Before
//    public void setup() {
//        sandbox = new Sandbox();
//        sandbox.setSandboxId("sandbox");
//        user = new User();
//        user.setSbmUserId("me");
//        UserRole userRole = new UserRole();
//        userRole.setUser(user);
//        List<UserRole> userRoles = new ArrayList<>();
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        SandboxImport sandboxImport = new SandboxImport();
//        List<SandboxImport> sandboxImports = new ArrayList<>();
//        sandboxImports.add(sandboxImport);
//        sandbox.setImports(sandboxImports);
//        Set<SystemRole> systemRoles = new HashSet<>();
//        systemRoles.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoles);
//        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
//        sandboxActivityLog = new SandboxActivityLog();
//        sandboxActivityLog.setSandbox(sandbox);
//        sandboxActivityLog.setUser(user);
//    }
//
//    @Test
//    public void getSandboxImportsTest() throws Exception {
//        String json = json(sandbox.getImports());
//        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
//        mvc
//                .perform(get("/fhirdata/import?sandboxId=" + sandbox.getSandboxId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//    }
//
//    @Test(expected = NestedServletException.class)
//    public void getSandboxImportsTestSandboxNull() throws Exception {
//        String json = json(sandbox.getImports());
//        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
//        mvc
//                .perform(get("/fhirdata/import?sandboxId=" + sandbox.getSandboxId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//    }
//
//    @Test
//    public void importAllPatientDataTest() throws Exception {
//        when(authorizationService.getBearerToken(any())).thenReturn("");
//        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
//        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
//        doNothing().when(authorizationService).checkUserAuthorization(any(), any());
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
//        when(authorizationService.checkSystemUserCanManageSandboxDataAuthorization(any(), any(), any())).thenReturn("");
//        when(sandboxActivityLogService.sandboxImport(sandbox, user)).thenReturn(sandboxActivityLog);
//        when(dataManagerService.importPatientData(sandbox, "", "1", "1", "1")).thenReturn("SUCCESS");
//        mvc
//                .perform(get("/fhirdata/import?sandboxId=" + sandbox.getSandboxId() + "&patientId=1&fhirIdPrefix=1&endpoint=1"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
//    }
//
//    @Test
//    public void resetTest() throws Exception {
//        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
//        when(authorizationService.getBearerToken(any())).thenReturn("token");
//        when(dataManagerService.reset(sandbox, "token")).thenReturn("SUCCESS");
//        mvc
//                .perform(post("/fhirdata/reset?sandboxId=" + sandbox.getSandboxId() + "&dataSet=DEFAULT"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("SUCCESS"));
//    }
//
//    @Test(expected = NestedServletException.class)
//    public void resetTestSandboxNull() throws Exception {
//        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
//        when(authorizationService.getBearerToken(any())).thenReturn("token");
//        when(dataManagerService.reset(sandbox, "token")).thenReturn("SUCCESS");
//        mvc
//                .perform(post("/fhirdata/reset?sandboxId=" + sandbox.getSandboxId() + "&dataSet=DEFAULT"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("SUCCESS"));
//    }
//
//    @SuppressWarnings("unchecked")
//    private String json(Object o) throws IOException {
//        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
//        mappingJackson2HttpMessageConverter.write(
//                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
//        return mockHttpOutputMessage.getBodyAsString();
//    }
}
