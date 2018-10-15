package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.AppRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.hspconsortium.sandboxmanagerapi.services.impl.AppServiceImpl;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AppServiceTest {

    private AppRepository appRepository = mock(AppRepository.class);


    private ImageService imageService = mock(ImageService.class);

    private OAuthClientService oAuthClientService = mock(OAuthClientService.class);

    private LaunchScenarioService launchScenarioService = mock(LaunchScenarioService.class);

    private UserLaunchService userLaunchService = mock(UserLaunchService.class);

    private AppServiceImpl appService = new AppServiceImpl(appRepository);

    private App app;
    private List<App> apps = new ArrayList<>();
    private Sandbox sandbox;
    private LaunchScenario launchScenario;
    private List<LaunchScenario> launchScenarios = new ArrayList<>();
    private UserLaunch userLaunch;
    private List<UserLaunch> userLaunches = new ArrayList<>();
    private Image logo;

    @Before
    public void setup() throws Exception {
        appService.setLaunchScenarioService(launchScenarioService);
        appService.setoAuthClientService(oAuthClientService);
        appService.setImageService(imageService);
        appService.setUserLaunchService(userLaunchService);
        apps.add(app);

        sandbox = new Sandbox();
        sandbox.setSandboxId("sandbox");
        sandbox.setVisibility(Visibility.PRIVATE);
        User user = new User();
        user.setSbmUserId("me");
        sandbox.setCreatedBy(user);

        app = new App();
        app.setCopyType(CopyType.MASTER);
        app.setClientId("clientId");
        app.setClientName("clientName");
        app.setId(1);
        app.setSandbox(sandbox);
        app.setLaunchUri("launchUri");
        JSONObject client = new JSONObject();
        client.put("clientName", app.getClientName());
        client.put("clientId", app.getClientId());
        String clientString = client.toString();
        app.setClientJSON(clientString);

        launchScenario = new LaunchScenario();
        launchScenario.setApp(app);
        launchScenarios.add(launchScenario);
        launchScenario.setId(1);

        userLaunch = new UserLaunch();
        userLaunch.setId(1);
        userLaunches.add(userLaunch);

        logo = new Image();
        logo.setId(1);
        app.setLogo(logo);

        when(oAuthClientService.getOAuthClientWithClientId(app.getClientId())).thenReturn(app.getClientJSON());
    }

    @Test
    public void saveTest() {
        when(appRepository.save(app)).thenReturn(app);
        App returnedApp = appService.save(app);
        assertEquals(app, returnedApp);
    }

    @Test
    public void deleteWithIdTest() {
        appService.delete(1);
        verify(appRepository).delete(1);
    }

    @Test
    public void deleteWithAppTest() {
        when(launchScenarioService.findByAppIdAndSandboxId(app.getId(), app.getSandbox().getSandboxId())).thenReturn(launchScenarios);
        when(userLaunchService.findByLaunchScenarioId(launchScenario.getId())).thenReturn(userLaunches);
        appService.delete(app);
        verify(oAuthClientService).deleteOAuthClientWithClientId(app.getClientId());
        verify(userLaunchService).delete(userLaunch.getId());
        verify(launchScenarioService).delete(launchScenario.getId());
        verify(imageService).delete(logo.getId());
    }

    @Test
    public void deleteWithAppTestReplicaCopyType() {
        app.setCopyType(CopyType.REPLICA);
        when(launchScenarioService.findByAppIdAndSandboxId(app.getId(), app.getSandbox().getSandboxId())).thenReturn(launchScenarios);
        when(userLaunchService.findByLaunchScenarioId(launchScenario.getId())).thenReturn(userLaunches);
        appService.delete(app);
        verify(oAuthClientService, times(0)).deleteOAuthClientWithClientId(app.getClientId());
    }

    @Test
    public void createTest() {
        when(oAuthClientService.postOAuthClient(app.getClientJSON())).thenReturn(app.getClientJSON());
        appService.create(app, sandbox);
        verify(oAuthClientService).postOAuthClient(app.getClientJSON());
    }

    @Test(expected = RuntimeException.class)
    public void createTestWithBadClientJSON() {
        String clientString = "not an object";
        app.setClientJSON(clientString);
        when(oAuthClientService.postOAuthClient(app.getClientJSON())).thenReturn(clientString);
        appService.create(app, sandbox);
    }

    @Test
    public void updateTest() {
        when(appRepository.findOne(app.getId())).thenReturn(app);
        appService.update(app);
        verify(oAuthClientService).putOAuthClientWithClientId(app.getClientId(), app.getClientJSON());
    }

    @Test(expected = RuntimeException.class)
    public void updateTestWithBadClientJSON() {
        app.setClientJSON("not an object");
        when(appRepository.findOne(app.getId())).thenReturn(app);
        appService.update(app);
        verify(oAuthClientService).putOAuthClientWithClientId(app.getClientId(), app.getClientJSON());
    }

    @Test
    public void updateTestReplicaCopy() {
        app.setCopyType(CopyType.REPLICA);
        when(appRepository.findOne(app.getId())).thenReturn(app);
        App returnedApp = appService.update(app);
        verify(oAuthClientService, times(0)).putOAuthClientWithClientId(app.getClientId(), app.getClientJSON());
        assertEquals(app, returnedApp);
    }

    @Test
    public void getClientJSONTest() {
        when(oAuthClientService.getOAuthClientWithClientId(app.getClientId())).thenReturn(app.getClientJSON());
        App returnedApp = appService.getClientJSON(app);
        assertEquals(app, returnedApp);
    }

    @Test
    public void updateAppImageTest() {
        when(oAuthClientService.getOAuthClientWithClientId(app.getClientId())).thenReturn(app.getClientJSON());
        appService.updateAppImage(app, logo);
        verify(oAuthClientService).putOAuthClientWithClientId(app.getClientId(), app.getClientJSON());
        verify(imageService).delete(app.getLogo().getId());
    }

    @Test
    public void updateAppImageTestReplicaCopy() {
        app.setCopyType(CopyType.REPLICA);
        appService.updateAppImage(app, logo);
        verify(oAuthClientService, times(0)).putOAuthClientWithClientId(app.getClientId(), app.getClientJSON());
        verify(imageService).delete(app.getLogo().getId());
    }

    @Test(expected = RuntimeException.class)
    public void updateAppImageTestWithBadClientJSON() {
        when(oAuthClientService.getOAuthClientWithClientId(app.getClientId())).thenReturn("Not an object");
        appService.updateAppImage(app, logo);
    }

    @Test
    public void deleteAppImageTest() throws Exception {
        JSONObject client = new JSONObject();
        client.put("clientName", app.getClientName());
        client.put("clientId", app.getClientId());
        client.put("logoUri", "null");
        String clientString = client.toString();
        app.setClientJSON(clientString);
        appService.deleteAppImage(app);
        verify(oAuthClientService).putOAuthClientWithClientId(app.getClientId(), app.getClientJSON());
        verify(imageService).delete(logo.getId());
    }

    @Test
    public void deleteAppImageTestNoLogo() {
        app.setLogo(null);
        appService.deleteAppImage(app);
        verify(imageService, times(0)).delete(logo.getId());
    }

    @Test(expected = RuntimeException.class)
    public void deleteAppImageTestWithBadClientJSON() {
        when(oAuthClientService.getOAuthClientWithClientId(app.getClientId())).thenReturn("Not an object");
        appService.deleteAppImage(app);
    }

    @Test
    public void deleteAppImageTestReplicaCopy() {
        app.setCopyType(CopyType.REPLICA);
        App returnedApp = appService.deleteAppImage(app);
        assertEquals(app, returnedApp);
    }

    @Test
    public void getByIdTest() {
        when(appRepository.findOne(app.getId())).thenReturn(app);
        App returnedApp = appService.getById(app.getId());
        assertEquals(app, returnedApp);
    }

    @Test
    public void findByLaunchUriAndClientIdAndSandboxIdTest() {
        when(appRepository.findByLaunchUriAndClientIdAndSandboxId(app.getLaunchUri(), app.getClientId(), app.getSandbox().getSandboxId())).thenReturn(app);
        App returnedApp = appService.findByLaunchUriAndClientIdAndSandboxId(app.getLaunchUri(), app.getClientId(), app.getSandbox().getSandboxId());
        assertEquals(app, returnedApp);
    }

    @Test
    public void findBySandboxIdTest() {
        when(appRepository.findBySandboxId(sandbox.getSandboxId())).thenReturn(apps);
        List<App> returnedApps = appService.findBySandboxId(sandbox.getSandboxId());
        assertEquals(apps, returnedApps);
    }

    @Test
    public void findBySandboxIdAndCreatedByOrVisibilityTest() {
        when(appRepository.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), sandbox.getCreatedBy().getSbmUserId(), sandbox.getVisibility())).thenReturn(apps);
        List<App> returnedApps = appService.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), sandbox.getCreatedBy().getSbmUserId(), sandbox.getVisibility());
        assertEquals(apps, returnedApps);
    }

    @Test
    public void findBySandboxIdAndCreatedByTest() {
        when(appRepository.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), sandbox.getCreatedBy().getSbmUserId())).thenReturn(apps);
        List<App> returnedApps = appService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), sandbox.getCreatedBy().getSbmUserId());
        assertEquals(apps, returnedApps);
    }
}
