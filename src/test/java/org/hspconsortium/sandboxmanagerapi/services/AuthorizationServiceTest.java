package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.controllers.UnauthorizedException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.impl.AuthorizationServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

public class AuthorizationServiceTest {

    private OAuthService oAuthService = mock(OAuthService.class);

    MockHttpServletRequest request = new MockHttpServletRequest();

    private AuthorizationServiceImpl authorizationService = new AuthorizationServiceImpl();

    private User user;
    private Sandbox sandbox;
    private AbstractSandboxItem abstractSandboxItem;
    private UserRole userRole;
    private List<UserRole> userRoles;
    private Set<SystemRole> systemRoles;

    @Before
    public void setup() {
        authorizationService.setoAuthService(oAuthService);
        user = new User();
        user.setSbmUserId("userId");
        user.setEmail("email");
        user.setName("name");
        sandbox = new Sandbox();
        sandbox.setSandboxId("sandbox");
        userRole = new UserRole();
        userRole.setUser(user);
        userRoles = new ArrayList<>();
        systemRoles = new HashSet<>();

        abstractSandboxItem = new AbstractSandboxItem() {
            @Override
            public Sandbox getSandbox() {
                return sandbox;
            }

            @Override
            public void setSandbox(Sandbox sandbox) {
                this.sandbox = sandbox;
            }

            @Override
            public Integer getId() {
                return id;
            }

            @Override
            public void setId(Integer id) {
                this.id = id;
            }

            @Override
            public User getCreatedBy() {
                return createdBy;
            }

            @Override
            public void setCreatedBy(User createdBy) {
                this.createdBy = createdBy;
            }

            @Override
            public Timestamp getCreatedTimestamp() {
                return createdTimestamp;
            }

            @Override
            public void setCreatedTimestamp(Timestamp createdTimestamp) {
                this.createdTimestamp = createdTimestamp;
            }

            @Override
            public Visibility getVisibility() {
                return visibility;
            }

            @Override
            public void setVisibility(Visibility visibility) {
                this.visibility = visibility;
            }
        };
    }

    @Test
    public void checkUserAuthorizationTest() {
        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
        authorizationService.checkUserAuthorization(request, user.getSbmUserId());
    }

//    @Test(expected = UnauthorizedException.class)
//    public void checkUserAuthorizationUserUnauthorizedTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        authorizationService.checkUserAuthorization(request, "A");
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkUserAuthorizationTestNotAuthorized() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn("other-userId");
//        authorizationService.checkUserAuthorization(request, user.getSbmUserId());
//    }
//
//    @Test
//    public void getSystemUserIdTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        String userId = authorizationService.getSystemUserId(request);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test
//    public void getUserNameTest() {
//        when(oAuthService.getOAuthUserName(request)).thenReturn(user.getName());
//        String name = authorizationService.getUserName(request);
//        assertEquals(user.getName(), name);
//    }
//
//    @Test
//    public void getEmailTest() {
//        when(oAuthService.getOAuthUserEmail(request)).thenReturn(user.getEmail());
//        String email = authorizationService.getEmail(request);
//        assertEquals(user.getEmail(), email);
//    }
//
//    @Test
//    public void getBearerTokenTest() {
//        when(oAuthService.getBearerToken(request)).thenReturn("token");
//        String token = authorizationService.getBearerToken(request);
//        assertEquals("token", token);
//    }
//
//    @Test
//    public void checkSandboxUserReadAuthorizationTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        String userId = authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSandboxUserReadAuthorizationNotAuthorizedTest() {
//        sandbox.setUserRoles(new ArrayList<>());
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
//    }
//
//    @Test
//    public void checkSandboxUserModifyAuthorizationUserAuthorizedTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        abstractSandboxItem.setCreatedBy(user);
//        abstractSandboxItem.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        String userId = authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, abstractSandboxItem);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSandboxUserModifyAuthorizationUserUnauthorizedTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        abstractSandboxItem.setCreatedBy(user);
//        abstractSandboxItem.setVisibility(Visibility.PUBLIC);
//        userRole.setRole(Role.MANAGE_USERS);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        sandbox.setVisibility(Visibility.PUBLIC);
//        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, abstractSandboxItem);
//    }
//
//    @Test
//    public void checkSandboxUserModifyAuthorizationASIVisibilityPublicSNDVisibilityPrivateTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        abstractSandboxItem.setCreatedBy(user);
//        abstractSandboxItem.setVisibility(Visibility.PUBLIC);
//        sandbox.setVisibility(Visibility.PRIVATE);
//        String userId = authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, abstractSandboxItem);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSandboxUserModifyAuthorizationASIVisibilityPublicSNDVisibilityPublicTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        abstractSandboxItem.setCreatedBy(user);
//        abstractSandboxItem.setVisibility(Visibility.PUBLIC);
//        sandbox.setVisibility(Visibility.PUBLIC);
//        userRole.setRole(Role.MANAGE_USERS);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, abstractSandboxItem);
//    }
//
//    @Test
//    public void checkSandboxUserModifyAuthorizationASIVisibilityPublicSNDVisibilityPublicRoleAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        abstractSandboxItem.setCreatedBy(user);
//        abstractSandboxItem.setVisibility(Visibility.PUBLIC);
//        sandbox.setVisibility(Visibility.PUBLIC);
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        String userId =  authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, abstractSandboxItem);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSandboxUserModifyAuthorizationASIVisibilityPublicSNDVisibilityPublicRoleNotAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        abstractSandboxItem.setCreatedBy(user);
//        abstractSandboxItem.setVisibility(Visibility.PUBLIC);
//        sandbox.setVisibility(Visibility.PUBLIC);
//        userRole.setRole(Role.USER);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, abstractSandboxItem);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSandboxUserModifyAuthorizationASIVisibilityPublicSNDVisibilityPublicRoleManageUsersTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        abstractSandboxItem.setCreatedBy(user);
//        abstractSandboxItem.setVisibility(Visibility.PUBLIC);
//        sandbox.setVisibility(Visibility.PUBLIC);
//        userRole.setRole(Role.MANAGE_USERS);
//        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, abstractSandboxItem);
//    }
//
//    @Test
//    public void checkSystemUserDeleteSandboxAuthorizationTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        sandbox.setVisibility(Visibility.PRIVATE);
//        String userId = authorizationService.checkSystemUserDeleteSandboxAuthorization(request,sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserDeleteSandboxAuthorizationUserUnauthorizedTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.USER);
//        authorizationService.checkSystemUserDeleteSandboxAuthorization(request,sandbox, user);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserDeleteSandboxAuthorizationSanboxPublicTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.USER);
//        sandbox.setVisibility(Visibility.PUBLIC);
//        authorizationService.checkSystemUserDeleteSandboxAuthorization(request,sandbox, user);
//    }
//
//    @Test
//    public void checkSystemUserDeleteSandboxAuthorizationSanboxPublicSystemRoleAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PUBLIC);
//        systemRoles.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoles);
//        String userId = authorizationService.checkSystemUserDeleteSandboxAuthorization(request,sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test
//    public void checkSystemUserDeleteSandboxAuthorizationSandboxPrivateUserAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        sandbox.setVisibility(Visibility.PRIVATE);
//        String userId = authorizationService.checkSystemUserDeleteSandboxAuthorization(request,sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserDeleteSandboxAuthorizationSandboxPrivateUserNotAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.USER);
//        sandbox.setVisibility(Visibility.PRIVATE);
//        authorizationService.checkSystemUserDeleteSandboxAuthorization(request,sandbox, user);
//    }
//
//    @Test
//    public void checkSystemUserCanModifySandboxAuthorizationTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        sandbox.setVisibility(Visibility.PRIVATE);
//        String userId = authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserCanModifySandboxAuthorizationUserUnathorizedTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.USER);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
//    }
//
//    @Test
//    public void checkSystemUserCanModifySandboxAuthorizationSNDVisPrivateUserAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        String userId = authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserCanModifySandboxAuthorizationSNDVisPrivateUserNotAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.USER);
//        authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserCanModifySandboxAuthorizationSNDVisPublicUserAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PUBLIC);
//        userRole.setRole(Role.ADMIN);
//        authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
//    }
//
//    @Test
//    public void checkSystemUserCanModifySandboxAuthorizationSNDVisPublicSystemRoleAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PUBLIC);
//        systemRoles.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoles);
//        String userId = authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test
//    public void checkSystemUserCanManageSandboxDataAuthorizationSNDVisPrivateAdminTest(){
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        String userId = authorizationService.checkSystemUserCanManageSandboxDataAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test
//    public void checkSystemUserCanManageSandboxDataAuthorizationSNDVisPrivateDataManageTest(){
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.MANAGE_DATA);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        String userId = authorizationService.checkSystemUserCanManageSandboxDataAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test
//    public void checkSystemUserCanManageSandboxDataAuthorizationSNDVisPublicSysAdminTest(){
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PUBLIC);
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        systemRoles.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoles);
//        sandbox.setUserRoles(userRoles);
//        String userId = authorizationService.checkSystemUserCanManageSandboxDataAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserCanManageSandboxDataAuthorizationSNDVisPublicNotSysAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PUBLIC);
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        systemRoles.add(SystemRole.USER);
//        user.setSystemRoles(systemRoles);
//        sandbox.setUserRoles(userRoles);
//        authorizationService.checkSystemUserCanManageSandboxDataAuthorization(request, sandbox, user);
//    }
//
//    @Test
//    public void checkSystemUserCanManageSandboxUsersAuthorizationSNDVisPrivateAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        systemRoles.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoles);
//        sandbox.setUserRoles(userRoles);
//        String userId = authorizationService.checkSystemUserCanManageSandboxUsersAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test
//    public void checkSystemUserCanManageSandboxUsersAuthorizationSNDVisPrivateDataManagerTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.MANAGE_DATA);
//        userRoles.add(userRole);
//        systemRoles.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoles);
//        sandbox.setUserRoles(userRoles);
//        String userId = authorizationService.checkSystemUserCanManageSandboxUsersAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserCanManageSandboxUsersAuthorizationSNDVisPrivateUserNotAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.USER);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        authorizationService.checkSystemUserCanManageSandboxUsersAuthorization(request, sandbox, user);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserCanManageSandboxUsersAuthorizationSNDVisPublicUserAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PUBLIC);
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        authorizationService.checkSystemUserCanManageSandboxUsersAuthorization(request, sandbox, user);
//    }
//
//    @Test
//    public void checkSystemUserCanManageSandboxUsersAuthorizationSNDVisPublicManageUserTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.MANAGE_USERS);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        String userId = authorizationService.checkSystemUserCanManageSandboxUsersAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test
//    public void checkSystemUserCanManageSandboxUsersAuthorizationSNDVisPublicSystemRoleAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        sandbox.setVisibility(Visibility.PUBLIC);
//        systemRoles.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoles);
//        String userId = authorizationService.checkSystemUserCanManageSandboxUsersAuthorization(request, sandbox, user);
//        assertEquals(user.getSbmUserId(), userId);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkUserSystemRoleNotAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        authorizationService.checkUserSystemRole(user,SystemRole.USER);
//    }
//
//    @Test
//    public void checkUserSystemRoleAdminUserAdminTest() {
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        systemRoles.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoles);
//        sandbox.setUserRoles(userRoles);
//        authorizationService.checkUserSystemRole(user,SystemRole.ADMIN);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSystemUserCanMakeTransactionTest() {
//        authorizationService.checkSystemUserCanMakeTransaction(sandbox, user);
//    }
//
//    @Test
//    public void checkSystemUserCanMakeTransactionUserAuthorizedTest() {
//        List<Sandbox> sandboxes = new ArrayList<>();
//        sandboxes.add(sandbox);
//        user.setSandboxes(sandboxes);
//        authorizationService.checkSystemUserCanMakeTransaction(sandbox, user);
//    }
//
//    @Test
//    public void checkIfPersonaAndHasAuthorityTest() {
//        UserPersona userPersona = new UserPersona();
//        userPersona.setSandbox(sandbox);
//        authorizationService.checkIfPersonaAndHasAuthority(sandbox, userPersona);
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkIfPersonaAndHasAuthorityPersonaNotContainSNDTest() {
//        UserPersona userPersona = new UserPersona();
//        Sandbox snd = new Sandbox();
//        userPersona.setSandbox(snd);
//        authorizationService.checkIfPersonaAndHasAuthority(sandbox, userPersona);
//    }
//
//    @Test
//    public void getDefaultVisibilityAdminTest() {
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        user.setSystemRoles(systemRoles);
//        Visibility vis = authorizationService.getDefaultVisibility(user, sandbox);
//        assertEquals(Visibility.PUBLIC, vis);
//    }
//
//    @Test
//    public void getDefaultVisibilityReadOnlyTest() {
//        sandbox.setVisibility(Visibility.PRIVATE);
//        userRole.setRole(Role.READONLY);
//        userRoles.add(userRole);
//        user.setSystemRoles(systemRoles);
//        Visibility vis = authorizationService.getDefaultVisibility(user, sandbox);
//        assertEquals(Visibility.PUBLIC, vis);
//    }
//
//    @Test
//    public void getDefaultVisibilityUserUnAuthorizedTest() {
//        sandbox.setVisibility(Visibility.PUBLIC);
//        Visibility vis = authorizationService.getDefaultVisibility(user, sandbox);
//        assertEquals(Visibility.PRIVATE, vis);
//    }
//
//    @Test
//    public void checkUserHasSystemRoleTest() {
//        systemRoles.add(SystemRole.CREATE_SANDBOX);
//        systemRoles.add(SystemRole.ADMIN);
//        systemRoles.add(SystemRole.USER);
//        user.setSystemRoles(systemRoles);
//        assertTrue(authorizationService.checkUserHasSystemRole(user, SystemRole.USER));
//    }
//
//    @Test
//    public void checkUserHasSystemRoleNotTest() {
//        systemRoles.add(SystemRole.CREATE_SANDBOX);
//        systemRoles.add(SystemRole.ADMIN);
//        user.setSystemRoles(systemRoles);
//        assertFalse(authorizationService.checkUserHasSystemRole(user, SystemRole.USER));
//    }
//
//    @Test
//    public void checkSandboxUserNotReadOnlyAuthorizationTest() {
//        userRole.setRole(Role.ADMIN);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        String authorization = authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
//        assertEquals(authorization, user.getSbmUserId());
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void checkSandboxUserNotReadOnlyAuthorizationRoleReadOnlyTest() {
//        userRole.setRole(Role.READONLY);
//        userRoles.add(userRole);
//        sandbox.setUserRoles(userRoles);
//        when(oAuthService.getOAuthUserId(request)).thenReturn(user.getSbmUserId());
//        authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
//    }
}