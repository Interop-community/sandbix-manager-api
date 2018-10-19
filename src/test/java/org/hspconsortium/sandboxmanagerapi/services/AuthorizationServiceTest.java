package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.controllers.UnauthorizedException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.impl.AuthorizationServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

public class AuthorizationServiceTest {

    private OAuthService oAuthService = mock(OAuthService.class);

    MockHttpServletRequest request = new MockHttpServletRequest();

    private AuthorizationServiceImpl authorizationService = new AuthorizationServiceImpl();

    private User user;
    private Sandbox sandbox;
    private AbstractSandboxItem abstractSandboxItem = mock(AbstractSandboxItem.class);

    @Before
    public void setup() {
        authorizationService.setoAuthService(oAuthService);
        user = new User();
        user.setSbmUserId("userId");
        user.setEmail("email");
        user.setName("name");
        sandbox = new Sandbox();
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole);
        sandbox.setUserRoles(userRoles);
    }

    @Test
    public void checkUserAuthorizationTest() {
        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
        authorizationService.checkUserAuthorization(request, user.getSbmUserId());
    }

    @Test(expected = UnauthorizedException.class)
    public void checkUserAuthorizationTestNotAuthorized() {
        when(oAuthService.getOAuthUserId(request)).thenReturn("other-userId");
        authorizationService.checkUserAuthorization(request, user.getSbmUserId());
    }

    @Test
    public void getSystemUserIdTest() {
        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
        String userId = authorizationService.getSystemUserId(request);
        assertEquals(user.getSbmUserId(), userId);
    }

    @Test
    public void getUserNameTest() {
        when(oAuthService.getOAuthUserName(request)).thenReturn(user.getName());
        String name = authorizationService.getUserName(request);
        assertEquals(user.getName(), name);
    }

    @Test
    public void getEmailTest() {
        when(oAuthService.getOAuthUserEmail(request)).thenReturn(user.getEmail());
        String email = authorizationService.getEmail(request);
        assertEquals(user.getEmail(), email);
    }

    @Test
    public void getBearerTokenTest() {
        when(oAuthService.getBearerToken(request)).thenReturn("token");
        String token = authorizationService.getBearerToken(request);
        assertEquals("token", token);
    }

    @Test
    public void checkSandboxUserReadAuthorizationTest() {
        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
        String userId = authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        assertEquals(user.getSbmUserId(), userId);
    }

    @Test(expected = UnauthorizedException.class)
    public void checkSandboxUserReadAuthorizationTestNotAuthorized() {
        sandbox.setUserRoles(new ArrayList<>());
        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
    }

//    @Test
//    public void checkSandboxUserModifyAuthorizationTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        abstractSandboxItem.setVisibility(Visibility.PRIVATE);
//        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, abstractSandboxItem);
//    }
}
