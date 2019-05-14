package org.hspconsortium.sandboxmanagerapi.controllers;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = CdsServiceEndpointController.class, secure = false)
@ContextConfiguration(classes = CdsServiceEndpointController.class)
public class CdsServiceEndpointControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CdsServiceEndpointService cdsServiceEndpointService;

    @MockBean
    private CdsHookService cdsHookService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private RuleService ruleService;

    @MockBean
    private UserService userService;

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

    private CdsServiceEndpoint cdsServiceEndpoint;
    private CdsHook cdsHook;
    private User user;
    private Sandbox sandbox;
    private List<CdsHook> cdsHooks = new ArrayList<>();

    @Before
    public void setup() {
        cdsServiceEndpoint = new CdsServiceEndpoint();
        cdsHook = new CdsHook();
        user = new User();
        sandbox = new Sandbox();

        user.setSbmUserId("me");
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole);
        sandbox.setUserRoles(userRoles);
        sandbox.setVisibility(Visibility.PUBLIC);
        sandbox.setSandboxId("sandbox");

        cdsHook.setHook("patient-view");
        cdsHook.setTitle("Sends a Demo Info Card");
        cdsHook.setHookUrl("http://www.google.com");
        cdsHook.setHookId("demo-suggestion-card");

        cdsHooks.add(cdsHook);

        cdsServiceEndpoint.setId(1);
        cdsServiceEndpoint.setSandbox(sandbox);
        cdsServiceEndpoint.setCreatedBy(user);
        cdsServiceEndpoint.setCdsHooks(cdsHooks);
        cdsServiceEndpoint.setUrl("http://www.google.com");
    }

    @Test
    public void createCdsServiceEndpointTest() throws Exception {
        String json = json(cdsServiceEndpoint);
        when(sandboxService.findBySandboxId(cdsServiceEndpoint.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(ruleService.checkIfUserCanCreateApp(sandbox)).thenReturn(true);
        when(authorizationService.checkSandboxUserReadAuthorization(any(), any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(cdsServiceEndpointService.create(any(), any())).thenReturn(cdsServiceEndpoint);
        mvc
                .perform(
                        post("/cds-services")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void createCdsServiceEndpointTestSandboxNotFound() throws Exception {
        String json = json(cdsServiceEndpoint);
        when(sandboxService.findBySandboxId(cdsServiceEndpoint.getSandbox().getSandboxId())).thenReturn(null);
        mvc
                .perform(
                        post("/cds-services")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json));
    }

    @Test
    public void createTestUserCantCreateCdsService() throws Exception {
        String json = json(cdsServiceEndpoint);
        when(sandboxService.findBySandboxId(cdsServiceEndpoint.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(ruleService.checkIfUserCanCreateApp(sandbox)).thenReturn(false);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(cdsServiceEndpointService.create(any(), any())).thenReturn(cdsServiceEndpoint);
        mvc
                .perform(
                        post("/cds-services")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void updateTest() throws Exception {
        String json = json(cdsServiceEndpoint);
        when(cdsServiceEndpointService.getById(cdsServiceEndpoint.getId())).thenReturn(cdsServiceEndpoint);
        when(sandboxService.findBySandboxId(cdsServiceEndpoint.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(ruleService.checkIfUserCanCreateApp(sandbox)).thenReturn(true);
        when(authorizationService.checkSandboxUserReadAuthorization(any(), any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(cdsServiceEndpointService.update(any())).thenReturn(cdsServiceEndpoint);

        mvc
                .perform(
                        put("/cds-services/" + cdsServiceEndpoint.getId())
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void updateTestCdsServiceEndpointNotFound() throws Exception {
        String json = json(cdsServiceEndpoint);
        when(cdsServiceEndpointService.getById(1)).thenReturn(null);
        mvc
                .perform(
                        put("/cds-services/" + cdsServiceEndpoint.getId())
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void updateTestCdsServiceEndpointIdNotMatch() throws Exception {
        String json = json(cdsServiceEndpoint);
        when(cdsServiceEndpointService.update(any())).thenReturn(cdsServiceEndpoint);
        mvc
                .perform(
                        put("/cds-services/" + 3)
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void getCdsServiceEndpointsTest() throws Exception {
        List<CdsServiceEndpoint> cdsServiceEndpoints = new ArrayList<>();
        cdsServiceEndpoints.add(cdsServiceEndpoint);
        String json = json(cdsServiceEndpoints);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.checkSandboxUserReadAuthorization(any(), any())).thenReturn(user.getSbmUserId());
        when(cdsServiceEndpointService.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), user.getSbmUserId(), Visibility.PUBLIC)).thenReturn(cdsServiceEndpoints);
        when(cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId())).thenReturn(cdsHooks);
        mvc
                .perform(get("/cds-services?sandboxId=" + sandbox.getSandboxId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getCdsServiceEndpointsTestSandoxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        when(cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId())).thenReturn(cdsHooks);
        mvc
                .perform(get("/cds-services?sandboxId=" + sandbox.getSandboxId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void getCdsServiceEndpointTest() throws Exception {
        String json = json(cdsServiceEndpoint);
        when(cdsServiceEndpointService.getById(cdsServiceEndpoint.getId())).thenReturn(cdsServiceEndpoint);
        when(cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId())).thenReturn(cdsHooks);
        mvc
                .perform(get("/cds-services/" + cdsServiceEndpoint.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getCdsServiceEndpointNotFoundTest() throws Exception {
        when(cdsServiceEndpointService.getById(cdsServiceEndpoint.getId())).thenReturn(null);
        mvc
                .perform(get("/cds-services/" + cdsServiceEndpoint.getId()));
    }

    @Test
    public void deleteTest() throws Exception {
        when(cdsServiceEndpointService.getById(cdsServiceEndpoint.getId())).thenReturn(cdsServiceEndpoint);
        doNothing().when(cdsServiceEndpointService).delete(cdsServiceEndpoint);
        mvc
                .perform(delete("/cds-services/" + cdsServiceEndpoint.getId()))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void deleteTestCdsServiceEndpointNotFound() throws Exception {
        doNothing().when(cdsServiceEndpointService).delete(cdsServiceEndpoint);
        mvc
                .perform(delete("/cds-services/" + cdsServiceEndpoint.getId()));
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
