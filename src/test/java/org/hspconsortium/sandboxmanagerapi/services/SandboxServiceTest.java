package org.hspconsortium.sandboxmanagerapi.services;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.hspconsortium.sandboxmanagerapi.services.impl.SandboxServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SandboxServiceTest {

    private SandboxRepository repository = mock(SandboxRepository.class);
    private UserService userService = mock(UserService.class);
    private UserRoleService userRoleService = mock(UserRoleService.class);
    private UserPersonaService userPersonaService = mock(UserPersonaService.class);
    private UserLaunchService userLaunchService = mock(UserLaunchService.class);
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

    private User user;
    private String bearerToken = "token";
    private SandboxImport sandboxImport;
    private StatusLine statusLine;
    private LaunchScenario launchScenario;
    private UserPersona userPersona;
    private App app;
    private UserLaunch userLaunch;
    private UserRole userRole;
    private List<LaunchScenario> launchScenarios;
    private List<UserPersona> userPersonas;
    private List<App> apps;
    private List<UserLaunch> userLaunches;
    private List<UserRole> userRoles;
    private List<Sandbox> sandboxes;
    private Role role = Role.ADMIN;
    private String token = "token";

    @Before
    public void setup() {
        sandboxService.setUserAccessHistoryService(userAccessHistoryService);
        sandboxService.setUserLaunchService(userLaunchService);
        sandboxService.setUserPersonaService(userPersonaService);
        sandboxService.setUserRoleService(userRoleService);
        sandboxService.setUserService(userService);
        sandboxService.setAppService(appService);
        sandboxService.setLaunchScenarioService(launchScenarioService);
        sandboxService.setSandboxImportService(sandboxImportService);
        sandboxService.setSandboxActivityLogService(sandboxActivityLogService);
        sandboxService.setRuleService(ruleService);
        sandboxService.setHttpClient(httpClient);

//        sandbox = new Sandbox();
        sandbox.setId(1);
        sandbox.setSandboxId("sandboxId");
        sandbox.setApiEndpointIndex("6");
        newSandbox.setSandboxId("new-sandbox");
        newSandbox.setApiEndpointIndex("7");
        sandboxes = new ArrayList<>();
        sandboxes.add(sandbox);
        user = new User();
        user.setId(1);
        user.setSbmUserId("userId");
        user.setSandboxes(sandboxes);
        sandbox.setCreatedBy(user);
        List<SandboxImport> sandboxImportList = new ArrayList<>();
        sandboxImport = new SandboxImport();
        sandboxImportList.add(sandboxImport);
        sandbox.setImports(sandboxImportList);
        launchScenarios = new ArrayList<>();
        launchScenario = new LaunchScenario();
        launchScenario.setId(1);
        launchScenario.setVisibility(Visibility.PRIVATE);
        launchScenarios.add(launchScenario);
        launchScenario.setSandbox(sandbox);
        apps = new ArrayList<>();
        app = new App();
        app.setId(1);
        app.setVisibility(Visibility.PRIVATE);
        apps.add(app);
        userPersonas = new ArrayList<>();
        userPersona = new UserPersona();
        userPersona.setId(1);
        userPersona.setVisibility(Visibility.PRIVATE);
        userPersona.setPersonaUserId("user@sandbox");
        launchScenario.setUserPersona(userPersona);
        userPersonas.add(userPersona);
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

        statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!");

        ReflectionTestUtils.setField(sandboxService, "defaultSandboxVisibility", "PRIVATE");
        ReflectionTestUtils.setField(sandboxService, "expirationDate", "2018-09-01");
        ReflectionTestUtils.setField(sandboxService, "defaultSandboxCreatorRoles", new String[0]);
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_1", "apiBaseURL_1");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_2", "apiBaseURL_2");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_3", "apiBaseURL_3");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_4", "apiBaseURL_4");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_5", "apiBaseURL_5");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_6", "apiBaseURL_6");
        ReflectionTestUtils.setField(sandboxService, "apiBaseURL_7", "apiBaseURL_7");

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
    }

    @Test
    public void deleteTestAllAdminIsNull() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.delete(sandbox, bearerToken, null, false);
        verify(sandboxActivityLogService).sandboxDelete(sandbox, sandbox.getCreatedBy());
    }

    @Test
    public void deleteTestAllVerifyItemsDeleted() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(launchScenarioService.findBySandboxId(sandbox.getSandboxId())).thenReturn(launchScenarios);
        when(userPersonaService.findBySandboxId(sandbox.getSandboxId())).thenReturn(userPersonas);
        when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(apps);
        sandboxService.delete(sandbox, bearerToken, user, false);
        verify(launchScenarioService).delete(launchScenario);
        verify(userPersonaService).delete(userPersona);
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
        Sandbox returnedSandbox = sandboxService.clone(newSandbox, sandbox.getSandboxId(), user, bearerToken);
        assertEquals(null, returnedSandbox);
    }

    @Test
    public void cloneTestCantClone() throws IOException {
        when(ruleService.checkIfUserCanCreateSandbox(user, token)).thenReturn(false);
        Sandbox returnedSandbox = sandboxService.clone(sandbox, sandbox.getSandboxId(), user, bearerToken);
        assertEquals(null, returnedSandbox);
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
    public void addMemberTest() {
        sandboxService.addMember(sandbox, user, role);
        verify(sandboxActivityLogService).sandboxUserRoleChange(sandbox, user,  role, true);
        verify(userService).addSandbox(sandbox, user);
        verify(sandboxActivityLogService).sandboxUserAdded(sandbox, user);
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
    public void changePayerForSandboxTest() {
        sandboxService.changePayerForSandbox(sandbox, user);
        verify(sandbox).setPayerUserId(user.getId());
    }

    @Test
    public void hasMemberRoleTest() {
        Boolean bool = sandboxService.hasMemberRole(sandbox, user, role);
        assertEquals(true, bool);
    }

    @Test
    public void hasMemberRoleTestNoRole() {
        sandbox.setUserRoles(new ArrayList<>());
        Boolean bool = sandboxService.hasMemberRole(sandbox, user, role);
        assertEquals(false, bool);
    }

    @Test
    public void addSandboxImportTest() {
        sandboxService.addSandboxImport(sandbox, sandboxImport);
        verify(sandbox).setImports(sandbox.getImports());
    }

    @Test
    public void isSandboxMemberTest() {
        Boolean bool = sandboxService.isSandboxMember(sandbox, user);
        assertEquals(true, bool);
    }

    @Test
    public void isSandboxMemberTestNotMember() {
        sandbox = new Sandbox();
        sandbox.setUserRoles(new ArrayList<>());
        Boolean bool = sandboxService.isSandboxMember(sandbox, user);
        assertEquals(false, bool);
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
        Sandbox returnedSandbox = sandboxService.save(sandbox);
        assertEquals(sandbox, returnedSandbox);
    }

    @Test
    public void getAllowedSandboxesTest() {
        List<Sandbox> publicSandBoxes = new ArrayList<>();
        Sandbox publicSandbox = new Sandbox();
        publicSandbox.setVisibility(Visibility.PUBLIC);
        publicSandBoxes.add(publicSandbox);
        when(repository.findByVisibility(Visibility.PUBLIC)).thenReturn(publicSandBoxes);
        List<Sandbox> returnedSandboxes = sandboxService.getAllowedSandboxes(user);
        assertEquals(2, returnedSandboxes.size());
    }

    @Test
    public void findBySandboxIdTest() {
        Sandbox returnedSandbox = sandboxService.findBySandboxId(sandbox.getSandboxId());
        assertEquals(sandbox, returnedSandbox);
    }

    @Test
    public void findByVisibilityTest() {
        when(repository.findByVisibility(Visibility.PUBLIC)).thenReturn(sandboxes);
        List<Sandbox> returnedSandboxes = sandboxService.findByVisibility(Visibility.PUBLIC);
        assertEquals(sandboxes, returnedSandboxes);
    }

    @Test
    public void fullCountTest() {
        when(repository.fullCount()).thenReturn("1");
        String returnedCount = sandboxService.fullCount();
        assertEquals("1", returnedCount);
    }

    @Test
    public void schemaCountTest() {
        when(repository.schemaCount("6")).thenReturn("1");
        String returnedSchemaCount = sandboxService.schemaCount("6");
        assertEquals("1", returnedSchemaCount);
    }

    @Test
    public void intervalCountTest() {
        Timestamp timestamp = new Timestamp(new java.util.Date().getTime());
        when(repository.intervalCount(timestamp)).thenReturn("1");
        String returnedIntervalCount = sandboxService.intervalCount(timestamp);
        assertEquals("1", returnedIntervalCount);
    }

    @Test
    public void findByPayerIdTest() {
        when(repository.findByPayerUserId(user.getId())).thenReturn(sandboxes);
        List<Sandbox> returnedSandboxes = sandboxService.findByPayerId(user.getId());
        assertEquals(sandboxes, returnedSandboxes);
    }

    @Test
    public void getSandboxApiURLTest() {
        sandbox.setApiEndpointIndex("1");
        String url = sandboxService.getSandboxApiURL(sandbox);
        assertEquals("apiBaseURL_1/" + sandbox.getSandboxId(), url);

        sandbox.setApiEndpointIndex("2");
        url = sandboxService.getSandboxApiURL(sandbox);
        assertEquals("apiBaseURL_2/" + sandbox.getSandboxId(), url);

        sandbox.setApiEndpointIndex("3");
        url = sandboxService.getSandboxApiURL(sandbox);
        assertEquals("apiBaseURL_3/" + sandbox.getSandboxId(), url);

        sandbox.setApiEndpointIndex("4");
        url = sandboxService.getSandboxApiURL(sandbox);
        assertEquals("apiBaseURL_4/" + sandbox.getSandboxId(), url);

        sandbox.setApiEndpointIndex("5");
        url = sandboxService.getSandboxApiURL(sandbox);
        assertEquals("apiBaseURL_5/" + sandbox.getSandboxId(), url);

        sandbox.setApiEndpointIndex("6");
        url = sandboxService.getSandboxApiURL(sandbox);
        assertEquals("apiBaseURL_6/" + sandbox.getSandboxId(), url);

        sandbox.setApiEndpointIndex("7");
        url = sandboxService.getSandboxApiURL(sandbox);
        assertEquals("apiBaseURL_7/" + sandbox.getSandboxId(), url);
    }


}
