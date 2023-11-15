package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.ConfigType;
import org.logicahealth.sandboxmanagerapi.model.Config;
import org.logicahealth.sandboxmanagerapi.repositories.ConfigRepository;
import org.logicahealth.sandboxmanagerapi.services.ConfigService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {
    private static Logger LOGGER = LoggerFactory.getLogger(ConfigServiceImpl.class.getName());

    private final ConfigRepository repository;

    @Inject
    public ConfigServiceImpl(final ConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Config save(Config configuration) {
        
        LOGGER.info("save");

        Config retVal = repository.save(configuration);

        LOGGER.debug("save: "
        +"Parameters: configuration = "+configuration+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public List<Config> findByConfigType(ConfigType configType) {
        
        LOGGER.info("findByConfigType");

        LOGGER.debug("findByConfigType: "
        +"Parameters: configType = "+configType+"; Return value = "+repository.findByConfigType(configType));

        return repository.findByConfigType(configType);
    }

}
