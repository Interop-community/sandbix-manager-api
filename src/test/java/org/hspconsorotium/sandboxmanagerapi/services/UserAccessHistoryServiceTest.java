package org.hspconsorotium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.UserAccessHistoryRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.UserAccessHistoryServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UserAccessHistoryServiceTest {

    private UserAccessHistoryRepository userAccessHistoryRepository = mock(UserAccessHistoryRepository.class);

    private UserAccessHistoryServiceImpl userAccessHistoryService = new UserAccessHistoryServiceImpl(userAccessHistoryRepository);

    private UserAccessHistory userAccessHistory;
    private List<UserAccessHistory> userAccessHistoryList;
    private Sandbox sandbox;
    private User user;
    private Timestamp timestamp;

    @Before
    public void setup() {
        timestamp = new Timestamp(new java.util.Date().getTime());
        userAccessHistory = new UserAccessHistory();
        userAccessHistory.setId(1);
        userAccessHistory.setAccessTimestamp(timestamp);
        userAccessHistoryList = new ArrayList<>();
        userAccessHistoryList.add(userAccessHistory);
        sandbox = new Sandbox();
        sandbox.setSandboxId("sandboxId");
        user = new User();
        user.setSbmUserId("userId");
    }

    @Test
    public void getLatestUserAccessHistoryInstanceTest() {
        when(userAccessHistoryRepository.findBySbmUserIdAndSandboxId(user.getSbmUserId(), sandbox.getSandboxId())).thenReturn(userAccessHistoryList);
        Timestamp returnedTimestamp = userAccessHistoryService.getLatestUserAccessHistoryInstance(sandbox, user);
        assertEquals(timestamp, returnedTimestamp);
    }

    @Test
    public void getLatestUserAccessHistoryInstanceTestReturnsNull() {
        when(userAccessHistoryRepository.findBySbmUserIdAndSandboxId(user.getSbmUserId(), sandbox.getSandboxId())).thenReturn(new ArrayList<>());
        Timestamp returnedTimestamp = userAccessHistoryService.getLatestUserAccessHistoryInstance(sandbox, user);
        assertEquals(null, returnedTimestamp);
    }

    @Test
    public void getLatestUserAccessHistoryInstancesWithSandboxTest() {
        when(userAccessHistoryRepository.findBySandboxId(sandbox.getSandboxId())).thenReturn(userAccessHistoryList);
        List<UserAccessHistory> returnedUserAccessHistoryList = userAccessHistoryService.getLatestUserAccessHistoryInstancesWithSandbox(sandbox);
        assertEquals(userAccessHistoryList, returnedUserAccessHistoryList);
    }

    @Test
    public void getLatestUserAccessHistoryInstancesWithSbmUserTest() {
        when(userAccessHistoryRepository.findBySbmUserId(user.getSbmUserId())).thenReturn(userAccessHistoryList);
        List<UserAccessHistory> returnedUserAccessHistoryList = userAccessHistoryService.getLatestUserAccessHistoryInstancesWithSbmUser(user);
        assertEquals(userAccessHistoryList, returnedUserAccessHistoryList);
    }

    @Test
    public void saveUserAccessInstanceTest() {
        when(userAccessHistoryRepository.findBySbmUserIdAndSandboxId(user.getSbmUserId(), sandbox.getSandboxId())).thenReturn(new ArrayList<>());
        userAccessHistoryService.saveUserAccessInstance(sandbox, user);
        verify(userAccessHistoryRepository, times(0)).save(userAccessHistory);
    }

    @Test
    public void saveUserAccessInstanceTestUpdate() {
        when(userAccessHistoryRepository.findBySbmUserIdAndSandboxId(user.getSbmUserId(), sandbox.getSandboxId())).thenReturn(userAccessHistoryList);
        userAccessHistoryService.saveUserAccessInstance(sandbox, user);
        verify(userAccessHistoryRepository).save(userAccessHistory);
    }

    @Test
    public void deleteUserAccessInstancesForSandboxTest() {
        when(userAccessHistoryRepository.findBySandboxId(sandbox.getSandboxId())).thenReturn(userAccessHistoryList);
        userAccessHistoryService.deleteUserAccessInstancesForSandbox(sandbox);
        verify(userAccessHistoryRepository).delete(userAccessHistory.getId());
    }

    @Test
    public void deleteUserAccessInstancesForUserTest() {
        when(userAccessHistoryRepository.findBySbmUserId(user.getSbmUserId())).thenReturn(userAccessHistoryList);
        userAccessHistoryService.deleteUserAccessInstancesForUser(user);
        verify(userAccessHistoryRepository).delete(userAccessHistory.getId());
    }
}
