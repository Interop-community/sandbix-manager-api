package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.ContextParams;
import org.logicahealth.sandboxmanagerapi.repositories.ContextParamsRepository;
import org.logicahealth.sandboxmanagerapi.services.ContextParamsService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class ContextParamsServiceImpl implements ContextParamsService {
    private static Logger LOGGER = LoggerFactory.getLogger(ContextParamsServiceImpl.class.getName());

    private final ContextParamsRepository repository;

    @Inject
    public ContextParamsServiceImpl(final ContextParamsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ContextParams save(final ContextParams contextParams) {
        
        LOGGER.info("Inside ContextParamsServiceImpl - save");

        ContextParams retVal = repository.save(contextParams);

        LOGGER.debug("Inside ContextParamsServiceImpl - save: "
        +"Parameters: contextParams = "+contextParams+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public void delete(final int id) {

        LOGGER.info("Inside ContextParamsServiceImpl - delete");

        repository.deleteById(id);
        
        LOGGER.debug("Inside ContextParamsServiceImpl - delete: "
        +"Parameters: id = "+id+"; No return value");
    }

    @Override
    @Transactional
    public void delete(final ContextParams contextParams) {
        
        LOGGER.info("Inside ContextParamsServiceImpl - delete");

        delete(contextParams.getId());
        
        LOGGER.debug("Inside ContextParamsServiceImpl - delete: "
        +"Parameters: contextParams = "+contextParams+"; No return value");
    }

}
