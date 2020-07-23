package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.UserAccessHistory;
import org.hspconsortium.sandboxmanagerapi.repositories.UserAccessHistoryRepository;
import org.hspconsortium.sandboxmanagerapi.services.UserAccessHistoryService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UserAccessHistoryServiceImpl implements UserAccessHistoryService {

    private UserAccessHistoryRepository userAccessHistoryRepository;

    public UserAccessHistoryServiceImpl(UserAccessHistoryRepository userAccessHistoryRepository) {
        this.userAccessHistoryRepository = userAccessHistoryRepository;
    }

    public Timestamp getLatestUserAccessHistoryInstance(Sandbox sandbox, User user) {
        List<UserAccessHistory> userAccessHistories = userAccessHistoryRepository.findBySbmUserIdAndSandboxId(user.getSbmUserId(), sandbox.getSandboxId());
        if (userAccessHistories.size() > 0) {
            return userAccessHistories.get(0).getAccessTimestamp();
        }
        return null;
    }

    public List<UserAccessHistory> getLatestUserAccessHistoryInstancesWithSandbox(Sandbox sandbox) {
        return userAccessHistoryRepository.findBySandboxId(sandbox.getSandboxId());
    }

    public List<UserAccessHistory> getLatestUserAccessHistoryInstancesWithSbmUser(User user) {
        return userAccessHistoryRepository.findBySbmUserId(user.getSbmUserId());
    }

    public void saveUserAccessInstance(Sandbox sandbox, User user) {
        List<UserAccessHistory> userAccessHistories = userAccessHistoryRepository.findBySbmUserIdAndSandboxId(user.getSbmUserId(), sandbox.getSandboxId());
        if (userAccessHistories.size() > 0) {
            // Update instance
            UserAccessHistory userAccessHistory = userAccessHistories.get(0);
            userAccessHistory.setAccessTimestamp(new Timestamp(System.currentTimeMillis()));
            userAccessHistoryRepository.save(userAccessHistory);
        } else {
            // Create instance
            UserAccessHistory userAccessHistory = new UserAccessHistory();
            userAccessHistory.setSandboxId(sandbox.getSandboxId());
            userAccessHistory.setSbmUserId(user.getSbmUserId());
            userAccessHistory.setAccessTimestamp(new Timestamp(System.currentTimeMillis()));
            userAccessHistoryRepository.save(userAccessHistory);
        }
    }

    public void deleteUserAccessInstancesForSandbox(Sandbox sandbox) {
        List<UserAccessHistory> userAccessHistories = userAccessHistoryRepository.findBySandboxId(sandbox.getSandboxId());
        for (UserAccessHistory userAccessHistory: userAccessHistories) {
            userAccessHistoryRepository.delete(userAccessHistory);
        }
    }

    public void deleteUserAccessInstancesForUser(User user) {
        List<UserAccessHistory> userAccessHistories = userAccessHistoryRepository.findBySbmUserId(user.getSbmUserId());
        for (UserAccessHistory userAccessHistory: userAccessHistories) {
            userAccessHistoryRepository.delete(userAccessHistory);
        }
    }
}
