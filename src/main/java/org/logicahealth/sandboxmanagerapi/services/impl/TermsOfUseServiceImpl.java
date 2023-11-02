package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.TermsOfUse;
import org.logicahealth.sandboxmanagerapi.repositories.TermsOfUseRepository;
import org.logicahealth.sandboxmanagerapi.services.TermsOfUseService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TermsOfUseServiceImpl implements TermsOfUseService {
    private static Logger LOGGER = LoggerFactory.getLogger(TermsOfUseServiceImpl.class.getName());

    private final TermsOfUseRepository repository;

    @Inject
    public TermsOfUseServiceImpl(final TermsOfUseRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TermsOfUse save(TermsOfUse termsOfUse) {
        
        LOGGER.info("Inside TermsOfUseServiceImpl - save");

        TermsOfUse retVal = repository.save(termsOfUse);

        LOGGER.debug("Inside TermsOfUseServiceImpl - save: "
        +"Parameters: termsOfUse = "+termsOfUse
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public TermsOfUse getById(final int id) {

        LOGGER.info("Inside TermsOfUseServiceImpl - getById");

        LOGGER.debug("Inside TermsOfUseServiceImpl - getById: "
        +"Parameters: id = "+id
        +"; Return value = "+repository.findById(id).orElse(null));

        return  repository.findById(id).orElse(null);
    }

    @Override
    public TermsOfUse mostRecent() {

        LOGGER.info("Inside TermsOfUseServiceImpl - mostRecent");

        List<TermsOfUse> all = repository.orderByCreatedTimestamp();

        LOGGER.debug("Inside TermsOfUseServiceImpl - mostRecent: "
        +"No input parameters; Return value = "+(all != null && !all.isEmpty() ? all.get(0) : null));

        return (all != null && !all.isEmpty() ? all.get(0) : null);
    }
}
