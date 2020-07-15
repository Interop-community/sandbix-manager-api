package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxCreationStatus;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxRepository;
import org.hspconsortium.sandboxmanagerapi.services.SandboxSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SandboxSaveServiceImpl implements SandboxSaveService {

    private final SandboxRepository repository;

    @Autowired
    public SandboxSaveServiceImpl(SandboxRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void saveSandbox(Sandbox sandbox, SandboxCreationStatus sandboxCreationStatus) {
        sandbox = repository.findBySandboxId(sandbox.getSandboxId());
        sandbox.setCreationStatus(sandboxCreationStatus);
        this.repository.save(sandbox);
    }
}
