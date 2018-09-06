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
//    private CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
//    private CloseableHttpResponse response = spy(CloseableHttpResponse.class);

    private SandboxServiceImpl sandboxService = new SandboxServiceImpl(repository);

    private Sandbox sandbox;
    private User user;
    private String bearerToken = "token";
    private SandboxImport sandboxImport;
    private StatusLine statusLine;
    private LaunchScenario launchScenario;
    private UserPersona userPersona;
    private App app;
    private List<LaunchScenario> launchScenarios;
    private List<UserPersona> userPersonas;
    private List<App> apps;

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
//        sandboxService.setHttpClient(httpClient);

        sandbox = new Sandbox();
        sandbox.setId(1);
        sandbox.setSandboxId("sandboxId");
        sandbox.setApiEndpointIndex("6");
        user = new User();
        user.setSbmUserId("userId");
        sandbox.setCreatedBy(user);
        List<SandboxImport> sandboxImportList = new ArrayList<>();
        sandboxImport = new SandboxImport();
        sandboxImportList.add(sandboxImport);
        sandbox.setImports(sandboxImportList);
        launchScenarios = new ArrayList<>();
        launchScenario = new LaunchScenario();
        launchScenarios.add(launchScenario);
        apps = new ArrayList<>();
        app = new App();
        apps.add(app);
        userPersonas = new ArrayList<>();
        userPersona = new UserPersona();
        userPersonas.add(userPersona);
        HttpClientBuilder built = HttpClientBuilder.create();

        statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!");

//        when(ruleService.checkIfUserCanCreateSandbox(user)).thenReturn(true);
//        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
//        when(builder.create()).thenReturn(built);
    }

    @Test
    public void deleteTest() {
        sandboxService.delete(sandbox.getId());
        verify(repository).delete(sandbox.getId());
    }

//    @Test
//    public void deleteTestAll() throws IOException {
//        when(httpClient.execute(any())).thenReturn(response);
//        when(response.getStatusLine()).thenReturn(statusLine);
//        sandboxService.delete(sandbox, bearerToken, user);
//        verify(sandboxImportService).delete(sandboxImport);
//        verify(sandboxActivityLogService).sandboxDelete(sandbox, user);
//    }
//
//    @Test
//    public void deleteTestAllAdminIsNull() throws IOException {
//        when(httpClient.execute(any())).thenReturn(response);
//        when(response.getStatusLine()).thenReturn(statusLine);
//        sandboxService.delete(sandbox, bearerToken, null);
//        verify(sandboxActivityLogService).sandboxDelete(sandbox, sandbox.getCreatedBy());
//    }
//
//    @Test
//    public void deleteTestAllVerifyItemsDeleted() throws IOException {
//        when(httpClient.execute(any())).thenReturn(response);
//        when(response.getStatusLine()).thenReturn(statusLine);
//        when(launchScenarioService.findBySandboxId(sandbox.getSandboxId())).thenReturn(launchScenarios);
//        when(userPersonaService.findBySandboxId(sandbox.getSandboxId())).thenReturn(userPersonas);
//        when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(apps);
//        sandboxService.delete(sandbox, bearerToken, user);
//        verify(launchScenarioService).delete(launchScenario);
//        verify(userPersonaService).delete(userPersona);
//        verify(appService).delete(app);
//        verify(userAccessHistoryService).deleteUserAccessInstancesForSandbox(sandbox);
//    }
//
//    @Test
//    public void cloneTest() throws IOException {
//        when(httpClient.execute(any())).thenReturn(response);
//        when(response.getStatusLine()).thenReturn(statusLine);
//
//    }
//
//    @Test
//    public void cloneTestCantClone() throws IOException {
//        when(ruleService.checkIfUserCanCreateSandbox(user)).thenReturn(false);
//        Sandbox returnedSandbox = sandboxService.clone(sandbox, sandbox.getSandboxId(), user, bearerToken);
//        assertEquals(null, returnedSandbox);
//    }
//
//
//    @Test
//    public void findBySandboxId() {
//        Sandbox returnedSandbox = sandboxService.findBySandboxId(sandbox.getSandboxId());
//        assertEquals(sandbox, returnedSandbox);
//    }

//    @Test
//    public void
}
