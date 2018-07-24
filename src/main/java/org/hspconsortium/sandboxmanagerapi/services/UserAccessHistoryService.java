package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.UserAccessHistory;

import java.sql.Timestamp;
import java.util.List;

public interface UserAccessHistoryService {

    Timestamp getLatestUserAccessHistoryInsance(Sandbox sandbox, User user);

    List<UserAccessHistory> getLatestUserAccessHistoryInsancesWithSandbox(Sandbox sandbox);

    List<UserAccessHistory> getLatestUserAccessHistoryInsancesWithSbmUser(User user);

    void saveUserAccessInstance(Sandbox sandbox, User user);

    void deleteUserAccessInstancesForSandbox(Sandbox sandbox);

    void deleteUserAccessInstancesForUser(User user);

}
