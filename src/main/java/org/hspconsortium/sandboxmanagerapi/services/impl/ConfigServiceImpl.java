package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.ConfigType;
import org.hspconsortium.sandboxmanagerapi.model.Config;
import org.hspconsortium.sandboxmanagerapi.repositories.ConfigRepository;
import org.hspconsortium.sandboxmanagerapi.services.ConfigService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository repository;

    @Inject
    public ConfigServiceImpl(final ConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Config save(Config configuration) {
        return repository.save(configuration);
    }

    @Override
    public List<Config> findByConfigType(ConfigType configType) {
        return repository.findByConfigType(configType);
    }

}
