package org.hspconsortium.sandboxmanagerapi.services;

import java.util.HashMap;

/**
 */
public interface AdminService {

    HashMap<String, Object> syncSandboxManagerandReferenceApi(Boolean fix, String request);

}
