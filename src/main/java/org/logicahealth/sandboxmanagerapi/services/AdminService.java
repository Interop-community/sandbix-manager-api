package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.User;

import java.util.HashMap;
import java.util.Set;

/**
 */
public interface AdminService {

    HashMap<String, Object> syncSandboxManagerandReferenceApi(Boolean fix, String request);

    Set<String> deleteUnusedSandboxes(User user, String bearerToken);
}
