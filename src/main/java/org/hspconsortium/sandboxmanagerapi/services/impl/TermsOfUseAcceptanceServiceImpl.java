package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.hspconsortium.sandboxmanagerapi.repositories.TermsOfUseAcceptanceRepository;
import org.hspconsortium.sandboxmanagerapi.services.TermsOfUseAcceptanceService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class TermsOfUseAcceptanceServiceImpl implements TermsOfUseAcceptanceService {

    private final TermsOfUseAcceptanceRepository repository;

    @Inject
    public TermsOfUseAcceptanceServiceImpl(final TermsOfUseAcceptanceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TermsOfUseAcceptance save(TermsOfUseAcceptance termsOfUseAcceptance) {
        return repository.save(termsOfUseAcceptance);
    }

    @Override
    public TermsOfUseAcceptance getById(final int id) {
        return  repository.findById(id).orElse(null);
    }

}
