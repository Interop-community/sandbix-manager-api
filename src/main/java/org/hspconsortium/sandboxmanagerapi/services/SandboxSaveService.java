package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxCreationStatus;

public interface SandboxSaveService {

    void saveSandbox(Sandbox sandbox, SandboxCreationStatus sandboxCreationStatus);

}
