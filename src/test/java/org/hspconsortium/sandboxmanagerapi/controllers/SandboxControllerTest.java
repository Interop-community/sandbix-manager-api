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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private HashMap<String, Sandbox> sandboxHashMap;

    @Before
    public void setup() {
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
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
        sandbox.setCreatedBy(user);
        Set<SystemRole> systemRoles = new HashSet<>();
        systemRoles.add(SystemRole.ADMIN);
        systemRoles.add(SystemRole.CREATE_SANDBOX);
        user.setSystemRoles(systemRoles);
        sandboxHashMap = new HashMap<>();
        sandboxHashMap.put("clonedSandbox", sandbox);
        sandboxHashMap.put("newSandbox", sandbox);
        when(userService.findBySbmUserId(sandbox.getCreatedBy().getSbmUserId())).thenReturn(user);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
    }

    @Test
    public void createSandboxTest() throws Exception {
        String json = json(sandbox);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        when(sandboxService.create(any(), any(), any())).thenReturn(sandbox);
        mvc
                .perform(
                        post("/sandbox")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void createSandboxTestDuplicateSandboxId() throws Exception {
        String json = json(sandbox);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        mvc
                .perform(
                        post("/sandbox")
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .content(json));
    }

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

    @Test
    public void checkForSandboxByIdTest() throws Exception {
        String response = "{\"sandboxId\": \"" + sandbox.getSandboxId() +"\"}";
        mvc
                .perform(get("/sandbox?lookUpId=" + sandbox.getSandboxId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string(response));
    }

    @Test
    public void checkForSandboxByIdTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc
                .perform(get("/sandbox?lookUpId=" + sandbox.getSandboxId()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void getSandboxByIdTest() throws Exception {

    }

    @Test
    public void deleteSandboxByIdTest() throws Exception {

    }

    @Test
    public void updateSandboxByIdTest() throws Exception {

    }

    @Test
    public void getSandboxesByMemberTest() throws Exception {

    }

    @Test
    public void removeSandboxMemberTest() throws Exception {

    }

    @Test
    public void updateSandboxMemberRoleTest() throws Exception {

    }

    @Test
    public void changePayerForSandboxTest() throws Exception {

    }

    @Test
    public void sandboxLoginTest() throws Exception {

    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
