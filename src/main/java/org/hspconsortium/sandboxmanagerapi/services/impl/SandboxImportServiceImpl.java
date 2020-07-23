package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.SandboxImport;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxImportRepository;
import org.hspconsortium.sandboxmanagerapi.services.SandboxImportService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class SandboxImportServiceImpl implements SandboxImportService {

    private final SandboxImportRepository repository;

    @Inject
    public SandboxImportServiceImpl(final SandboxImportRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SandboxImport save(final SandboxImport sandboxImport) {
        return repository.save(sandboxImport);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(final SandboxImport sandboxImport) {
        delete(sandboxImport.getId());
    }

}
