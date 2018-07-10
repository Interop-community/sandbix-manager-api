package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;

import java.sql.Timestamp;

public interface UserAccessHistoryService {

    Timestamp getLatestUserAccessHistoryInsance(Sandbox sandbox, User user);

    void saveUserAccessInstance(Sandbox sandbox, User user);

    void deleteUserAccessInstancesForSandbox(Sandbox sandbox);

    void deleteUserAccessInstancesForUser(User user);

}
