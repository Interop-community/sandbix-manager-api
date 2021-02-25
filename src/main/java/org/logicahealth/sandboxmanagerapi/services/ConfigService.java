package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.ConfigType;
import org.logicahealth.sandboxmanagerapi.model.Config;

import java.util.List;

public interface ConfigService {

    Config save(final Config configuration);

    List<Config> findByConfigType(final ConfigType configType);

}

