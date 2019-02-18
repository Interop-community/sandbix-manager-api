package org.hspconsortium.sandboxmanagerapi.services;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.SandboxServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SandboxServiceTest {

    private SandboxRepository repository = mock(SandboxRepository.class);
    private UserService userService = mock(UserService.class);
    private UserRoleService userRoleService = mock(UserRoleService.class);
    private UserPersonaService userPersonaService = mock(UserPersonaService.class);
    private UserLaunchService userLaunchService = mock(UserLaunchService.class);
    private SandboxInviteService sandboxInviteService = mock(SandboxInviteService.class);
    private AppService appService = mock(AppService.class);
    private LaunchScenarioService launchScenarioService = mock(LaunchScenarioService.class);
    private SandboxImportService sandboxImportService = mock(SandboxImportService.class);
    private SandboxActivityLogService sandboxActivityLogService = mock(SandboxActivityLogService.class);
    private RuleService ruleService = mock(RuleService.class);
    private UserAccessHistoryService userAccessHistoryService = mock(UserAccessHistoryService.class);
    private CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    private CloseableHttpResponse response = spy(CloseableHttpResponse.class);

    private SandboxServiceImpl sandboxService = new SandboxServiceImpl(repository);

    private Sandbox sandbox = spy(Sandbox.class);
    private Sandbox newSandbox = spy(Sandbox.class);
    private Sandbox snd = spy(Sandbox.class);
    private Sandbox newSandboxApiNotSeven = spy(Sandbox.class);

    private User user;
    private User user2;
    private User userEmptyRoles;
    private String bearerToken = "token";
    private SandboxImport sandboxImport;
    private StatusLine statusLine;
    private LaunchScenario launchScenario;
    private UserPersona userPersona;
    private SandboxInvite sandboxInvite;
    private App app;
    private UserLaunch userLaunch;
    private UserRole userRole;
    private UserRole userRole2;
    private List<LaunchScenario> launchScenarios;
    private List<UserPersona> userPersonas;
    private List<SandboxInvite> sandboxInvites;
    private List<App> apps;
    private List<UserLaunch> userLaunches;
    private List<UserRole> userRoles;
    private List<UserRole> userRoles2;
    private List<Sandbox> sandboxes;
    private Role role = Role.ADMIN;
    private String token = "token";

    @Before
    public void setup() {
        sandboxService.setUserAccessHistoryService(userAccessHistoryService);
        sandboxService.setUserLaunchService(userLaunchService);
        sandboxService.setUserPersonaService(userPersonaService);
        sandboxService.setUserRoleService(userRoleService);
        sandboxService.setSandboxInviteService(sandboxInviteService);
        sandboxService.setUserService(userService);
        sandboxService.setAppService(appService);
        sandboxService.setLaunchScenarioService(launchScenarioService);
        sandboxService.setSandboxImportService(sandboxImportService);
        sandboxService.setSandboxActivityLogService(sandboxActivityLogService);
        sandboxService.setRuleService(ruleService);
        sandboxService.setHttpClient(httpClient);

        sandbox.setId(1);
        sandbox.setSandboxId("sandboxId");
        sandbox.setApiEndpointIndex("6");
        sandbox.setVisibility(Visibility.PUBLIC);
        newSandbox.setSandboxId("new-sandbox");
        newSandbox.setApiEndpointIndex("7");
        sandboxes = new ArrayList<>();
        sandboxes.add(sandbox);
        snd = new Sandbox();

        user = new User();
        user.setId(1);
        user.setSbmUserId("userId");
        user.setSandboxes(sandboxes);
        user2 = new User();
        userEmptyRoles = new User();

        sandbox.setCreatedBy(user);
        List<SandboxImport> sandboxImportList = new ArrayList<>();
        sandboxImport = new SandboxImport();
        sandboxImportList.add(sandboxImport);
        sandbox.setImports(sandboxImportList);
        launchScenarios = new ArrayList<>();
        launchScenario = new LaunchScenario();
        launchScenario.setId(1);
        launchScenario.setVisibility(Visibility.PRIVATE);
        launchScenario.setSandbox(sandbox);
        launchScenarios.add(launchScenario);

        apps = new ArrayList<>();
        app = new App();
        app.setId(1);
        app.setVisibility(Visibility.PRIVATE);
        app.setClientId("clientId");
        app.setLaunchUri("launchUri");
        launchScenario.setApp(app);
        apps.add(app);
        userPersonas = new ArrayList<>();
        userPersona = new UserPersona();
        userPersona.setId(1);
        userPersona.setVisibility(Visibility.PRIVATE);
        userPersona.setPersonaUserId("user@sandbox");
        launchScenario.setUserPersona(userPersona);
        userPersonas.add(userPersona);
        sandboxInvites = new ArrayList<>();
        sandboxInvite = new SandboxInvite();
        sandboxInvite.setSandbox(sandbox);
        sandboxInvites.add(sandboxInvite);
        userLaunch = new UserLaunch();
        userLaunch.setId(1);
        userLaunch.setLaunchScenario(launchScenario);
        userLaunches = new ArrayList<>();
        userLaunches.add(userLaunch);

        userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(Role.ADMIN);
        userRole.setId(1);

        userRoles = new ArrayList<>();
        userRoles.add(userRole);
        sandbox.setUserRoles(userRoles);

        userRole2 = new UserRole();
        userRole2.setUser(user);
        userRole2.setRole(Role.READONLY);
        userRole2.setId(1);

        userRoles2 = new ArrayList<>();
        userRoles2.add(userRole);
        userRoles2.add(userRole2);
        newSandboxApiNotSeven.setUserRoles(userRoles2);

        statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!");

        String[] defaultPublicSandboxRoles = new String[]{"USER"};
        String[] defaultSandboxCreatorRoles = new String[]{"USER"};
        String[] defaultPrivateSandboxRoles = new String[]{"ADMIN"};

        ReflectionTestUtils.setField(sandboxService, "defaultSandboxVisibility", "PRIVATE");
        ReflectionTestUtils.setField(sandboxService, "expirationDate", "2018-09-01");
        ReflectionTestUtils.setField(sandboxService, "defaultSandboxCreatorRoles", defaultSandboxCreatorRoles);
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_1", "apiBaseURL_1");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_2", "apiBaseURL_2");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_3", "apiBaseURL_3");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_4", "apiBaseURL_4");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_5", "apiBaseURL_5");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_6", "apiBaseURL_6");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_7", "apiBaseURL_7");
        ReflectionTestUtils.setField(sandboxService, "defaultPublicSandboxRoles", defaultPublicSandboxRoles);
        ReflectionTestUtils.setField(sandboxService, "defaultPrivateSandboxRoles", defaultPrivateSandboxRoles);

        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(repository.save(newSandbox)).thenReturn(newSandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
    }

    @Test
    public void deleteTest() {
        sandboxService.delete(sandbox.getId());
        verify(repository).delete(sandbox.getId());
    }

    @Test
    public void deleteTestAll() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.delete(sandbox, bearerToken, user, false);
        verify(sandboxImportService).delete(sandboxImport);
        verify(sandboxActivityLogService).sandboxDelete(sandbox, user);
        verify(sandboxInviteService).findInvitesBySandboxId(sandbox.getSandboxId());
    }

    @Test
    public void deleteTestAllAdminIsNull() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.delete(sandbox, bearerToken, null, false);
        verify(sandboxActivityLogService).sandboxDelete(sandbox, sandbox.getCreatedBy());
    }

    @Test
    public void deleteSyncTrueTest() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.delete(sandbox, bearerToken, null, true);
        verify(sandboxActivityLogService).sandboxDelete(sandbox, sandbox.getCreatedBy());
    }

    @Test
    public void deleteSandboxAndBearerTokenOnlyTest() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.delete(sandbox, bearerToken);
        verify(sandboxActivityLogService).sandboxDelete(sandbox, sandbox.getCreatedBy());
    }

    @Test
    public void deleteTestAllVerifyItemsDeleted() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(launchScenarioService.findBySandboxId(sandbox.getSandboxId())).thenReturn(launchScenarios);
        when(userPersonaService.findBySandboxId(sandbox.getSandboxId())).thenReturn(userPersonas);
        when(sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId())).thenReturn(sandboxInvites);
        when(appService.findBySandboxIdIncludingCustomApps(sandbox.getSandboxId())).thenReturn(apps);
        sandboxService.delete(sandbox, bearerToken, user, false);
        verify(launchScenarioService).delete(launchScenario);
        verify(userPersonaService).delete(userPersona);
        verify(sandboxInviteService).delete(sandboxInvite);
        verify(appService).delete(app);
        verify(userAccessHistoryService).deleteUserAccessInstancesForSandbox(sandbox);
    }

    @Test(expected = RuntimeException.class)
    public void deleteTestErrorInApiCall() throws IOException {
        when(launchScenarioService.findBySandboxId(sandbox.getSandboxId())).thenReturn(launchScenarios);
        when(userPersonaService.findBySandboxId(sandbox.getSandboxId())).thenReturn(userPersonas);
        when(httpClient.execute(any())).thenReturn(response);
        statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "NOT FINE!");
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(mock(HttpEntity.class));
        sandboxService.delete(sandbox, bearerToken, user, false);
    }

    @Test(expected = RuntimeException.class)
    public void deleteTestThrownExeptionInApiCall() throws IOException {
        when(launchScenarioService.findBySandboxId(sandbox.getSandboxId())).thenReturn(launchScenarios);
        when(userPersonaService.findBySandboxId(sandbox.getSandboxId())).thenReturn(userPersonas);
        when(httpClient.execute(any())).thenThrow(IOException.class);
        sandboxService.delete(sandbox, bearerToken, user, false);
    }

    @Test
    public void cloneTest() throws IOException {
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(null);
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
        verify(newSandbox).setCreatedBy(user);
        verify(newSandbox).setCreatedTimestamp(any());
        verify(newSandbox).setVisibility(any());
        verify(newSandbox).setExpirationMessage(any());
        verify(newSandbox).setExpirationDate(any());
        verify(newSandbox).setPayerUserId(any());
        verify(sandboxActivityLogService).sandboxCreate(newSandbox, user);
    }

    @Test
    public void cloneEndpointNotEqualSevenTest() throws IOException {
        newSandboxApiNotSeven.setApiEndpointIndex("4");
        when(repository.save(newSandboxApiNotSeven)).thenReturn(newSandboxApiNotSeven);
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(null);
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.clone(newSandboxApiNotSeven, sandbox.getSandboxId(), user, bearerToken);
        verify(newSandboxApiNotSeven).setCreatedBy(user);
        verify(newSandboxApiNotSeven).setCreatedTimestamp(any());
        verify(sandboxActivityLogService).sandboxCreate(newSandboxApiNotSeven, user);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void cloneTestExistingSandboxDoesntExist() throws IOException {
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cloneTestNewSandboxAlreadyExists() throws IOException {
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(repository.findBySandboxId(newSandbox.getSandboxId())).thenReturn(newSandbox);
        sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
    }

    @Test
    public void cloneTestCloneUserPersonas() throws IOException {
        newSandbox.setDataSet(DataSet.DEFAULT);
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(null);
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(userPersonaService.findBySandboxId(sandbox.getSandboxId())).thenReturn(userPersonas);
        sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
        verify(userPersonaService).save(any());
    }

    @Test
    public void cloneTestCloneAppsAndLaunchScenarios() throws IOException {
        newSandbox.setApps(DataSet.DEFAULT);
        newSandbox.setDataSet(DataSet.DEFAULT);
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(null);
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(apps);
        when(appService.findByLaunchUriAndClientIdAndSandboxId(any(), any(), any())).thenReturn(app);
        List<ContextParams> contextParams = new ArrayList<>();
        contextParams.add(new ContextParams());
        launchScenario.setContextParams(contextParams);
        when(launchScenarioService.findBySandboxId(sandbox.getSandboxId())).thenReturn(launchScenarios);
        sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
        verify(launchScenarioService).save(any());
        verify(appService).save(any());
    }

    @Test
    public void cloneTestCloneAppsOnly() throws IOException {
        newSandbox.setApps(DataSet.DEFAULT);
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(null);
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(apps);
        when(launchScenarioService.findBySandboxId(sandbox.getSandboxId())).thenReturn(launchScenarios);
        sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
        verify(launchScenarioService, times(0)).save(any());
        verify(appService).save(any());
    }

    @Test
    public void cloneTestInitialPersonaNotNull() throws IOException {
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(userPersona);
        assertEquals(null, sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken));
    }

    @Test(expected = NullPointerException.class)
    public void cloneTestInitialPersonaNull() throws IOException {
        ReflectionTestUtils.setField(sandboxService, "expirationDate", "afdfd");
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(null);
        Sandbox returnedSandbox = sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
    }

    @Test
    public void cloneTestCantClone() throws IOException {
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(false);
        assertEquals(null, sandboxService.clone(sandbox, sandbox.getSandboxId(), user, bearerToken));
    }

    @Test(expected = RuntimeException.class)
    public void cloneTestErrorInCallToReferenceApi() throws IOException {
        statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "NOT FINE!");
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(null);
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(mock(HttpEntity.class));
        sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
    }

    @Test(expected = RuntimeException.class)
    public void cloneTestThrowsExceptionInApiCall() throws IOException {
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(true);
        when(userPersonaService.findByPersonaUserId(user.getSbmUserId())).thenReturn(null);
        when(httpClient.execute(any())).thenThrow(IOException.class);
        sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
    }

    @Test
    public void updateTest() throws IOException {
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.update(sandbox, user, bearerToken);
        verify(sandbox).setName(anyString());
        verify(sandbox).setDescription(anyString());
        verify(sandboxActivityLogService, times(0)).sandboxOpenEndpoint(any(), any(), any());
    }

    @Test
    public void updateTestIsOpenAccess() throws IOException {
        Sandbox otherSandbox = new Sandbox();
        otherSandbox.setAllowOpenAccess(true);
        otherSandbox.setId(2);
        otherSandbox.setSandboxId("otherSandboxId");
        otherSandbox.setApiEndpointIndex("6");
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(otherSandbox);
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.update(sandbox, user, bearerToken);
        verify(sandboxActivityLogService).sandboxOpenEndpoint(any(), any(), any());
    }


    @Test
    public void updateDataSetNATest() throws IOException {
        Sandbox otherSandbox = new Sandbox();
        otherSandbox.setAllowOpenAccess(true);
        otherSandbox.setId(2);
        otherSandbox.setSandboxId("otherSandboxId");
        otherSandbox.setApiEndpointIndex("6");
        otherSandbox.setDataSet(DataSet.NONE);
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(otherSandbox);
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.update(sandbox, user, bearerToken);
        verify(sandboxActivityLogService).sandboxOpenEndpoint(any(), any(), any());
    }

    @Test(expected = RuntimeException.class)
    public void updateTestNot200CodeReturned() throws IOException {
        Sandbox otherSandbox = new Sandbox();
        otherSandbox.setAllowOpenAccess(true);
        otherSandbox.setId(2);
        otherSandbox.setSandboxId("otherSandboxId");
        otherSandbox.setApiEndpointIndex("6");
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(otherSandbox);
        when(httpClient.execute(any())).thenReturn(response);
        statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "NOT FINE!");
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(mock(HttpEntity.class));
        sandboxService.update(sandbox, user, bearerToken);
    }

    @Test(expected = RuntimeException.class)
    public void updateTestExceptionThrownWithExecute() throws IOException {
        Sandbox otherSandbox = new Sandbox();
        otherSandbox.setAllowOpenAccess(true);
        otherSandbox.setId(2);
        otherSandbox.setSandboxId("otherSandboxId");
        otherSandbox.setApiEndpointIndex("6");
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(otherSandbox);
        when(httpClient.execute(any())).thenThrow(IOException.class);
        sandboxService.update(sandbox, user, bearerToken);
    }

    @Test
    public void removeMemberTest() {
        when(launchScenarioService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId())).thenReturn(launchScenarios);
        when(userPersonaService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId())).thenReturn(userPersonas);
        when(appService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId())).thenReturn(apps);
        when(userLaunchService.findByUserId(user.getSbmUserId())).thenReturn(userLaunches);

        sandboxService.removeMember(sandbox, user, bearerToken);
        verify(userService).removeSandbox(sandbox, user);
        verify(userLaunchService).delete(userLaunch);
        verify(launchScenarioService).delete(launchScenario);
        verify(appService).delete(app);
        verify(userPersonaService).delete(userPersona);
        verify(sandboxActivityLogService).sandboxUserRemoved(sandbox, sandbox.getCreatedBy(), user);
        verify(userRoleService).delete(userRole);
    }

    @Test
    public void removeMemberUserNullTest() {
        sandboxService.removeMember(sandbox, null, bearerToken);
        verify(userService, times(0)).removeSandbox(sandbox, user);
    }

    @Test
    public void removeMemberUserHasRoleTest() {
        when(launchScenarioService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId())).thenReturn(launchScenarios);
        when(userPersonaService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId())).thenReturn(userPersonas);
        when(appService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId())).thenReturn(apps);
        when(userLaunchService.findByUserId(user.getSbmUserId())).thenReturn(userLaunches);

        sandboxService.removeMember(sandbox, user2, bearerToken);
    }



    @Test
    public void addMemberTest() {
        sandboxService.addMember(sandbox, user, role);
        verify(sandboxActivityLogService).sandboxUserRoleChange(sandbox, user,  role, true);
        verify(userService).addSandbox(sandbox, user);
        verify(sandboxActivityLogService).sandboxUserAdded(sandbox, user);
    }

    @Test
    public void addMemberTest2() {
        sandboxService.addMember(sandbox, user);
        verify(sandboxActivityLogService).sandboxUserRoleChange(sandbox, user,  Role.USER, true);
    }

    @Test
    public void addMemberDefaultRolePrivateTest() {
        snd.setVisibility(Visibility.PRIVATE);
        sandboxService.addMember(snd, user);
        verify(sandboxActivityLogService).sandboxUserRoleChange(snd, user,  role, true);
    }

    @Test
    public void addMemberRoleTest() {
        sandbox.setUserRoles(new ArrayList<>());
        sandboxService.addMemberRole(sandbox, user, role);
        verify(sandboxActivityLogService).sandboxUserRoleChange(sandbox, user, role, true);
    }

    @Test
    public void addMemberRoleTestAlreadyMember() {
        sandboxService.addMemberRole(sandbox, user, role);
        verify(sandboxActivityLogService, times(0)).sandboxUserRoleChange(sandbox, user, role, true);
    }

    @Test
    public void addMemberRoleTestNewRole() {
        List<UserRole> userRoles = new ArrayList<>();
        UserRole userRole = new UserRole();
        userRole.setRole(Role.ADMIN);
        userRole.setUser(user);
        userRoles.add(userRole);
        sandbox.setUserRoles(userRoles);
        sandboxService.addMemberRole(sandbox, user, Role.MANAGE_USERS);
        verify(sandboxActivityLogService).sandboxUserRoleChange(sandbox, user, Role.MANAGE_USERS, true);
    }

    @Test
    public void removeMemberRoleTest() {
        sandboxService.removeMemberRole(sandbox, user, role);
        verify(userRoleService).delete(userRole);
    }

    @Test
    public void removeMemberRoleUserNotSandboxMemberTest() {
        sandboxService.removeMemberRole(sandbox, userEmptyRoles, role);
        verify(userRoleService, times(0)).delete(userRole);
    }

    @Test
    public void removeMemberRoleUserRoleUserNotMatchUserIdTest() {
        sandboxService.removeMemberRole(newSandboxApiNotSeven, user, Role.USER);
        verify(userRoleService, times(0)).delete(userRole);
    }

    @Test
    public void changePayerForSandboxTest() {
        sandboxService.changePayerForSandbox(sandbox, user);
        verify(sandbox).setPayerUserId(user.getId());
    }

    @Test
    public void hasMemberRoleTest() {
        assertEquals(true, sandboxService.hasMemberRole(sandbox, user, role));
    }

    @Test
    public void hasMemberRoleTestNoRole() {
        assertEquals(false, sandboxService.hasMemberRole(snd, user, role));
    }

    @Test
    public void hasMemberRoleUserNotMatchTest() {
        assertEquals(false, sandboxService.hasMemberRole(sandbox, userEmptyRoles, role));
    }

    @Test
    public void addSandboxImportTest() {
        sandboxService.addSandboxImport(sandbox, sandboxImport);
        verify(sandbox).setImports(sandbox.getImports());
    }

    @Test
    public void isSandboxMemberTest() {
        assertEquals(true, sandboxService.isSandboxMember(sandbox, user));
    }

    @Test
    public void isSandboxMemberTestNotMember() {
        assertEquals(false, sandboxService.isSandboxMember(snd, user));
    }

    @Test
    public void sandboxLoginTest() {
        sandboxService.sandboxLogin(sandbox.getSandboxId(), user.getSbmUserId());
        verify(userService).findBySbmUserId(user.getSbmUserId());
        verify(sandboxActivityLogService).sandboxLogin(sandbox, user);
    }

    @Test
    public void sandboxLoginTestNotMember() {
        User otherUser = new User();
        otherUser.setSbmUserId("other-user");
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(otherUser);
        sandboxService.sandboxLogin(sandbox.getSandboxId(), user.getSbmUserId());
        verify(userService).findBySbmUserId(user.getSbmUserId());
        verify(sandboxActivityLogService, times(0)).sandboxLogin(sandbox, user);
    }

    @Test
    public void saveTest() {
        when(repository.save(sandbox)).thenReturn(sandbox);
        assertEquals(sandbox, sandboxService.save(sandbox));
    }

    @Test
    public void getAllowedSandboxesTest() {
        List<Sandbox> publicSandBoxes = new ArrayList<>();
        Sandbox publicSandbox = new Sandbox();
        publicSandbox.setVisibility(Visibility.PUBLIC);
        publicSandBoxes.add(publicSandbox);
        when(repository.findByVisibility(Visibility.PUBLIC)).thenReturn(publicSandBoxes);
        assertEquals(2, sandboxService.getAllowedSandboxes(user).size());
    }

    @Test
    public void getAllowedSandboxesUserNullTest() {
        assertEquals(0, sandboxService.getAllowedSandboxes(null).size());
    }

    @Test
    public void getAllowedSandboxesUserNotMatchTest() {
        when(repository.findByVisibility(Visibility.PUBLIC)).thenReturn(sandboxes);
        assertEquals(1, sandboxService.getAllowedSandboxes(userEmptyRoles).size());
    }

    @Test
    public void getAllowedSandboxesListSandboxContainSandboxTest() {
        when(repository.findByVisibility(Visibility.PUBLIC)).thenReturn(sandboxes);
        assertEquals(1, sandboxService.getAllowedSandboxes(user).size());
    }


    @Test
    public void findBySandboxIdTest() {
        assertEquals(sandbox, sandboxService.findBySandboxId(sandbox.getSandboxId()));
    }

    @Test
    public void findByVisibilityTest() {
        when(repository.findByVisibility(Visibility.PUBLIC)).thenReturn(sandboxes);
        assertEquals(sandboxes, sandboxService.findByVisibility(Visibility.PUBLIC));
    }

    @Test
    public void fullCountTest() {
        when(repository.fullCount()).thenReturn("1");
        assertEquals("1", sandboxService.fullCount());
    }

    @Test
    public void schemaCountTest() {
        when(repository.schemaCount("6")).thenReturn("1");
        assertEquals("1", sandboxService.schemaCount("6"));
    }

    @Test
    public void intervalCountTest() {
        Timestamp timestamp = new Timestamp(new java.util.Date().getTime());
        when(repository.intervalCount(timestamp)).thenReturn("1");
        assertEquals("1", sandboxService.intervalCount(timestamp));
    }

    @Test
    public void findByPayerIdTest() {
        when(repository.findByPayerUserId(user.getId())).thenReturn(sandboxes);
        assertEquals(sandboxes, sandboxService.findByPayerId(user.getId()));
    }

    @Test
    public void getSandboxApiURLTest1() {
        sandbox.setApiEndpointIndex("1");
        assertEquals("apiBaseURL_1/" + sandbox.getSandboxId(), sandboxService.getSandboxApiURL(sandbox));
    }

    @Test
    public void getSandboxApiURLTest2() {
        sandbox.setApiEndpointIndex("2");
        assertEquals("apiBaseURL_2/" + sandbox.getSandboxId(), sandboxService.getSandboxApiURL(sandbox));
    }

    @Test
    public void getSandboxApiURLTest3() {
        sandbox.setApiEndpointIndex("3");
        assertEquals("apiBaseURL_3/" + sandbox.getSandboxId(), sandboxService.getSandboxApiURL(sandbox));
    }

    @Test
    public void getSandboxApiURLTest4() {
        sandbox.setApiEndpointIndex("4");
        assertEquals("apiBaseURL_4/" + sandbox.getSandboxId(), sandboxService.getSandboxApiURL(sandbox));
    }
    @Test
    public void getSandboxApiURLTest5() {
        sandbox.setApiEndpointIndex("5");
        assertEquals("apiBaseURL_5/" + sandbox.getSandboxId(), sandboxService.getSandboxApiURL(sandbox));
    }

    @Test
    public void getSandboxApiURLTest6() {
        sandbox.setApiEndpointIndex("6");
        assertEquals("apiBaseURL_6/" + sandbox.getSandboxId(), sandboxService.getSandboxApiURL(sandbox));
    }

    @Test
    public void getSandboxApiURLTest7() {
        sandbox.setApiEndpointIndex("7");
        assertEquals("apiBaseURL_7/" + sandbox.getSandboxId(), sandboxService.getSandboxApiURL(sandbox));
    }

    @Test
    public void createTest() {
        //THIS METHOD IS NO LONGER USED
    }

    @Test
    public void resetTest() {
        sandboxService.reset(sandbox, "");
        verify(launchScenarioService).findBySandboxId(sandbox.getSandboxId());
    }

    @Test
    public void newSandboxesInIntervalCountTest() {
        Date d = new Date();
        Timestamp intervalTime = new Timestamp(d.getTime());
        sandboxService.newSandboxesInIntervalCount(intervalTime, "5");
        verify(repository).newSandboxesInIntervalCount(intervalTime, "5");

    }

    @Test
    public void getSystemSandboxApiURLTest() {
        assertEquals(sandboxService.getSystemSandboxApiURL(), "apiBaseURL_5/system");
    }

    @Test
    public void findAllTest() {
        sandboxService.findAll();
        verify(repository).findAll();
    }


}
