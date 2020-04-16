package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.json.JSONObject;
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
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = SandboxController.class, secure = false)
@ContextConfiguration(classes = SandboxController.class)
public class SandboxControllerTest {

    @Autowired
    private MockMvc mvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private UserService userService;

    @MockBean
    private SandboxInviteService sandboxInviteService;

    @MockBean
    private UserAccessHistoryService userAccessHistoryService;

    @MockBean
    private SandboxActivityLogService sandboxActivityLogService;

    @MockBean
    private AuthorizationService authorizationService;

    @Inject
    private SandboxController sandboxController;

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
    private User user2;
    private HashMap<String, Sandbox> sandboxHashMap;

    @Before
    public void setup() {
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
        user = new User();
        sandbox = new Sandbox();
        user.setSbmUserId("me");
        user.setId(1);
        user2 = new User();
        user2.setSbmUserId("removedUser");
        user2.setId(2);
        UserRole userRole = new UserRole();
        UserRole userRole2 = new UserRole();
        userRole.setUser(user);
        userRole2.setUser(user2);
        userRole.setRole(Role.ADMIN);
        userRole2.setRole(Role.USER);
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole);
        userRoles.add(userRole2);
        sandbox.setUserRoles(userRoles);
        sandbox.setVisibility(Visibility.PRIVATE);
        sandbox.setSandboxId("sandbox");
        sandbox.setCreatedBy(user);
        sandbox.setId(1);
        sandbox.setName("sandbox");

        Set<SystemRole> systemRoles = new HashSet<>();
        systemRoles.add(SystemRole.ADMIN);
        systemRoles.add(SystemRole.CREATE_SANDBOX);
        user.setSystemRoles(systemRoles);
        user2.setSystemRoles(systemRoles);
        sandboxHashMap = new HashMap<>();
        sandboxHashMap.put("clonedSandbox", sandbox);
        sandboxHashMap.put("newSandbox", sandbox);
        when(userService.findBySbmUserId(sandbox.getCreatedBy().getSbmUserId())).thenReturn(user);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        ReflectionTestUtils.setField(sandboxController, "templateSandboxIds", new String[1]);
    }

//    @Test(expected = NestedServletException.class)
//    public void createSandboxTestDuplicateSandboxId() throws Exception {
//        String json = json(sandbox);
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
//        mvc
//                .perform(
//                        post("/sandbox")
//                                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                                .content(json));
//    }

    @Test
    public void cloneSandboxTest() throws Exception {
        String json1 = json(sandboxHashMap);
        String json2 = json(sandbox);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        when(sandboxService.clone(any(), any(), any(), any())).thenReturn(sandbox);
        mvc
                .perform(post("/sandbox/clone")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json2));
    }

//    @Test
//    public void checkForSandboxByIdTest() throws Exception {
//        String response = "{\"sandboxId\": \"" + sandbox.getSandboxId() +"\"}";
//        mvc
//                .perform(get("/sandbox?lookUpId=" + sandbox.getSandboxId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().string(response));
//    }
//
//    @Test
//    public void checkForSandboxByIdTestSandboxNotFound() throws Exception {
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
//        mvc
//                .perform(get("/sandbox?lookUpId=" + sandbox.getSandboxId()))
//                .andExpect(status().isOk())
//                .andExpect(content().string(""));
//    }

//    @Test
//    public void getSandboxByIdTest() throws Exception {
//        String response = "{\"sandboxId\": \"" + sandbox.getSandboxId() + "\",\"apiEndpointIndex\": \"" + sandbox.getApiEndpointIndex() + "\",\"allowOpenAccess\": \"" + sandbox.isAllowOpenAccess() + "\"}";
//        mvc
//                .perform(get("/sandbox?sandboxId=" + sandbox.getSandboxId()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(content().string(response));
//    }
//
//    @Test
//    public void getSandboxByIdTestSandboxNotFound() throws Exception {
//        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
//        mvc
//                .perform(get("/sandbox?sandboxId=" + sandbox.getSandboxId()))
//                .andExpect(status().isOk())
//                .andExpect(content().string(""));
//    }

    @Test
    public void getSandboxByIdTest2() throws Exception {
        String json = json(sandbox);
        mvc
                .perform(get("/sandbox/" + sandbox.getSandboxId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getSandboxByIdTest2SandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc
                .perform(get("/sandbox/" + sandbox.getId()));
    }

    @Test
    public void getSandboxByIdTest2NotSandboxMember() throws Exception {
        String json = json(sandbox);
        when(sandboxService.isSandboxMember(sandbox, user)).thenReturn(false);
        mvc
                .perform(get("/sandbox/" + sandbox.getSandboxId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void getSandboxByIdTest2SandboxVisibilityPublic() throws Exception {
        sandbox.setVisibility(Visibility.PUBLIC);
        String json = json(sandbox);
        when(sandboxService.isSandboxMember(sandbox, user)).thenReturn(false);
        mvc
                .perform(get("/sandbox/" + sandbox.getSandboxId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test
    public void deleteSandboxByIdTest() throws Exception {
        List<SandboxInvite> sandboxInvites = new ArrayList<>();
        SandboxInvite sandboxInvite = new SandboxInvite();
        sandboxInvites.add(sandboxInvite);
        when(sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId())).thenReturn(sandboxInvites);
        mvc
                .perform(delete("/sandbox/" + sandbox.getSandboxId()))
                .andExpect(status().isOk());
        verify(sandboxInviteService).delete(sandboxInvite);
        verify(sandboxService).delete(any(), any());
    }

    @Test
    public void deleteSandboxByIdTestEmptyInviteList() throws Exception {
        List<SandboxInvite> sandboxInvites = new ArrayList<>();
        SandboxInvite sandboxInvite = new SandboxInvite();
        when(sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId())).thenReturn(sandboxInvites);
        mvc
                .perform(delete("/sandbox/" + sandbox.getSandboxId()))
                .andExpect(status().isOk());
        verify(sandboxInviteService, never()).delete(sandboxInvite);
        verify(sandboxService).delete(any(), any());
    }

    @Test(expected = NestedServletException.class)
    public void deleteSandboxByIdTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc
                .perform(delete("/sandbox/" + sandbox.getSandboxId()));
    }

    @Test
    public void updateSandboxByIdTest() throws Exception {
        String json = json(sandbox);
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
        verify(sandboxService).update(any(), any(), any());
    }

    @Test
    public void getSandboxesByMemberTest() throws Exception {
        List<Sandbox> sandboxes = new ArrayList<>();
        sandboxes.add(sandbox);
        String json = json(sandboxes);
        when(sandboxService.getAllowedSandboxes(user)).thenReturn(sandboxes);
        mvc
                .perform(get("/sandbox?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void getSandboxesByMemberTestUserNotFound() throws Exception {
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        mvc
                .perform(get("/sandbox?userId=" + user.getSbmUserId()));
    }

    @Test
    public void removeSandboxMemberTest() throws Exception {
        String json = json(sandbox);
        when(userService.findBySbmUserId(any())).thenReturn(user);
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "?removeUserId=" + user.getSbmUserId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void removeSandboxMemberTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        String json = json(sandbox);
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "?removeUserId=" + user.getSbmUserId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json));
    }

    @Test(expected = NestedServletException.class)
    public void removeSandboxMemberTestUserNotFound() throws Exception {
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        String json = json(sandbox);
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "?removeUserId=" + user.getSbmUserId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void removeSandboxMemberTestCanRemoveUser() throws Exception {
        String json = json(sandbox);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user2.getSbmUserId())).thenReturn(user2);
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "?removeUserId=" + user2.getSbmUserId())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void updateSandboxMemberRoleTest() throws Exception {
        String json = json(sandbox);
        sandbox.setCreatedBy(new User());
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "?editUserRole=" + user.getSbmUserId() + "&role=ADMIN&add=true")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
        verify(sandboxService).addMemberRole(any(), any(), any());
    }

    @Test
    public void updateSandboxMemberRoleTestAddIsFalse() throws Exception {
        String json = json(sandbox);
        sandbox.setCreatedBy(new User());
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "?editUserRole=" + user.getSbmUserId() + "&role=ADMIN&add=false")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json))
                .andExpect(status().isOk());
        verify(sandboxService).removeMemberRole(any(), any(), any());
    }

    @Test(expected = NestedServletException.class)
    public void updateSandboxMemberRoleTestSandboxNotFound() throws Exception {
        String json = json(sandbox);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "?editUserRole=" + user.getSbmUserId() + "&role=ADMIN&add=true")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json));
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void updateSandboxMemberRoleTestUnsupportedEncoding() throws Exception {
        String json = json(sandbox);
        sandbox.setCreatedBy(user);
        when(userService.findBySbmUserId("a")).thenReturn(new User());
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "?editUserRole=" + user.getSbmUserId() + "&role=ADMIN&add=true")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(json));
    }

    @Test
    public void changePayerForSandboxTest() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "/changePayer?newPayerId=" + user.getSbmUserId()))
                .andExpect(status().isOk());
        verify(sandboxService).changePayerForSandbox(any(), any());
    }

    @Test(expected = NestedServletException.class)
    public void changePayerForSandboxTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "/changePayer?newPayerId=" + user.getSbmUserId()));
    }

    @Test(expected = NestedServletException.class)
    public void changePayerForSandboxTestUserDenied() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn("a");
        when(userService.findBySbmUserId("a")).thenReturn(user);
        mvc
                .perform(put("/sandbox/" + sandbox.getSandboxId() + "/changePayer?newPayerId=" + "ab"));
    }

    @Test
    public void sandboxLoginTest() throws Exception {
        mvc
                .perform(post("/sandbox/" + sandbox.getSandboxId() + "/login?userId=" + user.getSbmUserId()))
                .andExpect(status().isOk());
        verify(userAccessHistoryService).saveUserAccessInstance(any(), any());
        verify(sandboxService).sandboxLogin(any(), any());
    }

    @Test(expected = NestedServletException.class)
    public void sandboxLoginTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc
                .perform(post("/sandbox/" + sandbox.getSandboxId() + "/login?userId=" + user.getSbmUserId()));
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
