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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = SandboxInviteController.class, secure = false)
@ContextConfiguration(classes = SandboxInviteController.class)
public class SandboxInviteControllerTest {

    @Autowired
    private MockMvc mvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @MockBean
    private SandboxInviteService sandboxInviteService;

    @MockBean
    private UserService userService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private SandboxActivityLogService sandboxActivityLogService;

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
    private SandboxInvite sandboxInvite;
    private List<SandboxInvite> sandboxInvites;

    @Before
    public void setup() {
        user = new User();
        user.setSbmUserId("me");
        user.setEmail("email");
        sandbox = new Sandbox();
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        UserRole userRole2 = new UserRole();
        userRole.setRole(Role.MANAGE_USERS);
        userRole2.setUser(user);
        userRole2.setRole(Role.ADMIN);
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole);
        sandbox.setUserRoles(userRoles);
        sandbox.setVisibility(Visibility.PRIVATE);
        sandbox.setSandboxId("sandbox");
        sandboxInvite = new SandboxInvite();
        sandboxInvite.setSandbox(sandbox);
        sandboxInvite.setInvitee(user);
        sandboxInvite.setInvitedBy(user);
        sandboxInvite.setStatus(InviteStatus.PENDING);
        sandboxInvite.setId(1);
        sandboxInvites = new ArrayList<>();
        sandboxInvites.add(sandboxInvite);
    }

    @Test
    public void createOrUpdateSandboxInviteTest() throws Exception {
        String json = json(sandboxInvite);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(sandboxInviteService.findInvitesByInviteeIdAndSandboxId(sandboxInvite.getInvitee().getSbmUserId(), sandboxInvite.getSandbox().getSandboxId())).thenReturn(sandboxInvites);
        when(sandboxService.isSandboxMember(sandbox, sandboxInvite.getInvitee())).thenReturn(false);
        when(sandboxInviteService.save(any())).thenReturn(sandboxInvite);
        when(userService.findBySbmUserId(sandboxInvite.getInvitedBy().getSbmUserId())).thenReturn(user);
        doNothing().when(emailService).sendEmail(any(), any(), any());

        mvc
                .perform(put("/sandboxinvite")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void createOrUpdateSandboxInviteTestUsingEmailNotId() throws Exception {
        User invitee = new User();
        invitee.setEmail("email");
        sandboxInvite.setInvitee(invitee);
        String json = json(sandboxInvite);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(any())).thenReturn(user);
        when(sandboxInviteService.findInvitesByInviteeIdAndSandboxId(sandboxInvite.getInvitee().getSbmUserId(), sandboxInvite.getSandbox().getSandboxId())).thenReturn(new ArrayList<>());
        when(sandboxInviteService.findInvitesByInviteeEmailAndSandboxId(sandboxInvite.getInvitee().getEmail(), sandboxInvite.getSandbox().getSandboxId())).thenReturn(sandboxInvites);
        when(sandboxService.isSandboxMember(sandbox, sandboxInvite.getInvitee())).thenReturn(false);
        when(sandboxInviteService.save(any())).thenReturn(sandboxInvite);
        doNothing().when(emailService).sendEmail(any(), any(), any());

        mvc
                .perform(put("/sandboxinvite")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void createOrUpdateSandboxInviteTestInvitesEmpty() throws Exception {
        String json = json(sandboxInvite);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(any())).thenReturn(user);
        when(sandboxInviteService.findInvitesByInviteeIdAndSandboxId(sandboxInvite.getInvitee().getSbmUserId(), sandboxInvite.getSandbox().getSandboxId())).thenReturn(new ArrayList<>());
        when(sandboxInviteService.findInvitesByInviteeEmailAndSandboxId(sandboxInvite.getInvitee().getEmail(), sandboxInvite.getSandbox().getSandboxId())).thenReturn(new ArrayList<>());
        when(sandboxInviteService.create(any())).thenReturn(sandboxInvite);
        mvc
                .perform(put("/sandboxinvite")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void createOrUpdateSandboxInviteTestSandboxNotFound() throws Exception {
        String json = json(sandboxInvite);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc
                .perform(put("/sandboxinvite")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json));
    }

    @Test
    public void getSandboxInvitesByInviteeTest() throws Exception {
        String json = json(sandboxInvites);
        when(sandboxInviteService.findInvitesByInviteeIdAndStatus(user.getSbmUserId(), sandboxInvite.getStatus())).thenReturn(sandboxInvites);

        mvc
                .perform(get("/sandboxinvite?sbmUserId=" + user.getSbmUserId() + "&status=" + sandboxInvite.getStatus().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void getSandboxInvitesByInviteeTestReturnsEmpty() throws Exception {
        when(sandboxInviteService.findInvitesByInviteeIdAndStatus(user.getSbmUserId(), sandboxInvite.getStatus())).thenReturn(null);

        mvc
                .perform(get("/sandboxinvite?sbmUserId=" + user.getSbmUserId() + "&status=" + sandboxInvite.getStatus().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void getSandboxInvitesBySandboxTest() throws Exception {
        String json = json(sandboxInvites);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(sandboxInviteService.findInvitesBySandboxIdAndStatus(sandbox.getSandboxId(), sandboxInvite.getStatus())).thenReturn(sandboxInvites);
        mvc
                .perform(get("/sandboxinvite?sandboxId=" + sandbox.getSandboxId() + "&status=" + sandboxInvite.getStatus().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void getSandboxInvitesBySandboxTestReturnsEmpty() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(sandboxInviteService.findInvitesBySandboxIdAndStatus(sandbox.getSandboxId(), sandboxInvite.getStatus())).thenReturn(null);
        mvc
                .perform(get("/sandboxinvite?sandboxId=" + sandbox.getSandboxId() + "&status=" + sandboxInvite.getStatus().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test(expected = NestedServletException.class)
    public void getSandboxInvitesBySandboxTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc
                .perform(get("/sandboxinvite?sandboxId=" + sandbox.getSandboxId() + "&status=" + sandboxInvite.getStatus().toString()));
    }

    @Test(expected = NestedServletException.class)
    public void getSandboxInvitesBySandboxTestUserNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        mvc
                .perform(get("/sandboxinvite?sandboxId=" + sandbox.getSandboxId() + "&status=" + sandboxInvite.getStatus().toString()));
    }

    @Test
    public void updateSandboxInviteTest() throws Exception {
        String json = json(sandboxInvite);
        when(sandboxInviteService.getById(sandboxInvite.getId())).thenReturn(sandboxInvite);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        mvc
                .perform(put("/sandboxinvite/" + sandboxInvite.getId() + "?status=" + InviteStatus.ACCEPTED.toString())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
        verify(sandboxActivityLogService).sandboxUserInviteAccepted(sandbox, user);
        verify(sandboxInviteService).save(sandboxInvite);
    }

    @Test(expected = NestedServletException.class)
    public void updateSandboxInviteTestInviteNotFound() throws Exception {
        String json = json(sandboxInvite);
        when(sandboxInviteService.getById(sandboxInvite.getId())).thenReturn(null);
        mvc
                .perform(put("/sandboxinvite/" + sandboxInvite.getId() + "?status=" + InviteStatus.ACCEPTED.toString())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json));
    }

    @Test(expected = NestedServletException.class)
    public void updateSandboxInviteInviteeNotFound() throws Exception {
        String json = json(sandboxInvite);
        when(sandboxInviteService.getById(sandboxInvite.getId())).thenReturn(sandboxInvite);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        mvc
                .perform(put("/sandboxinvite/" + sandboxInvite.getId() + "?status=" + InviteStatus.ACCEPTED.toString())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json));
    }

    @Test
    public void updateSandboxInviteTestStatusIsRejected() throws Exception {
        String json = json(sandboxInvite);
        when(sandboxInviteService.getById(sandboxInvite.getId())).thenReturn(sandboxInvite);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        mvc
                .perform(put("/sandboxinvite/" + sandboxInvite.getId() + "?status=" + InviteStatus.REJECTED.toString())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
        verify(sandboxActivityLogService).sandboxUserInviteRejected(sandboxInvite.getSandbox(), sandboxInvite.getInvitee());
    }

    @Test
    public void updateSandboxInviteTestStatusRevoked() throws Exception {
        String json = json(sandboxInvite);
        when(sandboxInviteService.getById(sandboxInvite.getId())).thenReturn(sandboxInvite);
        when(userService.findBySbmUserId(any())).thenReturn(user);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        mvc
                .perform(put("/sandboxinvite/" + sandboxInvite.getId() + "?status=" + InviteStatus.REVOKED.toString())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
        verify(sandboxActivityLogService).sandboxUserInviteRevoked(sandboxInvite.getSandbox(), user);
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
