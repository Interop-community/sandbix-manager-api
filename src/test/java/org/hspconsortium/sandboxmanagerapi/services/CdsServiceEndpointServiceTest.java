package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.CdsServiceEndpointRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.CdsServiceEndpointServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CdsServiceEndpointServiceTest {

    private CdsServiceEndpointRepository cdsServiceEndpointRepository = mock(CdsServiceEndpointRepository.class);

    private CdsHookService cdsHookService = mock(CdsHookService.class);

    private LaunchScenarioService launchScenarioService = mock(LaunchScenarioService.class);

    private UserLaunchService userLaunchService = mock(UserLaunchService.class);

    private CdsServiceEndpointServiceImpl cdsServiceEndpointService = new CdsServiceEndpointServiceImpl(cdsServiceEndpointRepository);

    private CdsServiceEndpoint cdsServiceEndpoint;
    private List<CdsServiceEndpoint> cdsServiceEndpoints = new ArrayList<>();
    private Sandbox sandbox;
    private LaunchScenario launchScenario;
    private List<LaunchScenario> launchScenarios = new ArrayList<>();
    private CdsHook cdsHook;
    private CdsHook existingCdsHook;
    private List<CdsHook> cdsHooks = new ArrayList<>();
    private List<CdsHook> existingCdsHooks = new ArrayList<>();
    private UserLaunch userLaunch;
    private List<UserLaunch> userLaunches = new ArrayList<>();

    @Before
    public void setup() {
        cdsServiceEndpointService.setCdsHookService(cdsHookService);
        cdsServiceEndpointService.setLaunchScenarioService(launchScenarioService);
        cdsServiceEndpointService.setLaunchScenarioCdsServices(launchScenarioService, userLaunchService);
        cdsServiceEndpointService.setUserLaunchService(userLaunchService);

        sandbox = new Sandbox();
        sandbox.setSandboxId("sandbox");
        sandbox.setVisibility(Visibility.PRIVATE);
        User user = new User();
        user.setSbmUserId("me");
        sandbox.setCreatedBy(user);

        cdsHook = new CdsHook();
        cdsHook.setId(1);
        cdsHook.setHook("patient-view");
        cdsHook.setTitle("Sends a Demo Info Card");
        cdsHook.setHookUrl("http://www.google.com");
        cdsHook.setHookId("demo-suggestion-card");

        cdsHooks.add(cdsHook);

        existingCdsHook = new CdsHook();
        existingCdsHook.setId(1);
        existingCdsHook.setHook("patient-view");
        existingCdsHook.setTitle("Sends a Demo Info Card");
        existingCdsHook.setHookUrl("http://www.google.com");
        existingCdsHook.setHookId("demo-suggestion-card-2");

        existingCdsHooks.add(existingCdsHook);


        cdsServiceEndpoint = new CdsServiceEndpoint();
        cdsServiceEndpoint.setId(1);
        cdsServiceEndpoint.setSandbox(sandbox);
        cdsServiceEndpoint.setCreatedBy(user);
        cdsServiceEndpoint.setCdsHooks(cdsHooks);
        cdsServiceEndpoint.setUrl("http://www.google.com");
        cdsServiceEndpoint.setTitle("Bilirubin");
        cdsServiceEndpoint.setDescription("This is a test");

        userLaunch = new UserLaunch();
        userLaunch.setId(1);
        userLaunches.add(userLaunch);

        launchScenario = new LaunchScenario();
        launchScenario.setId(1);
        launchScenario.setCdsHook(cdsHook);
        launchScenarios.add(launchScenario);

    }

    @Test
    public void saveTest() {
        when(cdsServiceEndpointRepository.save(cdsServiceEndpoint)).thenReturn(cdsServiceEndpoint);
        assertEquals(cdsServiceEndpoint, cdsServiceEndpointService.save(cdsServiceEndpoint));
    }

    @Test
    public void deleteWithIdTest() {
        cdsServiceEndpointService.delete(1);
        verify(cdsServiceEndpointRepository).delete(1);
    }

    @Test
    public void deleteWithCdsServiceEndpointTest() {
        when(cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId())).thenReturn(cdsHooks);
        when(launchScenarioService.findByCdsHookIdAndSandboxId(cdsHook.getId(), cdsServiceEndpoint.getSandbox().getSandboxId())).thenReturn(launchScenarios);
        when(userLaunchService.findByLaunchScenarioId(1)).thenReturn(userLaunches);
        cdsServiceEndpointService.delete(cdsServiceEndpoint);
        verify(userLaunchService).delete(1);
        verify(launchScenarioService).delete(1);
    }

    @Test
    public void createTest() {
        when(cdsServiceEndpointService.findByCdsServiceEndpointUrlAndSandboxId(anyString(), anyString())).thenReturn(null);
        when(cdsServiceEndpointService.save(cdsServiceEndpoint)).thenReturn(cdsServiceEndpoint);
        assertEquals(cdsServiceEndpoint, cdsServiceEndpointService.create(cdsServiceEndpoint, sandbox));
    }

    @Test
    public void createTestIfCdsServiceEndpointExists() {
        when(cdsServiceEndpointService.findByCdsServiceEndpointUrlAndSandboxId(anyString(), anyString())).thenReturn(cdsServiceEndpoint);
        when(cdsServiceEndpointService.getById(cdsServiceEndpoint.getId())).thenReturn(cdsServiceEndpoint);
        when(cdsServiceEndpointService.save(cdsServiceEndpoint)).thenReturn(cdsServiceEndpoint);
        assertEquals(cdsServiceEndpoint, cdsServiceEndpointService.create(cdsServiceEndpoint, sandbox));
    }

    @Test
    public void updateTest() {
        when(cdsServiceEndpointService.getById(cdsServiceEndpoint.getId())).thenReturn(cdsServiceEndpoint);
        when(cdsHookService.findByCdsServiceEndpointId(1)).thenReturn(cdsHooks);
        when(cdsServiceEndpointService.save(cdsServiceEndpoint)).thenReturn(cdsServiceEndpoint);
        assertEquals(cdsServiceEndpoint, cdsServiceEndpointService.update(cdsServiceEndpoint));
    }

    @Test
    public void updateSameCdsHookTest() {
        when(cdsServiceEndpointService.getById(cdsServiceEndpoint.getId())).thenReturn(cdsServiceEndpoint);
        when(cdsHookService.findByCdsServiceEndpointId(1)).thenReturn(existingCdsHooks);
        cdsServiceEndpointService.update(cdsServiceEndpoint);
        verify(cdsHookService).delete(existingCdsHook);
    }

    @Test
    public void getByIdTest() {
        when(cdsServiceEndpointRepository.findOne(cdsServiceEndpoint.getId())).thenReturn(cdsServiceEndpoint);
        assertEquals(cdsServiceEndpoint, cdsServiceEndpointService.getById(cdsServiceEndpoint.getId()));
    }

    @Test
    public void findBySandboxIdTest() {
        when(cdsServiceEndpointRepository.findBySandboxId(cdsServiceEndpoint.getSandbox().getSandboxId())).thenReturn(cdsServiceEndpoints);
        assertEquals(cdsServiceEndpoints, cdsServiceEndpointService.findBySandboxId(cdsServiceEndpoint.getSandbox().getSandboxId()));
    }

    @Test
    public void findBySandboxIdAndCreatedByOrVisibilityTest() {
        when(cdsServiceEndpointRepository.findBySandboxIdAndCreatedByOrVisibility(cdsServiceEndpoint.getSandbox().getSandboxId(), cdsServiceEndpoint.getCreatedBy().getSbmUserId(), Visibility.PUBLIC)).thenReturn(cdsServiceEndpoints);
        List<CdsServiceEndpoint> returnedCdsServiceEndpoints = cdsServiceEndpointService.findBySandboxIdAndCreatedByOrVisibility(cdsServiceEndpoint.getSandbox().getSandboxId(), cdsServiceEndpoint.getCreatedBy().getSbmUserId(), Visibility.PUBLIC);
        assertEquals(cdsServiceEndpoints, returnedCdsServiceEndpoints);
    }

    @Test
    public void findBySandboxIdAndCreatedByTest() {
        when(cdsServiceEndpointRepository.findBySandboxIdAndCreatedBy(cdsServiceEndpoint.getSandbox().getSandboxId(), cdsServiceEndpoint.getCreatedBy().getSbmUserId())).thenReturn(cdsServiceEndpoints);
        List<CdsServiceEndpoint> returnedCdsServiceEndpoints = cdsServiceEndpointService.findBySandboxIdAndCreatedBy(cdsServiceEndpoint.getSandbox().getSandboxId(), cdsServiceEndpoint.getCreatedBy().getSbmUserId());
        assertEquals(cdsServiceEndpoints, returnedCdsServiceEndpoints);
    }

    @Test
    public void findByCdsServiceEndpointUrlAndSandboxIdTest() {
        when(cdsServiceEndpointRepository.findByCdsServiceEndpointUrlAndSandboxId(cdsServiceEndpoint.getUrl(), cdsServiceEndpoint.getSandbox().getSandboxId())).thenReturn(cdsServiceEndpoint);
        CdsServiceEndpoint returnedCdsServiceEndpoint = cdsServiceEndpointService.findByCdsServiceEndpointUrlAndSandboxId(cdsServiceEndpoint.getUrl(), cdsServiceEndpoint.getSandbox().getSandboxId());
        assertEquals(cdsServiceEndpoint, returnedCdsServiceEndpoint);
    }
}
