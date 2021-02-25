package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.SandboxImport;

public interface SandboxImportService {

    SandboxImport save(final SandboxImport sandboxImport);

    void delete(final int id);

    void delete(SandboxImport sandboxImport);

}
