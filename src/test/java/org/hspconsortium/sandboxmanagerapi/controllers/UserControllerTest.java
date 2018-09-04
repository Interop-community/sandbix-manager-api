package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.Role;
import org.hspconsortium.sandboxmanagerapi.model.SystemRole;
import org.hspconsortium.sandboxmanagerapi.model.UserPersona;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.hspconsortium.sandboxmanagerapi.model.User;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = UserController.class, secure = false)
@ContextConfiguration(classes = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private UserService userService;

    @MockBean
    private SandboxInviteService sandboxInviteService;

    @MockBean
    private UserPersonaService userPersonaService;

    @MockBean
    private SandboxActivityLogService sandboxActivityLogService;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    private User user;
    private List<User> userList;
    private UserPersona userPersona;

    @Before
    public void setup() {
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
        user = new User();
        user.setSbmUserId("me");
        user.setName("user");
        user.setEmail("user@email");
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        userList = new ArrayList<>();
        userList.add(user);
        Set<SystemRole> systemRoles = new HashSet<>();
        systemRoles.add(SystemRole.ADMIN);
        user.setSystemRoles(systemRoles);
        userPersona = new UserPersona();
    }

    @Test
    public void getUserTest() throws Exception {
        String json = json(user);
        when(oAuthService.getOAuthUserName(any())).thenReturn(user.getName());
        when(oAuthService.getOAuthUserEmail(any())).thenReturn(user.getEmail());
        mvc
                .perform(get("/user?sbmUserId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void getUserTestUsingEmail() throws Exception {
        String json = json(user);
        when(oAuthService.getOAuthUserName(any())).thenReturn(user.getName());
        when(oAuthService.getOAuthUserEmail(any())).thenReturn(user.getEmail());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(userService.findByUserEmail(user.getEmail())).thenReturn(user);
        mvc
                .perform(get("/user?sbmUserId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
        verify(userService).findByUserEmail(user.getEmail());
    }

    @Test
    public void getUserTestCreateUserWhoDoesntExist() throws Exception {
        when(oAuthService.getOAuthUserName(any())).thenReturn(user.getName());
        when(oAuthService.getOAuthUserEmail(any())).thenReturn(user.getEmail());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(userService.findByUserEmail(user.getEmail())).thenReturn(null);
        mvc
                .perform(get("/user?sbmUserId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        verify(userPersonaService).findByPersonaUserId(user.getSbmUserId());
        verify(sandboxActivityLogService).systemUserCreated(any(), any());
        verify(userService).save(any());
    }

    @Test
    public void getUserTestCreateUserEmptyFields() throws Exception {
        when(oAuthService.getOAuthUserName(any())).thenReturn(user.getName());
        when(oAuthService.getOAuthUserEmail(any())).thenReturn(user.getEmail());
        user.setName(null);
        user.setEmail("otherEmail@email");
        user.setSystemRoles(new HashSet<>());
        mvc
                .perform(get("/user?sbmUserId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        verify(sandboxActivityLogService).systemUserRoleChange(user, SystemRole.USER, true);
        verify(sandboxActivityLogService).systemUserRoleChange(user, SystemRole.CREATE_SANDBOX, true);
        verify(sandboxInviteService).mergeSandboxInvites(user, user.getEmail());
        verify(userService).save(any());
    }

    @Test
    public void getUserTestIsPersona() throws Exception {
        when(oAuthService.getOAuthUserName(any())).thenReturn(user.getName());
        when(oAuthService.getOAuthUserEmail(any())).thenReturn(user.getEmail());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(userPersona);
        mvc
                .perform(get("/user?sbmUserId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void getAllUsersTest() throws Exception {
        String json = json(userList);
        when(userService.findAllUsers()).thenReturn(userList);
        mvc
                .perform(get("/user/all?sbmUserId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getAllUsersTestNotAuthorized() throws Exception {
        String json = json(userList);
        user.setSystemRoles(new HashSet<>());
        when(userService.findAllUsers()).thenReturn(userList);
        mvc
                .perform(get("/user/all?sbmUserId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void acceptTermsOfUseTest() throws Exception {
        String termsId = "id";
        mvc
                .perform(post("/user/acceptterms?sbmUserId=" + user.getSbmUserId() + "&termsId=" + termsId))
                .andExpect(status().isOk());
        verify(userService).acceptTermsOfUse(user, termsId);
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
