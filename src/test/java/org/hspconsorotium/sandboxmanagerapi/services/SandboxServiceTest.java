package org.hspconsorotium.sandboxmanagerapi.services;

import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicStatusLine;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.hspconsortium.sandboxmanagerapi.services.impl.SandboxDeleteFailedException;
import org.hspconsortium.sandboxmanagerapi.services.impl.SandboxServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
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
    private HttpClientBuilder builder = mock(HttpClientBuilder.class);
    private CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    private CloseableHttpResponse response = spy(CloseableHttpResponse.class);

    private SandboxServiceImpl sandboxService = new SandboxServiceImpl(repository);

    private Sandbox sandbox = spy(Sandbox.class);
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
    private Role role = Role.ADMIN;

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
        user = new User();
        user.setId(1);
        user.setSbmUserId("userId");
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

        HttpClientBuilder built = HttpClientBuilder.create();

        statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!");

        when(ruleService.checkIfUserCanCreateSandbox(user)).thenReturn(true);
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
//        when(builder.create()).thenReturn(built);
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
        sandboxService.delete(sandbox, bearerToken, user);
        verify(sandboxImportService).delete(sandboxImport);
        verify(sandboxActivityLogService).sandboxDelete(sandbox, user);
    }

    @Test
    public void deleteTestAllAdminIsNull() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        sandboxService.delete(sandbox, bearerToken, null);
        verify(sandboxActivityLogService).sandboxDelete(sandbox, sandbox.getCreatedBy());
    }

    @Test
    public void deleteTestAllVerifyItemsDeleted() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(launchScenarioService.findBySandboxId(sandbox.getSandboxId())).thenReturn(launchScenarios);
        when(userPersonaService.findBySandboxId(sandbox.getSandboxId())).thenReturn(userPersonas);
        when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(apps);
        sandboxService.delete(sandbox, bearerToken, user);
        verify(launchScenarioService).delete(launchScenario);
        verify(userPersonaService).delete(userPersona);
        verify(appService).delete(app);
        verify(userAccessHistoryService).deleteUserAccessInstancesForSandbox(sandbox);
    }

    @Test
    public void cloneTest() throws IOException {
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);

    }

    @Test
    public void cloneTestCantClone() throws IOException {
        when(ruleService.checkIfUserCanCreateSandbox(user)).thenReturn(false);
        Sandbox returnedSandbox = sandboxService.clone(sandbox, sandbox.getSandboxId(), user, bearerToken);
        assertEquals(null, returnedSandbox);
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

    }

    @Test
    public void isSandboxMemberTestNotMember() {

    }



    @Test
    public void findBySandboxIdTest() {
        Sandbox returnedSandbox = sandboxService.findBySandboxId(sandbox.getSandboxId());
        assertEquals(sandbox, returnedSandbox);
    }


}
