package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.UserPersonaRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.UserPersonaServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserPersonaServiceTest {

    private UserPersonaRepository repository = mock(UserPersonaRepository.class);

    private UserPersonaServiceImpl userPersonaService = new UserPersonaServiceImpl(repository);

    private UserPersona userPersona;
    private List<UserPersona> userPersonas;
    private User user;
    private Sandbox sandbox;

    @Before
    public void setup() {
        userPersona = new UserPersona();
        userPersona.setId(1);
        userPersona.setPersonaUserId("userPersona");
        userPersonas = new ArrayList<>();
        userPersonas.add(userPersona);
        user = new User();
        user.setSbmUserId("userId");
        sandbox = new Sandbox();
        sandbox.setSandboxId("sandboxId");
    }

    @Test
    public void saveTest() {
        when(repository.save(userPersona)).thenReturn(userPersona);
        UserPersona returnedUserPersona = userPersonaService.save(userPersona);
        assertEquals(userPersona, returnedUserPersona);
    }

    @Test
    public void getByIdTest() {
        when(repository.findOne(userPersona.getId())).thenReturn(userPersona);
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
        userPersonaService.delete(userPersona.getId());
        verify(repository).delete(userPersona.getId());
    }

    @Test
    public void deleteTestByObject() {
        userPersonaService.delete(userPersona);
        verify(repository).delete(userPersona.getId());
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
