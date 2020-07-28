package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SystemRole;
import org.hspconsortium.sandboxmanagerapi.model.User;
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
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AdminController.class)
@ContextConfiguration(classes = AdminController.class)
public class AdminControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private UserService userService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private SandboxInviteService sandboxInviteService;

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

    private User user;
    private Sandbox sandbox;

    @Before
    public void setup() {
        when(oAuthService.getOAuthUserId(any())).thenReturn("me");
        user = new User();
        user.setSbmUserId("me");
        Set<SystemRole> systemRoles = new HashSet<>();
        systemRoles.add(SystemRole.ADMIN);
        user.setSystemRoles(systemRoles);
        sandbox = new Sandbox();
        sandbox.setSandboxId("sandbox");
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void deleteSandboxByIdTest() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId())).thenReturn(new ArrayList<>());
        when(oAuthService.getBearerToken(any())).thenReturn("token");
        doNothing().when(sandboxService).delete(sandbox, "token", user, false);
        mvc.perform(delete("/admin/sandbox/" + sandbox.getSandboxId()))
           .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void deleteSandboxByIdTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc.perform(delete("/admin/sandbox/" + sandbox.getSandboxId()));
    }

    @Test
    public void deleteUnusedSandboxesTest() throws Exception {
        Set<String> sandboxDeletedId = new HashSet<>(Arrays.asList("1", "2", "3", "4", "5"));

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        doNothing().when(authorizationService).checkUserSystemRole(user, SystemRole.ADMIN);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(adminService.deleteUnusedSandboxes(user, "")).thenReturn(sandboxDeletedId);

        mvc.perform(delete("/admin/deleteUnused"))
           .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void deleteUnusedSandboxesNullUserTest() throws Exception {
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(adminService.deleteUnusedSandboxes(user, "")).thenReturn(null);

        mvc.perform(delete("/admin/deleteUnused"));
    }

    @Test(expected = NestedServletException.class)
    public void deleteUnusedSandboxesUserUnauthorizedTest() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        doThrow(UnauthorizedException.class).when(authorizationService).checkUserSystemRole(user, SystemRole.ADMIN);

        mvc.perform(delete("/admin/deleteUnused"));
    }

    @Test
    public void listSandboxManagerReferenceApiDiscrepenciesTest() throws Exception {
        HashMap<String, Object> referenceAPIDiscrepencies = new HashMap<>();
        referenceAPIDiscrepencies.put("A", 1.0);

        String json = json(referenceAPIDiscrepencies);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(adminService.syncSandboxManagerandReferenceApi(false, "")).thenReturn(referenceAPIDiscrepencies);

        mvc.perform(get("/admin/sandbox-differences/$list"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void listSandboxManagerReferenceApiDiscrepenciesUserUnauthorizedTest() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        doThrow(UnauthorizedException.class).when(authorizationService).checkUserSystemRole(user, SystemRole.ADMIN);

        mvc.perform(get("/admin/sandbox-differences/$list"));
    }

    @Test(expected = NestedServletException.class)
    public void listSandboxManagerReferenceApiDiscrepenciesNullUserTest() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(adminService.syncSandboxManagerandReferenceApi(false, "")).thenReturn(null);

        mvc.perform(get("/admin/sandbox-differences/$list"));
    }

    @Test
    public void syncSandboxManagerReferenceApiDiscrepenciesTest() throws Exception {
        HashMap<String, Object> referenceAPIDiscrepencies = new HashMap<>();
        referenceAPIDiscrepencies.put("A", 1.0);

        String json = json(referenceAPIDiscrepencies);

        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getBearerToken(any())).thenReturn("");
        when(adminService.syncSandboxManagerandReferenceApi(true, "")).thenReturn(referenceAPIDiscrepencies);

        mvc.perform(get("/admin/sandbox-differences/$sync"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(json));
    }

    @Test(expected = NestedServletException.class)
    public void syncSandboxManagerReferenceApiDiscrepenciesUserUnauthorizedTest() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        doThrow(UnauthorizedException.class).when(authorizationService).checkUserSystemRole(user, SystemRole.ADMIN);

        mvc.perform(get("/admin/sandbox-differences/$sync"));
    }

    @Test(expected = NestedServletException.class)
    public void syncSandboxManagerReferenceApiDiscrepenciesNullUserTest() throws Exception {
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        when(adminService.syncSandboxManagerandReferenceApi(false, "")).thenReturn(null);

        mvc.perform(get("/admin/sandbox-differences/$sync"));
    }

    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}
