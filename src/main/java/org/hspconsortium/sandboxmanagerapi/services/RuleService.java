package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.App;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;

import java.util.List;

public interface RuleService {

    Boolean checkIfUserCanCreateSandbox(User user, String bearerToken);

    Boolean checkIfUserCanCreateApp(Sandbox sandbox);

    Boolean checkIfUserCanBeAdded(String sandBoxId);

//    Boolean checkIfUserHasStorage(Sandbox sandbox);

    Boolean checkIfUserCanPerformTransaction(Sandbox sandbox, String operation, String bearerToken);

}
