package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.ContextParams;
import org.hspconsortium.sandboxmanagerapi.repositories.ContextParamsRepository;
import org.hspconsortium.sandboxmanagerapi.services.ContextParamsService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class ContextParamsServiceImpl implements ContextParamsService {

    private final ContextParamsRepository repository;

    @Inject
    public ContextParamsServiceImpl(final ContextParamsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ContextParams save(final ContextParams contextParams) {
        return repository.save(contextParams);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(final ContextParams contextParams) {
        delete(contextParams.getId());
    }

}
