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
@WebMvcTest(value = LaunchScenarioController.class, secure = false)
@ContextConfiguration(classes = LaunchScenarioController.class)
public class LaunchScenarioControllerTest {

    @Autowired
    private MockMvc mvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private LaunchScenarioService launchScenarioService;

    @MockBean
    private UserService userService;

    @MockBean
    private AppService appService;

    @MockBean
    private UserPersonaService userPersonaService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private UserLaunchService userLaunchService;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private CdsHookService cdsHookService;

    @MockBean
    private CdsServiceEndpointService cdsServiceEndpointService;

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
    private App app;
    private LaunchScenario launchScenario;
    private UserLaunch userLaunch;
    private List<LaunchScenario> launchScenarios;
    private UserPersona userPersona;

    @Before
    public void setup() {
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
        sandbox = new Sandbox();
        sandbox.setSandboxId("sandbox");
        user = new User();
        user.setSbmUserId("me");
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole);
        sandbox.setUserRoles(userRoles);
        app = new App();
        app.setId(1);
        app.setSandbox(sandbox);
        launchScenario = new LaunchScenario();
        launchScenario.setId(1);
        launchScenario.setSandbox(sandbox);
        launchScenario.setCreatedBy(user);
        launchScenario.setVisibility(Visibility.PRIVATE);
        launchScenarios = new ArrayList<>();
        launchScenarios.add(launchScenario);
        userLaunch = new UserLaunch();
        userPersona = new UserPersona();
        userPersona.setSandbox(sandbox);
        userPersona.setId(1);
    }

    @Test
    public void createLaunchScenarioTest() throws Exception {
        String json = json(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(launchScenario.getCreatedBy().getSbmUserId())).thenReturn(user);
        when(launchScenarioService.create(any())).thenReturn(launchScenario);
        when(userLaunchService.create(any())).thenReturn(userLaunch);
        mvc
                .perform(post("/launchScenario")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void createLaunchScenarioTestSandboxNotFound() throws Exception {
        String json = json(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(null);
        mvc
                .perform(post("/launchScenario")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json));
    }

    @Test(expected = NestedServletException.class)
    public void createLaunchScenarioTestUserNotFound() throws Exception {
        String json = json(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(launchScenario.getCreatedBy().getSbmUserId())).thenReturn(null);
        mvc
                .perform(post("/launchScenario")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json));
    }

    @Test
    public void updateLaunchScenarioTest() throws Exception {
        String json = json(launchScenario);
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(launchScenarioService.update(any())).thenReturn(launchScenario);
        mvc
                .perform(put("/launchScenario/" + launchScenario.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void updateLaunchScenarioTestLaunchScenarioNotFound() throws Exception {
        String json = json(launchScenario);
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(null);
        mvc
                .perform(put("/launchScenario/" + launchScenario.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json));
    }

    @Test(expected = NestedServletException.class)
    public void updateLaunchScenarioTestSandboxNotFound() throws Exception {
        String json = json(launchScenario);
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(null);
        mvc
                .perform(put("/launchScenario/" + launchScenario.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json));
    }

    @Test
    public void updateLaunchTimestampTest() throws Exception {
        String json = json(launchScenario);
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(userLaunchService.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId())).thenReturn(userLaunch);
        mvc
                .perform(put("/launchScenario/" + launchScenario.getId() + "/launched")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void updateLaunchTimestampTestUserLaunchNotFound() throws Exception {
        String json = json(launchScenario);
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(userLaunchService.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId())).thenReturn(null);
        mvc
                .perform(put("/launchScenario/" + launchScenario.getId() + "/launched")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void updateLaunchTimestampTestLaunchScenarioNotFound() throws Exception {
        String json = json(launchScenario);
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(null);
        mvc
                .perform(put("/launchScenario/" + launchScenario.getId() + "/launched")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json));
    }

    @Test(expected = NestedServletException.class)
    public void updateLaunchTimestampTestSandboxNotFound() throws Exception {
        String json = json(launchScenario);
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(null);
        mvc
                .perform(put("/launchScenario/" + launchScenario.getId() + "/launched")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json));
    }

//    @Test
//    public void getLaunchScenariosForAppTest() throws Exception {
//        String json = json(launchScenarios);
//        when(appService.getById(app.getId())).thenReturn(app);
//        when(launchScenarioService.findByAppIdAndSandboxId(app.getId(), app.getSandbox().getSandboxId())).thenReturn(launchScenarios);
//        mvc
//                .perform(get("/launchScenario?appId=" + app.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//    }

//    @Test(expected = NestedServletException.class)
//    public void getLaunchScenariosForAppTestAppNotFound() throws Exception {
//        when(appService.getById(app.getId())).thenReturn(null);
//        mvc
//                .perform(get("/launchScenario?appId=" + app.getId()));
//    }

//    @Test
//    public void getLaunchScenariosForPersonaTest() throws Exception {
//        String json = json(launchScenarios);
//        when(userPersonaService.getById(userPersona.getId())).thenReturn(userPersona);
//        when(launchScenarioService.findByUserPersonaIdAndSandboxId(userPersona.getId(), userPersona.getSandbox().getSandboxId())).thenReturn(launchScenarios);
//        mvc
//                .perform(get("/launchScenario?userPersonaId=" + userPersona.getId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().json(json));
//    }

//    @Test(expected = NestedServletException.class)
//    public void getLaunchScenariosForPersonaTestUserPersonaNotFound() throws Exception {
//        when(userPersonaService.getById(userPersona.getId())).thenReturn(null);
//        mvc
//                .perform(get("/launchScenario?userPersonaId=" + userPersona.getId()));
//    }

    @Test
    public void deleteLaunchScenarioTest() throws Exception {
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(sandbox);
        doNothing().when(launchScenarioService).delete(launchScenario);
        mvc
                .perform(delete("/launchScenario/" + launchScenario.getId()))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void deleteLaunchScenarioTestLaunchScenarioNotFound() throws Exception {
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(null);
        doNothing().when(launchScenarioService).delete(launchScenario);
        mvc
                .perform(delete("/launchScenario/" + launchScenario.getId()));
    }

    @Test(expected = NestedServletException.class)
    public void deleteLaunchScenarioTestSandboxNotFound() throws Exception {
        when(launchScenarioService.getById(launchScenario.getId())).thenReturn(launchScenario);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(null);
        doNothing().when(launchScenarioService).delete(launchScenario);
        mvc
                .perform(delete("/launchScenario/" + launchScenario.getId()));
    }

    @Test
    public void getLaunchScenariosTest() throws Exception {
        String json = json(launchScenarios);
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(launchScenarioService.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), user.getSbmUserId(), Visibility.PUBLIC)).thenReturn(launchScenarios);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(launchScenarioService.updateLastLaunchForCurrentUser(launchScenarios, user)).thenReturn(launchScenarios);
        mvc
                .perform(get("/launchScenario?sandboxId=" + sandbox.getSandboxId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getLaunchScenariosTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(null);
        mvc
                .perform(get("/launchScenario?sandboxId=" + sandbox.getSandboxId()));
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
