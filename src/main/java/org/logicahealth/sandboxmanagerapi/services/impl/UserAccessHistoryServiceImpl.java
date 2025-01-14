package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.model.UserAccessHistory;
import org.logicahealth.sandboxmanagerapi.repositories.UserAccessHistoryRepository;
import org.logicahealth.sandboxmanagerapi.services.UserAccessHistoryService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserAccessHistoryServiceImpl implements UserAccessHistoryService {
    private static Logger LOGGER = LoggerFactory.getLogger(UserAccessHistoryServiceImpl.class.getName());

    private UserAccessHistoryRepository userAccessHistoryRepository;

    public UserAccessHistoryServiceImpl(UserAccessHistoryRepository userAccessHistoryRepository) {
        this.userAccessHistoryRepository = userAccessHistoryRepository;
    }

    public Timestamp getLatestUserAccessHistoryInstance(Sandbox sandbox, User user) {
        
        LOGGER.info("getLatestUserAccessHistoryInstance");

        List<UserAccessHistory> userAccessHistories = userAccessHistoryRepository.findBySbmUserIdAndSandboxId(user.getSbmUserId(), sandbox.getSandboxId());
        if (userAccessHistories.size() > 0) {

            LOGGER.debug("getLatestUserAccessHistoryInstance: "
            +"Parameters: sandbox = "+sandbox+", user = "+user
            +"; Return value = "+userAccessHistories.get(0).getAccessTimestamp());

            return userAccessHistories.get(0).getAccessTimestamp();
        }
        
        LOGGER.debug("getLatestUserAccessHistoryInstance: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = null");

        return null;
    }

    public List<UserAccessHistory> getLatestUserAccessHistoryInstancesWithSandbox(Sandbox sandbox) {
        
        LOGGER.info("getLatestUserAccessHistoryInstancesWithSandbox");

        LOGGER.debug("getLatestUserAccessHistoryInstancesWithSandbox: "
        +"Parameters: sandbox = "+sandbox
        +"; Return value = "+userAccessHistoryRepository.findBySandboxId(sandbox.getSandboxId()));

        return userAccessHistoryRepository.findBySandboxId(sandbox.getSandboxId());
    }

    public List<UserAccessHistory> getLatestUserAccessHistoryInstancesWithSbmUser(User user) {

        LOGGER.info("getLatestUserAccessHistoryInstancesWithSbmUser");

        LOGGER.debug("getLatestUserAccessHistoryInstancesWithSbmUser: "
        +"Parameters: user = "+user
        +"; Return value = "+userAccessHistoryRepository.findBySbmUserId(user.getSbmUserId()));

        return userAccessHistoryRepository.findBySbmUserId(user.getSbmUserId());
    }

    public void saveUserAccessInstance(Sandbox sandbox, User user) {

        LOGGER.info("saveUserAccessInstance");

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

        LOGGER.debug("saveUserAccessInstance: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; No return value");

    }

    public void deleteUserAccessInstancesForSandbox(Sandbox sandbox) {
        
        LOGGER.info("deleteUserAccessInstancesForSandbox");

        List<UserAccessHistory> userAccessHistories = userAccessHistoryRepository.findBySandboxId(sandbox.getSandboxId());
        for (UserAccessHistory userAccessHistory: userAccessHistories) {
            userAccessHistoryRepository.delete(userAccessHistory);
        }

        LOGGER.debug("deleteUserAccessInstancesForSandbox: "
        +"Parameters: sandbox = "+sandbox+"; No return value");

    }

    public void deleteUserAccessInstancesForUser(User user) {
        
        LOGGER.info("deleteUserAccessInstancesForUser");

        List<UserAccessHistory> userAccessHistories = userAccessHistoryRepository.findBySbmUserId(user.getSbmUserId());
        for (UserAccessHistory userAccessHistory: userAccessHistories) {
            userAccessHistoryRepository.delete(userAccessHistory);
        }

        LOGGER.debug("deleteUserAccessInstancesForUser: "
        +"Parameters: user = "+user+"; No return value");

    }
}
