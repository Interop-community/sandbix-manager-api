package org.logicahealth.sandboxmanagerapi.services;

import org.junit.Before;
import org.junit.Test;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.repositories.UserPersonaRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.UserPersonaServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UserPersonaServiceTest {

    private UserPersonaRepository repository = mock(UserPersonaRepository.class);
    private LaunchScenarioService launchScenarioService = mock(LaunchScenarioService.class);

    private UserPersonaServiceImpl userPersonaService = new UserPersonaServiceImpl(repository);

    private UserPersona userPersona;
    private List<UserPersona> userPersonas;
    private User user;
    private Sandbox sandbox;
    private List<LaunchScenario> launchScenarios;
    private LaunchScenario launchScenario;

    @Before
    public void setup() {
        sandbox = new Sandbox();
        sandbox.setSandboxId("sandboxId");
        userPersonaService.setLaunchScenarioService(launchScenarioService);
        userPersona = new UserPersona();
        userPersona.setId(1);
        userPersona.setPersonaUserId("userPersona");
        userPersona.setSandbox(sandbox);
        userPersonas = new ArrayList<>();
        userPersonas.add(userPersona);
        user = new User();
        user.setSbmUserId("userId");
        launchScenarios = new ArrayList<>();
        launchScenario = new LaunchScenario();
    }

    @Test
    public void saveTest() {
        when(repository.save(userPersona)).thenReturn(userPersona);
        UserPersona returnedUserPersona = userPersonaService.save(userPersona);
        assertEquals(userPersona, returnedUserPersona);
    }

    @Test
    public void getByIdTest() {
        when(repository.findById(userPersona.getId())).thenReturn(of(userPersona));
        UserPersona returnedUserPersona = userPersonaService.getById(userPersona.getId());
        assertEquals(userPersona, returnedUserPersona);
    }

    @Test
    public void findByPersonaUserIdTest() {
        when(repository.findByPersonaUserId(userPersona.getPersonaUserId())).thenReturn(userPersona);
        UserPersona returnedUserPersona = userPersonaService.findByPersonaUserId(userPersona.getPersonaUserId());
        assertEquals(userPersona, returnedUserPersona);
    }

    @Test
    public void findByPersonaUserIdAndSandboxIdTest() {
        when(repository.findByPersonaUserIdAndSandboxId(userPersona.getPersonaUserId(), sandbox.getSandboxId())).thenReturn(userPersona);
        UserPersona returnedUserPersona = userPersonaService.findByPersonaUserIdAndSandboxId(userPersona.getPersonaUserId(), sandbox.getSandboxId());
        assertEquals(userPersona, returnedUserPersona);
    }

    @Test
    public void findBySandboxIdAndCreatedByOrVisibilityTest() {
        when(repository.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), user.getSbmUserId(), Visibility.PUBLIC)).thenReturn(userPersonas);
        List<UserPersona> returnedUserPersonas = userPersonaService.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), user.getSbmUserId(), Visibility.PUBLIC);
        assertEquals(userPersonas, returnedUserPersonas);
    }

    @Test
    public void findDefaultBySandboxIdTest() {
//        when(repository.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), user.getSbmUserId(), Visibility.PUBLIC)).thenReturn(userPersonas);
//        List<UserPersona> returnedUserPersonas = userPersonaService.findBySandboxIdAndCreatedByOrVisibility(sandbox.getSandboxId(), user.getSbmUserId(), Visibility.PUBLIC);
//        assertEquals(userPersonas, returnedUserPersonas);
    }

    @Test
    public void findBySandboxIdAndCreatedByTest() {
        when(repository.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId())).thenReturn(userPersonas);
        List<UserPersona> returnedUserPersonas = userPersonaService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId());
        assertEquals(userPersonas, returnedUserPersonas);
    }

    @Test
    public void findBySandboxIdest() {
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(userPersonas);
        List<UserPersona> returnedUserPersonas = userPersonaService.findBySandboxId(sandbox.getSandboxId());
        assertEquals(userPersonas, returnedUserPersonas);
    }

    @Test
    public void deleteTest() {
        userPersonaService.delete(userPersona);
        verify(repository).deleteById(userPersona.getId());
    }

    @Test
    public void deleteTestByObject() {
        userPersonaService.delete(userPersona);
        verify(repository).deleteById(userPersona.getId());
    }

    @Test(expected = RuntimeException.class)
    public void deleteTestByObjectError() {
        launchScenario.setUserPersona(userPersona);
        launchScenarios.add(launchScenario);
        when(launchScenarioService.findByUserPersonaIdAndSandboxId(userPersona.getId(), sandbox.getSandboxId())).thenReturn(launchScenarios);
        userPersonaService.delete(userPersona);
        verify(repository).deleteById(userPersona.getId());
    }

    @Test
    public void createTest() {
        when(repository.save(userPersona)).thenReturn(userPersona);
        UserPersona returnedUserPersona = userPersonaService.save(userPersona);
        assertEquals(userPersona, returnedUserPersona);
    }

    @Test
    public void updateTest() {
        when(repository.save(userPersona)).thenReturn(userPersona);
        UserPersona returnedUserPersona = userPersonaService.save(userPersona);
        assertEquals(userPersona, returnedUserPersona);
    }

    @Test
    public void createOrUpdateTest() {
        when(repository.save(userPersona)).thenReturn(userPersona);
        UserPersona returnedUserPersona = userPersonaService.save(userPersona);
        assertEquals(userPersona, returnedUserPersona);
    }
}
