package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.SandboxImport;

public interface SandboxImportService {

    SandboxImport save(final SandboxImport sandboxImport);

    void delete(final int id);

    void delete(SandboxImport sandboxImport);

}
