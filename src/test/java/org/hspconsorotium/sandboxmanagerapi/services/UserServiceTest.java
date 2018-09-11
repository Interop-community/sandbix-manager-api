package org.hspconsorotium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.TermsOfUse;
import org.hspconsortium.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.repositories.UserRepository;
import org.hspconsortium.sandboxmanagerapi.services.TermsOfUseAcceptanceService;
import org.hspconsortium.sandboxmanagerapi.services.TermsOfUseService;
import org.hspconsortium.sandboxmanagerapi.services.UserAccessHistoryService;
import org.hspconsortium.sandboxmanagerapi.services.impl.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository repository = mock(UserRepository.class);
    private TermsOfUseService termsOfUseService = mock(TermsOfUseService.class);
    private TermsOfUseAcceptanceService termsOfUseAcceptanceService = mock(TermsOfUseAcceptanceService.class);
    private UserAccessHistoryService userAccessHistoryService = mock(UserAccessHistoryService.class);

    private UserServiceImpl userService = new UserServiceImpl(repository);

    private User user;
    private List<User> users;
    private TermsOfUse termsOfUse;
    private Sandbox sandbox;

    @Before
    public void setup() {
        userService.setTermsOfUseAcceptanceService(termsOfUseAcceptanceService);
        userService.setTermsOfUseService(termsOfUseService);
        userService.setUserAccessHistoryService(userAccessHistoryService);

        user = new User();
        user.setSbmUserId("userId");
        user.setEmail("user@email.com");
        user.setId(1);
        users = new ArrayList<>();
        users.add(user);
        termsOfUse = new TermsOfUse();
        termsOfUse.setId(1);
        sandbox = new Sandbox();
    }

    @Test
    public void saveTest() {
        when(repository.save(user)).thenReturn(user);
        User returnedUser = userService.save(user);
        assertEquals(user, returnedUser);
    }

    @Test
    public void deleteTest() {
        userService.delete(user);
        verify(userAccessHistoryService).deleteUserAccessInstancesForUser(user);
        verify(repository).delete(user);
    }

    @Test
    public void findAllUsersTest() {
        when(repository.findAll()).thenReturn(users);
        Iterable<User> returnedUsers = userService.findAllUsers();
        assertEquals(users, returnedUsers);
    }

    @Test
    public void findBySbmUserIdTest() {
        when(repository.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        User returnedUser = userService.findBySbmUserId(user.getSbmUserId());
        assertEquals(user, returnedUser);
    }

    @Test
    public void findBySbmUserIdTestReturnsNull() {
        when(repository.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        User returnedUser = userService.findBySbmUserId(user.getSbmUserId());
        assertEquals(null, returnedUser);
    }

    @Test
    public void findByUserEmailTest() {
        when(repository.findBySbmUserId(user.getEmail())).thenReturn(user);
        User returnedUser = userService.findBySbmUserId(user.getEmail());
        assertEquals(user, returnedUser);
    }

    @Test
    public void findByUserEmailTestReturnsNull() {
        when(repository.findBySbmUserId(user.getEmail())).thenReturn(null);
        User returnedUser = userService.findBySbmUserId(user.getEmail());
        assertEquals(null, returnedUser);
    }

    @Test
    public void findByIdTest() {
        when(repository.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        User returnedUser = userService.findBySbmUserId(user.getSbmUserId());
        assertEquals(user, returnedUser);
    }

    @Test
    public void findByIdTestReturnsNull() {
        when(repository.findBySbmUserId(user.getSbmUserId())).thenReturn(null);
        User returnedUser = userService.findBySbmUserId(user.getSbmUserId());
        assertEquals(null, returnedUser);
    }

    @Test
    public void findByIdTestAcceptedTermsOfUse() {
        User user = spy(User.class);
        List<TermsOfUseAcceptance> termsOfUseAcceptances = new ArrayList<>();
        TermsOfUseAcceptance termsOfUseAcceptance = new TermsOfUseAcceptance();
        termsOfUseAcceptance.setTermsOfUse(termsOfUse);
        termsOfUseAcceptances.add(termsOfUseAcceptance);
        user.setTermsOfUseAcceptances(termsOfUseAcceptances);
        when(repository.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(termsOfUseService.mostRecent()).thenReturn(termsOfUse);
        userService.findBySbmUserId(user.getSbmUserId());
        verify(user).setHasAcceptedLatestTermsOfUse(false);
        verify(user).setHasAcceptedLatestTermsOfUse(true);
    }

    @Test
    public void findByIdTestAcceptedTermsOfUseTermsNotFound() {
        User user = spy(User.class);
        when(repository.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(termsOfUseService.mostRecent()).thenReturn(null);
        userService.findBySbmUserId(user.getSbmUserId());
        verify(user).setHasAcceptedLatestTermsOfUse(true);
    }

//    @Test
//    public void findByIdTestAcceptedTermsOfUseTermsNotFound() {
//        User user = spy(User.class);
//        when(repository.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
//        when(termsOfUseService.mostRecent()).thenReturn(null);
//        userService.findBySbmUserId(user.getSbmUserId());
//        verify(user).setHasAcceptedLatestTermsOfUse(true);
//    }

    @Test
    public void fullCountTest() {
        when(repository.fullCount()).thenReturn("1");
        String count = userService.fullCount();
        assertEquals("1", count);
    }

    @Test
    public void intervalCountTest() {
        Timestamp timestamp = new Timestamp(new java.util.Date().getTime());
        when(repository.intervalCount(timestamp)).thenReturn("interval");
        String interval = userService.intervalCount(timestamp);
        assertEquals("interval", interval);
    }

    @Test
    public void removeSandboxTest() {
        List<Sandbox> sandboxes = spy(ArrayList.class);
        sandboxes.add(sandbox);
        User user = spy(User.class);
        when(user.getSandboxes()).thenReturn(sandboxes);
        userService.removeSandbox(sandbox, user);
        verify(sandboxes).remove(sandbox);
        verify(user).setSandboxes(any());
    }

    @Test
    public void addSandboxTest() {
        List<Sandbox> sandboxes = spy(ArrayList.class);
        User user = spy(User.class);
        when(user.getSandboxes()).thenReturn(sandboxes);
        userService.addSandbox(sandbox, user);
        verify(sandboxes).add(sandbox);
        verify(user).setSandboxes(any());
    }

    @Test
    public void addSandboxTestAlreadyHasSandbox() {
        List<Sandbox> sandboxes = spy(ArrayList.class);
        User user = spy(User.class);
        sandboxes.add(sandbox);
        when(user.getSandboxes()).thenReturn(sandboxes);
        userService.addSandbox(sandbox, user);
        verify(user, times(0)).setSandboxes(any());
    }

    @Test
    public void hasSandboxTest() {
        List<Sandbox> sandboxes = new ArrayList<>();
        sandboxes.add(sandbox);
        user.setSandboxes(sandboxes);
        Boolean returnedBool = userService.hasSandbox(sandbox, user);
        assertEquals(true, returnedBool);
    }

    @Test
    public void hasSandboxTestNoSandbox() {
        user.setSandboxes(new ArrayList<>());
        Boolean returnedBool = userService.hasSandbox(sandbox, user);
        assertEquals(false, returnedBool);
    }

    @Test
    public void acceptTermsOfUseTest() {
        TermsOfUseAcceptance termsOfUseAcceptance = new TermsOfUseAcceptance();
        termsOfUseAcceptance.setId(1);
        User user = spy(User.class);
        user.setTermsOfUseAcceptances(new ArrayList<>());
        userService.acceptTermsOfUse(user, "1");
        verify(termsOfUseService).getById(Integer.parseInt("1"));
        verify(termsOfUseAcceptanceService).save(any());
        verify(user, times(2)).setTermsOfUseAcceptances(any());
    }
}
