package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.ConfigType;
import org.hspconsortium.sandboxmanagerapi.model.Config;

import java.util.List;

public interface ConfigService {

    Config save(final Config configuration);

    List<Config> findByConfigType(final ConfigType configType);

}

