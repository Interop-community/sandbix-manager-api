package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Rule;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;

public interface RuleService {

    Boolean checkIfUserCanCreateSandbox(User user, String bearerToken);

    Boolean checkIfUserCanCreateApp(Sandbox sandbox);

    Boolean checkIfUserCanBeAdded(String sandBoxId);

//    Boolean checkIfUserHasStorage(Sandbox sandbox);

    Boolean checkIfUserCanPerformTransaction(Sandbox sandbox, String operation, String bearerToken);

    Rule findRulesByUser(User user);
}
