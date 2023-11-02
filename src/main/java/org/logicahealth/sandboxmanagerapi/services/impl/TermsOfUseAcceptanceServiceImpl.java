package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.logicahealth.sandboxmanagerapi.repositories.TermsOfUseAcceptanceRepository;
import org.logicahealth.sandboxmanagerapi.services.TermsOfUseAcceptanceService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TermsOfUseAcceptanceServiceImpl implements TermsOfUseAcceptanceService {
    private static Logger LOGGER = LoggerFactory.getLogger(TermsOfUseAcceptanceServiceImpl.class.getName());

    private final TermsOfUseAcceptanceRepository repository;

    @Inject
    public TermsOfUseAcceptanceServiceImpl(final TermsOfUseAcceptanceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TermsOfUseAcceptance save(TermsOfUseAcceptance termsOfUseAcceptance) {

        LOGGER.info("Inside TermsOfUseAcceptanceServiceImpl - save");

        TermsOfUseAcceptance retVal = repository.save(termsOfUseAcceptance);

        LOGGER.debug("Inside TermsOfUseAcceptanceServiceImpl - save: "
        +"Parameters: termsOfUseAcceptance = "+termsOfUseAcceptance
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public TermsOfUseAcceptance getById(final int id) {
        
        LOGGER.info("Inside TermsOfUseAcceptanceServiceImpl - getById");

        LOGGER.debug("Inside TermsOfUseAcceptanceServiceImpl - getById: "
        +"Parameters: id = "+id+"; Return value = "+repository.findById(id).orElse(null));

        return  repository.findById(id).orElse(null);
    }

}
