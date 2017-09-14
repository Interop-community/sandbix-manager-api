package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.TermsOfUse;
import org.hspconsortium.sandboxmanagerapi.repositories.TermsOfUseRepository;
import org.hspconsortium.sandboxmanagerapi.services.TermsOfUseService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class TermsOfUseServiceImpl implements TermsOfUseService {

    private final TermsOfUseRepository repository;

    @Inject
    public TermsOfUseServiceImpl(final TermsOfUseRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TermsOfUse save(TermsOfUse termsOfUse) {
        return repository.save(termsOfUse);
    }

    @Override
    public TermsOfUse getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    public TermsOfUse mostRecent() {
        List<TermsOfUse> all = repository.orderByCreatedTimestamp();
        return (all != null && all.size() > 0 ? all.get(0) : null);
    }
}
