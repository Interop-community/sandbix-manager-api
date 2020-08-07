package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.controllers.dto.UserPersonaCredentials;
import org.hspconsortium.sandboxmanagerapi.controllers.dto.UserPersonaDto;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = UserPersonaController.class)
@ContextConfiguration(classes = UserPersonaController.class)
public class UserPersonaControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserPersonaService userPersonaService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthorizationService authorizationService;

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
    private UserPersona userPersona;
    private List<UserPersona> userPersonas;
    private UserPersonaDto userPersonaDto;

    @Before
    public void setup() {
        sandbox = new Sandbox();
        user = new User();
        userPersona = new UserPersona();
        user.setSbmUserId("me");
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole);
        sandbox.setUserRoles(userRoles);
        sandbox.setVisibility(Visibility.PUBLIC);
        sandbox.setSandboxId("sandbox");
        userPersona.setSandbox(sandbox);
        userPersona.setCreatedBy(user);
        userPersona.setVisibility(Visibility.PRIVATE);
        userPersona.setPersonaUserId("user");
        userPersona.setId(1);
        userPersona.setFhirName("user");
        userPersona.setResourceUrl("url");
        userPersonas = new ArrayList<>();
        userPersonas.add(userPersona);
        userPersonaDto = new UserPersonaDto();
        userPersonaDto.setName(userPersona.getFhirName());
        userPersonaDto.setUsername(userPersona.getPersonaUserId());
        userPersonaDto.setResourceUrl(userPersona.getResourceUrl());
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void createUserPersonaTest() throws Exception {
        String json = json(userPersona);
        when(userPersonaService.create(any())).thenReturn(userPersona);

        mvc.perform(post("/userPersona")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void createUserPersonaTestSandboxNotFound() throws Exception {
        String json = json(userPersona);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);

        mvc.perform(post("/userPersona")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(json));
    }

    @Test
    public void updateUserPersonaTest() throws Exception {
        String json = json(userPersona);
        when(userPersonaService.update(any())).thenReturn(userPersona);

        mvc.perform(put("/userPersona")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void updateUserPersonaTestSandboxNotFound() throws Exception {
        String json = json(userPersona);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);

        mvc.perform(put("/userPersona")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(json));
    }

    @Test
    public void getSandboxUserPersonaTest() throws Exception {
        String json = json(userPersonas);
        when(userPersonaService.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), user.getSbmUserId(), Visibility.PUBLIC)).thenReturn(userPersonas);
        mvc.perform(get("/userPersona?sandboxId=" + sandbox.getSandboxId()))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getSandboxUserPersonaTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc.perform(get("/userPersona?sandboxId=" + sandbox.getSandboxId()));
    }

    @Test
    public void getSandboxDefaultUserPersonaTest() throws Exception {
        String json = json(userPersona);
        when(userPersonaService.findDefaultBySandboxId(sandbox.getSandboxId(), user.getSbmUserId(), Visibility.PUBLIC)).thenReturn(userPersona);
        mvc.perform(get("/userPersona/default?sandboxId=" + sandbox.getSandboxId()))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getSandboxDefaultUserPersonaTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc.perform(get("/userPersona/default?sandboxId=" + sandbox.getSandboxId()));
    }

    @Test
    public void checkForUserPersonaByIdTest() throws Exception {
        when(userPersonaService.findByPersonaUserId(userPersona.getPersonaUserId())).thenReturn(userPersona);

        mvc.perform(get("/userPersona?lookUpId=" + userPersona.getPersonaUserId()))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/plain;charset=UTF-8"))
           .andExpect(content().string(userPersona.getPersonaUserId()));
    }

    @Test
    public void checkForUserPersonaByIdTestReturnsNothing() throws Exception {
        when(userPersonaService.findByPersonaUserId(userPersona.getPersonaUserId())).thenReturn(null);

        mvc.perform(get("/userPersona?lookUpId=" + userPersona.getPersonaUserId()))
           .andExpect(status().isOk())
           .andExpect(content().string(""));
    }

    @Test
    public void deleteSandboxUserPersonaTest() throws Exception {
        when(userPersonaService.getById(userPersona.getId())).thenReturn(userPersona);
        doNothing().when(userPersonaService).delete(userPersona);
        mvc.perform(delete("/userPersona/" + userPersona.getId()))
           .andExpect(status().isOk());
        verify(userPersonaService).delete(userPersona);
    }

//    @Test(expected = NestedServletException.class)
//    public void deleteSandboxUserPersonaTestUserPersonaNotFound() throws Exception {
//        when(userPersonaService.getById(userPersona.getId())).thenReturn(null);
//        mvc
//                .perform(
//                        delete("/userPersona/" + userPersona.getId()));
//    }

    @Test
    public void readUserPersonaTest() throws Exception {
        String json = json(userPersonaDto);
        when(userPersonaService.findByPersonaUserId(userPersona.getPersonaUserId())).thenReturn(userPersona);
        mvc.perform(get("/userPersona/" + userPersona.getPersonaUserId()))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test
    public void readUserPersonaTestUserPersonaNotFound() throws Exception {
        when(userPersonaService.findByPersonaUserId(userPersona.getPersonaUserId())).thenReturn(null);
        mvc.perform(get("/userPersona/" + userPersona.getPersonaUserId()))
           .andExpect(status().isNotFound());
    }


    @Test
    public void authenticateUserPersonaTest() throws Exception {
        UserPersonaCredentials userPersonaCredentials = new UserPersonaCredentials();
        userPersonaCredentials.setUsername("username");
        userPersonaCredentials.setPassword("password");
        userPersonaCredentials.setJwt("jwt");
        String json = json(userPersonaCredentials);
        when(userPersonaService.findByPersonaUserId(userPersonaCredentials.getUsername())).thenReturn(userPersona);
        userPersona.setPassword("password");
        when(jwtService.createSignedJwt(userPersonaCredentials.getUsername())).thenReturn("jwt");
        mvc.perform(post("/userPersona/authenticate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test
    public void authenticateUserPersonaTestPasswordsDontMatch() throws Exception {
        UserPersonaCredentials userPersonaCredentials = new UserPersonaCredentials();
        userPersonaCredentials.setUsername("username");
        userPersonaCredentials.setPassword("password");
        userPersonaCredentials.setJwt("jwt");
        String json = json(userPersonaCredentials);
        when(userPersonaService.findByPersonaUserId(userPersonaCredentials.getUsername())).thenReturn(userPersona);
        userPersona.setPassword("different-password");
        when(jwtService.createSignedJwt(userPersonaCredentials.getUsername())).thenReturn("jwt");
        mvc.perform(post("/userPersona/authenticate")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(json))
           .andExpect(status().isForbidden());
    }

    @Test
    public void authenticateUserPersonaTestUserNotFound() throws Exception {
        UserPersonaCredentials userPersonaCredentials = new UserPersonaCredentials();
        userPersonaCredentials.setUsername("username");
        userPersonaCredentials.setPassword("password");
        String json = json(userPersonaCredentials);
        when(userPersonaService.findByPersonaUserId(userPersonaCredentials.getUsername())).thenReturn(null);
        mvc.perform(post("/userPersona/authenticate")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(json))
           .andExpect(status().isNotFound());
    }

    @Test
    public void authenticateUserPersonaTestUserCredentialsHasNoUserName() throws Exception {
        UserPersonaCredentials userPersonaCredentials = new UserPersonaCredentials();
        String json = json(userPersonaCredentials);
        mvc.perform(post("/userPersona/authenticate")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(json))
           .andExpect(status().isBadRequest());
    }

    @Test
    public void authenticateUserPersonaTestUserPersonaCredentialsNull() throws Exception {
        mvc.perform(post("/userPersona/authenticate"))
           .andExpect(status().isBadRequest());
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
