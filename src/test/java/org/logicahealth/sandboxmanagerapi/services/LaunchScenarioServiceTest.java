package org.logicahealth.sandboxmanagerapi.services;

import org.junit.Before;
import org.junit.Test;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.repositories.LaunchScenarioRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.LaunchScenarioServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LaunchScenarioServiceTest {

    private LaunchScenarioRepository repository = mock(LaunchScenarioRepository.class);
    private ContextParamsService contextParamsService = mock(ContextParamsService.class);
    private AppService appService = mock(AppService.class);
    private UserPersonaService userPersonaService = mock(UserPersonaService.class);
    private UserLaunchService userLaunchService = mock(UserLaunchService.class);

    private LaunchScenarioServiceImpl launchScenarioService = new LaunchScenarioServiceImpl(repository);

    private LaunchScenario launchScenario;
    private List<LaunchScenario> launchScenarios;
    private List<ContextParams> contextParamsList;
    private ContextParams contextParams;
    private UserLaunch userLaunch;
    private List<UserLaunch> userLaunches;
    private App app;
    private Sandbox sandbox;
    private UserPersona userPersona;
    private User user;

    @Before
    public void setup() {
        launchScenarioService.setAppService(appService);
        launchScenarioService.setContextParamsService(contextParamsService);
        launchScenarioService.setUserPersonaService(userPersonaService);
        launchScenarioService.setUserLaunchService(userLaunchService);

        launchScenario = new LaunchScenario();
        launchScenario.setId(1);
        launchScenario.setVisibility(Visibility.PRIVATE);
        launchScenarios = new ArrayList<>();
        launchScenarios.add(launchScenario);

        contextParamsList = new ArrayList<>();
        contextParams = new ContextParams();
        contextParams.setName("param");
        contextParams.setValue("value");
        contextParamsList.add(contextParams);
        launchScenario.setContextParams(contextParamsList);

        userLaunch = new UserLaunch();
        userLaunch.setId(1);
        userLaunch.setLaunchScenario(launchScenario);
        userLaunches = new ArrayList<>();
        userLaunches.add(userLaunch);

        app = new App();
        app.setLaunchUri("launchUri");
        app.setClientId("clientId");
        app.setId(1);
        app.setCustomApp(false);
        launchScenario.setApp(app);

        sandbox = new Sandbox();
        sandbox.setSandboxId("sandboxId");
        launchScenario.setSandbox(sandbox);

        userPersona = new UserPersona();
        userPersona.setPersonaUserId("userId");
        userPersona.setId(1);
        launchScenario.setUserPersona(userPersona);

        user = new User();
        user.setId(1);
        user.setSbmUserId("me");
        launchScenario.setCreatedBy(user);
        when(repository.findById(launchScenario.getId())).thenReturn(of(launchScenario));
    }

    @Test
    public void saveTest() {
        when(repository.save(launchScenario)).thenReturn(launchScenario);
        LaunchScenario returnedLaunchScenario = launchScenarioService.save(launchScenario);
        assertEquals(launchScenario, returnedLaunchScenario);
    }

    @Test
    public void deleteWithIdTest() {
        launchScenarioService.delete(launchScenario.getId());
        verify(repository).deleteById(launchScenario.getId());
    }

    @Test
    public void deleteWithLaunchScenarioTest() {
        when(userLaunchService.findByLaunchScenarioId(launchScenario.getId())).thenReturn(userLaunches);
        launchScenarioService.delete(launchScenario);
        verify(contextParamsService).delete(contextParams);
        verify(userLaunchService).delete(userLaunch.getId());
    }

    @Test
    public void deleteWithLaunchScenarioTestAppIsCustom() {
        app.setCustomApp(true);
        launchScenario.setApp(app);
        when(userLaunchService.findByLaunchScenarioId(launchScenario.getId())).thenReturn(userLaunches);
        launchScenarioService.delete(launchScenario);
        verify(appService).delete(app);
        verify(contextParamsService).delete(contextParams);
        verify(userLaunchService).delete(userLaunch.getId());
    }

    @Test
    public void createTest() {
        launchScenario.setUserPersona(userPersona);
        when(userPersonaService.findByPersonaUserIdAndSandboxId(launchScenario.getUserPersona().getPersonaUserId(), sandbox.getSandboxId())).thenReturn(userPersona);
        launchScenarioService.create(launchScenario);
        verify(userPersonaService).findByPersonaUserIdAndSandboxId(any(), any());
        verify(userPersonaService, times(0)).save(any());
        verify(appService, times(0)).save(launchScenario.getApp());
        verify(appService).findByLaunchUriAndClientIdAndSandboxId(any(), any(), any());
    }

    @Test
    public void createTestUserPersonaNotExist() {
        launchScenario.setUserPersona(userPersona);
        when(userPersonaService.findByPersonaUserIdAndSandboxId(launchScenario.getUserPersona().getPersonaUserId(), sandbox.getSandboxId())).thenReturn(null);
        launchScenarioService.create(launchScenario);
        verify(userPersonaService).findByPersonaUserIdAndSandboxId(any(), any());
        verify(userPersonaService).save(any());
        verify(appService, times(0)).save(launchScenario.getApp());
        verify(appService).findByLaunchUriAndClientIdAndSandboxId(any(), any(), any());
    }

    @Test
    public void createTestUserPersonaNull() {
        when(userPersonaService.findByPersonaUserIdAndSandboxId(launchScenario.getUserPersona().getPersonaUserId(), sandbox.getSandboxId())).thenReturn(null);
        launchScenarioService.create(launchScenario);
        verify(userPersonaService).findByPersonaUserIdAndSandboxId(any(), any());
        verify(userPersonaService).save(any());
        verify(appService, times(0)).save(launchScenario.getApp());
        verify(appService).findByLaunchUriAndClientIdAndSandboxId(any(), any(), any());
    }

    @Test
    public void createTestAppIsCustom() {
        app.setCustomApp(true);
        launchScenario.setApp(app);
        launchScenario.setUserPersona(userPersona);
        when(userPersonaService.findByPersonaUserIdAndSandboxId(launchScenario.getUserPersona().getPersonaUserId(), sandbox.getSandboxId())).thenReturn(null);
        launchScenarioService.create(launchScenario);
        verify(userPersonaService).findByPersonaUserIdAndSandboxId(any(), any());
        verify(userPersonaService).save(any());
        verify(appService).save(any());
        verify(appService, times(0)).findByLaunchUriAndClientIdAndSandboxId(any(), any(), any());
    }

    @Test
    public void updateTest() {
        when(repository.findById(launchScenario.getId())).thenReturn(of(launchScenario));
        when(userPersonaService.getById(launchScenario.getUserPersona().getId())).thenReturn(userPersona);
        when(appService.getById(launchScenario.getApp().getId())).thenReturn(app);
        launchScenarioService.update(launchScenario);
        verify(appService).getById(launchScenario.getApp().getId());
        verify(userPersonaService).getById(launchScenario.getUserPersona().getId());
    }

    @Test
    public void updateTestNotFound() {
        when(repository.findById(launchScenario.getId())).thenReturn(Optional.empty());
        launchScenarioService.update(launchScenario);
        verify(appService, times(0)).getById(launchScenario.getApp().getId());
        verify(userPersonaService, times(0)).getById(launchScenario.getUserPersona().getId());
    }

    @Test
    public void updateTestAppIsCustom() {
        app.setCustomApp(true);
        launchScenario.setApp(app);
        when(userPersonaService.getById(launchScenario.getUserPersona().getId())).thenReturn(userPersona);
        when(appService.getById(launchScenario.getApp().getId())).thenReturn(app);
        launchScenarioService.update(launchScenario);
        verify(userPersonaService).getById(launchScenario.getUserPersona().getId());
        //TODO
//        verify(appService).getById(launchScenario.getApp().getId());
        verify(appService).save(any());
    }

    @Test
    public void updateContextParamsTest() {
        List<ContextParams> newContextParamsList = new ArrayList<>();
        ContextParams newContextParams = new ContextParams();
        newContextParams.setName("param2");
        newContextParams.setValue("value2");
        newContextParamsList.add(newContextParams);
        LaunchScenario newLaunchScenario = launchScenarioService.updateContextParams(launchScenario, newContextParamsList);
        verify(contextParamsService).delete(any());
        launchScenario.setContextParams(newContextParamsList);
        assertEquals(launchScenario, newLaunchScenario);
    }

    @Test
    public void findAllTest() {
        Iterable<LaunchScenario> launchScenarios = () -> null;
        when(repository.findAll()).thenReturn(launchScenarios);
        Iterable<LaunchScenario> returnedLaunchScenarios = launchScenarioService.findAll();
        assertEquals(launchScenarios, returnedLaunchScenarios);
    }

    @Test
    public void findBySandboxIdTest() {
        when(repository.findBySandboxId(launchScenario.getSandbox().getSandboxId())).thenReturn(launchScenarios);
        List<LaunchScenario> returnedLaunchSenarios = launchScenarioService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
        assertEquals(launchScenarios, returnedLaunchSenarios);
    }

    @Test
    public void findByAppIdAndSandboxIdTest() {
        when(repository.findByAppIdAndSandboxId(launchScenario.getApp().getId(), launchScenario.getSandbox().getSandboxId())).thenReturn(launchScenarios);
        List<LaunchScenario> returnedLaunchSenarios = launchScenarioService.findByAppIdAndSandboxId(launchScenario.getApp().getId(), launchScenario.getSandbox().getSandboxId());
        assertEquals(launchScenarios, returnedLaunchSenarios);
    }

    @Test
    public void findByUserPersonaIdAndSandboxIdTest() {
        when(repository.findByUserPersonaIdAndSandboxId(launchScenario.getUserPersona().getId(), launchScenario.getSandbox().getSandboxId())).thenReturn(launchScenarios);
        List<LaunchScenario> returnedLaunchSenarios = launchScenarioService.findByUserPersonaIdAndSandboxId(launchScenario.getUserPersona().getId(), launchScenario.getSandbox().getSandboxId());
        assertEquals(launchScenarios, returnedLaunchSenarios);
    }

    @Test
    public void findBySandboxIdAndCreatedByOrVisibilityTest() {
        when(repository.findBySandboxIdAndCreatedByOrVisibility(launchScenario.getSandbox().getSandboxId(), launchScenario.getCreatedBy().getSbmUserId(), launchScenario.getVisibility())).thenReturn(launchScenarios);
        List<LaunchScenario> returnedLaunchSenarios = launchScenarioService.findBySandboxIdAndCreatedByOrVisibility(launchScenario.getSandbox().getSandboxId(), launchScenario.getCreatedBy().getSbmUserId(), launchScenario.getVisibility());
        assertEquals(launchScenarios, returnedLaunchSenarios);
    }

    @Test
    public void findBySandboxIdAndCreatedByTest() {
        when(repository.findBySandboxIdAndCreatedBy(launchScenario.getSandbox().getSandboxId(), launchScenario.getCreatedBy().getSbmUserId())).thenReturn(launchScenarios);
        List<LaunchScenario> returnedLaunchSenarios = launchScenarioService.findBySandboxIdAndCreatedBy(launchScenario.getSandbox().getSandboxId(), launchScenario.getCreatedBy().getSbmUserId());
        assertEquals(launchScenarios, returnedLaunchSenarios);
    }

    @Test
    public void updateLastLaunchForCurrentUserTest() {
        when(userLaunchService.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId())).thenReturn(userLaunch);
        launchScenarioService.updateLastLaunchForCurrentUser(launchScenarios, user);
        verify(userLaunchService).findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId());
    }

    @Test
    public void updateLastLaunchForCurrentUserTestUserLaunchNull() {
        when(userLaunchService.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId())).thenReturn(null);
        launchScenarioService.updateLastLaunchForCurrentUser(launchScenarios, user);
        verify(userLaunchService).findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId());
    }

}
