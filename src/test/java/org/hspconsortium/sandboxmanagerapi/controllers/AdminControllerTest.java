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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AdminController.class, secure = false)
@ContextConfiguration(classes = AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OAuthService oAuthService;

    @MockBean
    private UserService userService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private SandboxInviteService sandboxInviteService;

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
    }

    @Test
    public void deleteSandboxByIdTest() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId())).thenReturn(new ArrayList<>());
        when(oAuthService.getBearerToken(any())).thenReturn("token");
        doNothing().when(sandboxService).delete(sandbox, "token", user, false);
        mvc
                .perform(delete("/admin/sandbox/" + sandbox.getSandboxId()))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void deleteSandboxByIdTestSandboxNotFound() throws Exception {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        mvc
                .perform(delete("/admin/sandbox/" + sandbox.getSandboxId()));
    }

}
