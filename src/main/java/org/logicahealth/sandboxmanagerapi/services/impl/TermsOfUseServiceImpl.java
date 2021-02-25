package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.TermsOfUse;
import org.logicahealth.sandboxmanagerapi.repositories.TermsOfUseRepository;
import org.logicahealth.sandboxmanagerapi.services.TermsOfUseService;
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
        return  repository.findById(id).orElse(null);
    }

    @Override
    public TermsOfUse mostRecent() {
        List<TermsOfUse> all = repository.orderByCreatedTimestamp();
        return (all != null && !all.isEmpty() ? all.get(0) : null);
    }
}
