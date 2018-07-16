package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.AuthClient;
import org.hspconsortium.sandboxmanagerapi.repositories.AuthClientRepository;
import org.hspconsortium.sandboxmanagerapi.services.AuthClientService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class AuthClientServiceImpl implements AuthClientService {

    private final AuthClientRepository repository;

    @Inject
    public AuthClientServiceImpl(final AuthClientRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public AuthClient save(final AuthClient authClient) {
        return repository.save(authClient);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public AuthClient findById(final int id) { return repository.findById(id); }

}
