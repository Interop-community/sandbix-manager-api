package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.LaunchScenario;
import org.logicahealth.sandboxmanagerapi.model.UserPersona;
import org.logicahealth.sandboxmanagerapi.model.Visibility;
import org.logicahealth.sandboxmanagerapi.repositories.UserPersonaRepository;
import org.logicahealth.sandboxmanagerapi.services.LaunchScenarioService;
import org.logicahealth.sandboxmanagerapi.services.UserPersonaService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserPersonaServiceImpl implements UserPersonaService {
    private static Logger LOGGER = LoggerFactory.getLogger(UserPersonaServiceImpl.class.getName());
    
    private final UserPersonaRepository repository;

    private LaunchScenarioService launchScenarioService;

    @Inject
    public UserPersonaServiceImpl(final UserPersonaRepository repository) {
        this.repository = repository;
    }

    @Inject
    public void setLaunchScenarioService(LaunchScenarioService launchScenarioService) {
        this.launchScenarioService = launchScenarioService;
    }

    @Override
    @Transactional
    public UserPersona save(UserPersona userPersona) {
        
        LOGGER.info("save");

        UserPersona retVal = repository.save(userPersona);

        LOGGER.debug("save: "
        +"Parameters: userPersona = "+userPersona+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public UserPersona getById(final int id) {
        
        LOGGER.info("getById");

        LOGGER.debug("getById: "
        +"Parameters: id = "+id
        +"; Return value = "+repository.findById(id).orElse(null));

        return  repository.findById(id).orElse(null);
    }

    @Override
    public UserPersona findByPersonaUserId(String personaUserId) {
        
        LOGGER.info("findByPersonaUserId");

        LOGGER.debug("findByPersonaUserId: "
        +"Parameters: personaUserId = "+personaUserId
        +"; Return value = "+repository.findByPersonaUserId(personaUserId));

        return repository.findByPersonaUserId(personaUserId);
    }

    @Override
    public UserPersona findByPersonaUserIdAndSandboxId(final String personaUserId, final String sandboxId) {
        
        LOGGER.info("findByPersonaUserIdAndSandboxId");

        LOGGER.debug("findByPersonaUserIdAndSandboxId: "
        +"Parameters: personaUserId = "+personaUserId+", sandboxId = "+sandboxId
        +"; Return value = "+repository.findByPersonaUserIdAndSandboxId(personaUserId, sandboxId));

        return repository.findByPersonaUserIdAndSandboxId(personaUserId, sandboxId);
    }

    @Override
    public List<UserPersona> findBySandboxIdAndCreatedByOrVisibility(String sandboxId, String createdBy, Visibility visibility) {
        
        LOGGER.info("findBySandboxIdAndCreatedByOrVisibility");

        LOGGER.debug("findBySandboxIdAndCreatedByOrVisibility: "
        +"Parameters: sandboxId = "+sandboxId+", createdBy = "+createdBy
        +", visibility = "+visibility
        +"; Return value = "+repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility));

        return  repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public UserPersona findDefaultBySandboxId(String sandboxId, String createdBy, Visibility visibility) {
        
        LOGGER.info("findDefaultBySandboxId");

        List<UserPersona> personas = repository.findDefaultBySandboxId(sandboxId, createdBy, visibility);

        LOGGER.debug("findDefaultBySandboxId: "
        +"Parameters: sandboxId = "+sandboxId+", createdBy = "+createdBy
        +", visibility = "+visibility+"; Return value = "
        +(!personas.isEmpty() ? personas.get(0) : null));

        return !personas.isEmpty() ? personas.get(0) : null;
    }

    @Override
    public List<UserPersona> findBySandboxIdAndCreatedBy(String sandboxId, String createdBy) {
        
        LOGGER.info("findBySandboxIdAndCreatedBy");

        LOGGER.debug("findBySandboxIdAndCreatedBy: "
        +"Parameters: sandboxId = "+sandboxId+", createdBy = "+createdBy
        +"; Return value = "+repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy));

        return  repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Override
    public List<UserPersona> findBySandboxId(final String sandboxId) {
        
        LOGGER.info("findBySandboxId");

        LOGGER.debug("findBySandboxId: "
        +"Parameters: sandboxId = "+sandboxId
        +"; Return value = "+repository.findBySandboxId(sandboxId));

        return  repository.findBySandboxId(sandboxId);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        
        LOGGER.info("delete");

        repository.deleteById(id);

        LOGGER.debug("delete: "
        +"Parameters: id = "+id+"; No return value");

    }

    @Override
    public void delete(UserPersona userPersona) {
        
        LOGGER.info("delete");

        List<LaunchScenario> launchScenarios = launchScenarioService.findByUserPersonaIdAndSandboxId(userPersona.getId(), userPersona.getSandbox().getSandboxId());
        if (launchScenarios.size() > 0) {
            throw new RuntimeException("Can't delete persona. It's tied to one or more launch scenarios.");
        }
        delete(userPersona.getId());

        LOGGER.debug("delete: "
        +"Parameters: userPersona = "+userPersona+"; No return value");

    }

    @Override
    @Transactional
    public UserPersona create(UserPersona userPersona) {
        
        LOGGER.info("create");

        LOGGER.debug("create: "
        +"(BEFORE) Parameters: userPersona = "+userPersona);

        userPersona.setCreatedTimestamp(new Timestamp(new Date().getTime()));
        checkpersonaUserId(userPersona);

        UserPersona retVal = createOrUpdate(userPersona);

        LOGGER.debug("create: "
        +"(AFTER) Parameters: userPersona = "+userPersona
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public UserPersona update(UserPersona userPersona) {
        
        LOGGER.info("update");

        UserPersona retVal = createOrUpdate(userPersona);

        LOGGER.debug("update: "
        +"Parameters: userPersona = "+userPersona
        +"; Return value = "+retVal);

        return retVal;
    }

    private UserPersona createOrUpdate(final UserPersona userPersona) {
        
        LOGGER.info("createOrUpdate");

        UserPersona retVal = save(userPersona);

        LOGGER.debug("createOrUpdate: "
        +"Parameters: userPersona = "+userPersona
        +"; Return value = "+retVal);

        return retVal;
    }

    private void checkpersonaUserId(UserPersona userPersona) {
        
        LOGGER.info("checkpersonaUserId");

        UserPersona userPersonaExists = findByPersonaUserIdAndSandboxId(userPersona.getPersonaUserId(), userPersona.getSandbox().getSandboxId());
        if (userPersonaExists != null) {
            throw new IllegalArgumentException("Persona user " + userPersona.getPersonaUserId() + " already in use in sandbox " + userPersona.getSandbox().getSandboxId());
        }

        LOGGER.debug("checkpersonaUserId: "
        +"Parameters: userPersona = "+userPersona+"; No return value");

    }
}