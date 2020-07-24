package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.LaunchScenario;
import org.hspconsortium.sandboxmanagerapi.model.UserLaunch;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.repositories.UserLaunchRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.UserLaunchServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserLaunchServiceTest {

    private UserLaunchRepository repository = mock(UserLaunchRepository.class);

    private UserLaunchServiceImpl userLaunchService = new UserLaunchServiceImpl(repository);

    private UserLaunch userLaunch;
    private List<UserLaunch> userLaunches;
    private User user;
    private LaunchScenario launchScenario;

    @Before
    public void setup() {
        userLaunch = new UserLaunch();
        userLaunch.setId(1);
        userLaunches = new ArrayList<>();
        userLaunches.add(userLaunch);
        user = new User();
        user.setId(1);
        user.setSbmUserId("userId");
        launchScenario = new LaunchScenario();
        launchScenario.setId(1);
    }

    @Test
    public void saveTest() {
        when(repository.save(userLaunch)).thenReturn(userLaunch);
        UserLaunch returnedUserLaunch = userLaunchService.save(userLaunch);
        assertEquals(userLaunch, returnedUserLaunch);
    }

    @Test
    public void deleteTest() {
        userLaunchService.delete(userLaunch.getId());
        verify(repository).deleteById(userLaunch.getId());
    }

    @Test
    public void deleteTestWithUserLaunchObject() {
        userLaunchService.delete(userLaunch);
        verify(repository).deleteById(userLaunch.getId());
    }

    @Test
    public void createTest() {
        when(repository.save(userLaunch)).thenReturn(userLaunch);
        UserLaunch returnedUserLaunch = userLaunchService.create(userLaunch);
        assertEquals(userLaunch, returnedUserLaunch);
    }

    @Test
    public void getByIdTest() {
        when(repository.findById(userLaunch.getId())).thenReturn(of(userLaunch));
        UserLaunch returnedUserLaunch = userLaunchService.getById(userLaunch.getId());
        assertEquals(userLaunch, returnedUserLaunch);
    }

    @Test
    public void updateTest() {
        when(repository.findById(userLaunch.getId())).thenReturn(of(userLaunch));
        when(repository.save(userLaunch)).thenReturn(userLaunch);
        UserLaunch returnedUserLaunch = userLaunchService.update(userLaunch);
        assertEquals(userLaunch, returnedUserLaunch);
    }

    @Test
    public void updateTestReturnsNull() {
        when(repository.findById(userLaunch.getId())).thenReturn(Optional.empty());
        UserLaunch returnedUserLaunch = userLaunchService.update(userLaunch);
        assertNull(returnedUserLaunch);
    }

    @Test
    public void findByUserIdAndLaunchScenarioIdTest() {
        when(repository.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId())).thenReturn(userLaunch);
        UserLaunch returnedUserLaunch = userLaunchService.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId());
        assertEquals(userLaunch, returnedUserLaunch);
    }

    @Test
    public void findByUserIdTest() {
        when(repository.findByUserId(user.getSbmUserId())).thenReturn(userLaunches);
        List<UserLaunch> returnedUserLaunches = userLaunchService.findByUserId(user.getSbmUserId());
        assertEquals(userLaunches, returnedUserLaunches);
    }

    @Test
    public void findByLaunchScenarioIdTest() {
        when(repository.findByLaunchScenarioId(launchScenario.getId())).thenReturn(userLaunches);
        List<UserLaunch> returnedUserLaunches = userLaunchService.findByLaunchScenarioId(launchScenario.getId());
        assertEquals(userLaunches, returnedUserLaunches);
    }
}
