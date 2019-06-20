package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.CustomHook;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.repositories.CustomHookRepository;
import org.hspconsortium.sandboxmanagerapi.services.CustomHookService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class CustomHookServiceImpl implements CustomHookService {
    private CustomHookRepository repository;

    @Inject
    public CustomHookServiceImpl(final CustomHookRepository customHookRepository) {
        this.repository = customHookRepository;
    }

    @Override
    public Iterable<CustomHook> createCustomHooks(List<CustomHook> customHooks) {
        return repository.save(customHooks);
    }

    @Override
    public void delete(int id) {

    }

    @Override
    public void delete(CustomHook customHook) {

    }

    @Override
    public CustomHook getById(int id) {
        return null;
    }

    @Override
    public List<CustomHook> findBySandboxId(String sandboxId) {
        return repository.findBySandboxId(sandboxId);
    }
}
