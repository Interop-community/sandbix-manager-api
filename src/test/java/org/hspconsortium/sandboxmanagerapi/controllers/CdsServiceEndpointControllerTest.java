package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;

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
    private UserService userService;

    @MockBean
    private AuthorizationService authorizationService;

    private CdsServiceEndpoint cdsServiceEndpoint;
    private CdsHook cdsHook;
    private User user;
    private Sandbox sandbox;

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

        cdsServiceEndpoint.setSandbox(sandbox);
        cdsServiceEndpoint.setCreatedBy(user);
        Image image = new Image();
        image.setContentType("png");
        image.setBytes(hexStringToByteArray("e04fd020ea3a6910a2d808002b30309d"));
        cdsHook.setLogo(image);

    }
}
