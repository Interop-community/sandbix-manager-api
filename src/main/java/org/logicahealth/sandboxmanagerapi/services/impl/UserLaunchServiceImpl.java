package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.UserLaunch;
import org.logicahealth.sandboxmanagerapi.repositories.UserLaunchRepository;
import org.logicahealth.sandboxmanagerapi.services.UserLaunchService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserLaunchServiceImpl implements UserLaunchService {
    private static Logger LOGGER = LoggerFactory.getLogger(UserLaunchServiceImpl.class.getName());

    private final UserLaunchRepository repository;

    @Inject
    public UserLaunchServiceImpl(final UserLaunchRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserLaunch save(final UserLaunch userLaunch) {

        LOGGER.info("save");

        UserLaunch retVal = repository.save(userLaunch);

        LOGGER.debug("save: "
        +"Parameters: userLaunch = "+userLaunch
        +"; Return value = "+retVal);

        return retVal;
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
    @Transactional
    public void delete(final UserLaunch userLaunch) {
        
        LOGGER.info("delete");

        delete(userLaunch.getId());

        LOGGER.debug("delete: "
        +"Parameters: userLaunch = "+userLaunch+"; No return value");

    }

    @Override
    @Transactional
    public UserLaunch create(final UserLaunch userLaunch) {
        
        LOGGER.info("create");

        UserLaunch retVal = save(userLaunch);

        LOGGER.info("create: "
        +"Parameters: userLaunch = "+userLaunch
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public UserLaunch getById(final int id) {

        LOGGER.info("getById");

        LOGGER.debug("getById: "
        +"Parameters: id = "+id+"; Return value = "+repository.findById(id).orElse(null));

        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public UserLaunch update(final UserLaunch userLaunch) {
        
        LOGGER.info("update");

        UserLaunch updateUserLaunch = getById(userLaunch.getId());
        if (updateUserLaunch != null) {
            updateUserLaunch.setLastLaunch(new Timestamp(new Date().getTime()));

            UserLaunch retVal = save(updateUserLaunch);

            LOGGER.debug("update: "
            +"Parameters: userLaunch = "+userLaunch
            +"Return value = "+retVal);

            return retVal;
        }
        
        LOGGER.debug("update: "
        +"Parameters: userLaunch = "+userLaunch
        +"Return value = null");

        return null;
    }

    @Override
    public UserLaunch findByUserIdAndLaunchScenarioId(String sbmUserId, int launchScenarioId) {

        LOGGER.info("findByUserIdAndLaunchScenarioId");

        LOGGER.debug("findByUserIdAndLaunchScenarioId: "
        +"Parameters: sbmUserId = "+sbmUserId+", launchScenarioId = "+launchScenarioId
        +"; Return value = "+repository.findByUserIdAndLaunchScenarioId(sbmUserId, launchScenarioId));

        return repository.findByUserIdAndLaunchScenarioId(sbmUserId, launchScenarioId);
    }

    @Override
    public List<UserLaunch> findByUserId(String sbmUserId) {
        
        LOGGER.info("findByUserId");

        LOGGER.debug("findByUserId: "
        +"Parameters: sbmUserId = "+sbmUserId
        +"; Return value = "+repository.findByUserId(sbmUserId));

        return repository.findByUserId(sbmUserId);
    }

    @Override
    public List<UserLaunch> findByLaunchScenarioId(int launchScenarioId) {
        
        LOGGER.info("findByLaunchScenarioId");

        LOGGER.debug("findByLaunchScenarioId: "
        +"Parameters: launchScenarioId = "+launchScenarioId
        +"; Return value = "+repository.findByLaunchScenarioId(launchScenarioId));

        return repository.findByLaunchScenarioId(launchScenarioId);
    }

}
