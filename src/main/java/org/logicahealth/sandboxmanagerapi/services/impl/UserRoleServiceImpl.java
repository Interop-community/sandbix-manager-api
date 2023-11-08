package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.UserRole;
import org.logicahealth.sandboxmanagerapi.repositories.UserRoleRepository;
import org.logicahealth.sandboxmanagerapi.services.UserRoleService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserRoleServiceImpl implements UserRoleService {
    private static Logger LOGGER = LoggerFactory.getLogger(UserRoleServiceImpl.class.getName());

    private final UserRoleRepository repository;

    @Inject
    public UserRoleServiceImpl(final UserRoleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void delete(final int id){
        
        LOGGER.info("delete");

        repository.deleteById(id);

        LOGGER.debug("delete: "
        +"Parameters: id = "+id+"; No return value");

    }

    @Override
    @Transactional
    public void delete(final UserRole userRole){
        
        LOGGER.info("delete");

        delete(userRole.getId());

        LOGGER.debug("delete: "
        +"Parameters: userRole = "+userRole+"; No return value");

    }

    @Override
    @Transactional
    public UserRole save(final UserRole userRole) {

        LOGGER.info("save");

        UserRole retVal = repository.save(userRole);

        LOGGER.debug("save: "
        +"Parameters: userRole = "+userRole+"; Return value = "+retVal);

        return retVal;
    }

}
