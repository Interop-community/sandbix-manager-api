package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxActivityLogRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.SandboxActivityLogServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SandboxActivityLogServiceTest {

    private SandboxActivityLogRepository repository = mock(SandboxActivityLogRepository.class);

    private SandboxActivityLogServiceImpl sandboxActivityLogService = new SandboxActivityLogServiceImpl(repository);

    private SandboxActivityLog sandboxActivityLog;
    private Sandbox sandbox;
    private User user;
    private List<SandboxActivityLog> sandboxActivityLogs;

    @Before
    public void setup() {
        sandboxActivityLog = new SandboxActivityLog();
        sandboxActivityLog.setId(1);
        sandbox = new Sandbox();
        sandbox.setSandboxId("sandboxId");
        user = new User();
        user.setId(1);
        user.setSbmUserId("userId");
        sandboxActivityLogs = new ArrayList<>();
        sandboxActivityLogs.add(sandboxActivityLog);

        when(repository.findByUserId(user.getId())).thenReturn(sandboxActivityLogs);
    }

    @Test
    public void saveTest() {
        when(repository.save(sandboxActivityLog)).thenReturn(sandboxActivityLog);
        SandboxActivityLog returnedSandboxActivityLog = sandboxActivityLogService.save(sandboxActivityLog);
        assertEquals(sandboxActivityLog, returnedSandboxActivityLog);
    }

    @Test
    public void deleteTest() {
        sandboxActivityLogService.delete(sandboxActivityLog);
        verify(repository).delete(sandboxActivityLog.getId());
    }

    @Test
    public void sandboxCreateTest() {
        sandboxActivityLogService.sandboxCreate(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxLoginTest() {
        sandboxActivityLogService.sandboxLogin(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxDeleteTest() {
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandboxActivityLogs);
        sandboxActivityLogService.sandboxDelete(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
        verify(repository).delete(user.getId());
    }

    @Test
    public void sandboxUserInviteAcceptedTest() {
        sandboxActivityLogService.sandboxUserInviteAccepted(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxUserInviteRevokedTest() {
        sandboxActivityLogService.sandboxUserInviteRevoked(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxUserInviteRejectedTest() {
        sandboxActivityLogService.sandboxUserInviteRejected(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxUserRemovedTest() {
        sandboxActivityLogService.sandboxUserRemoved(sandbox, user, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxUserInvitedTest() {
        sandboxActivityLogService.sandboxUserInvited(sandbox, user, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxOpenEndpointTest() {
        sandboxActivityLogService.sandboxOpenEndpoint(sandbox, user, true);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxUserAddedTest() {
        sandboxActivityLogService.sandboxUserAdded(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxUserRoleChangeTest() {
        sandboxActivityLogService.sandboxUserRoleChange(sandbox, user, Role.ADMIN, true);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxImportTest() {
        sandboxActivityLogService.sandboxImport(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void sandboxResetTest() {
        sandboxActivityLogService.sandboxReset(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void systemUserCreatedTest() {
        sandboxActivityLogService.systemUserCreated(sandbox, user);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void systemUserRoleChangeTest() {
        sandboxActivityLogService.systemUserRoleChange(user, SystemRole.ADMIN, true);
        verify(repository).save(any(SandboxActivityLog.class));
    }

    @Test
    public void userDeleteTest() {
        sandboxActivityLogService.userDelete(user);
        verify(repository).save(any(SandboxActivityLog.class));
        verify(repository).delete(user.getId());
    }

    @Test
    public void findBySandboxIdTest() {
        when(repository.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandboxActivityLogs);
        List<SandboxActivityLog> returnedSandboxActivityLogs = sandboxActivityLogService.findBySandboxId(sandbox.getSandboxId());
        assertEquals(sandboxActivityLogs, returnedSandboxActivityLogs);
    }

    @Test
    public void findByUserSbmUserIdTest() {
        when(repository.findByUserSbmUserId(user.getSbmUserId())).thenReturn(sandboxActivityLogs);
        List<SandboxActivityLog> returnedSandboxActivityLogs = sandboxActivityLogService.findByUserSbmUserId(user.getSbmUserId());
        assertEquals(sandboxActivityLogs, returnedSandboxActivityLogs);
    }

    @Test
    public void findByUserIdTest() {
        when(repository.findByUserId(user.getId())).thenReturn(sandboxActivityLogs);
        List<SandboxActivityLog> returnedSandboxActivityLogs = sandboxActivityLogService.findByUserId(user.getId());
        assertEquals(sandboxActivityLogs, returnedSandboxActivityLogs);
    }

    @Test
    public void findBySandboxActivityTest() {
        when(repository.findBySandboxActivity(SandboxActivity.LOGGED_IN)).thenReturn(sandboxActivityLogs);
        List<SandboxActivityLog> returnedSandboxActivityLogs = sandboxActivityLogService.findBySandboxActivity(SandboxActivity.LOGGED_IN);
        assertEquals(sandboxActivityLogs, returnedSandboxActivityLogs);
    }

    @Test
    public void intervalActiveTest() {
        when(repository.intervalActive(any())).thenReturn("string");
        String returnedString = sandboxActivityLogService.intervalActive(new Timestamp(new Date().getTime()));
        assertEquals("string", returnedString);
    }

}
