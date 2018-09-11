package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.UserAccessHistory;

import java.sql.Timestamp;
import java.util.List;

public interface UserAccessHistoryService {

    Timestamp getLatestUserAccessHistoryInstance(Sandbox sandbox, User user);

    List<UserAccessHistory> getLatestUserAccessHistoryInstancesWithSandbox(Sandbox sandbox);

    List<UserAccessHistory> getLatestUserAccessHistoryInstancesWithSbmUser(User user);

    void saveUserAccessInstance(Sandbox sandbox, User user);

    void deleteUserAccessInstancesForSandbox(Sandbox sandbox);

    void deleteUserAccessInstancesForUser(User user);

}
