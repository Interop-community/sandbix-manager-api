package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.SandboxImport;
import org.logicahealth.sandboxmanagerapi.repositories.SandboxImportRepository;
import org.logicahealth.sandboxmanagerapi.services.SandboxImportService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SandboxImportServiceImpl implements SandboxImportService {
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxImportServiceImpl.class.getName());

    private final SandboxImportRepository repository;

    @Inject
    public SandboxImportServiceImpl(final SandboxImportRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SandboxImport save(final SandboxImport sandboxImport) {

        LOGGER.info("save");

        SandboxImport retVal = repository.save(sandboxImport);

        LOGGER.debug("save: "
        +"Parameters: sandboxImport = "+sandboxImport+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public void delete(final int id) {
        
        LOGGER.info("delete");

        LOGGER.debug("delete: "
        +"Parameters: id = "+id+"; No return value");

        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(final SandboxImport sandboxImport) {

        LOGGER.info("delete");

        LOGGER.debug("delete: "
        +"Parameters: sandboxImport = "+sandboxImport
        +"; No return value");

        delete(sandboxImport.getId());
    }

}
