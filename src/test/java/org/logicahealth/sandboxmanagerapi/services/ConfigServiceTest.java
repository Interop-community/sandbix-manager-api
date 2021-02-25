package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Config;
import org.logicahealth.sandboxmanagerapi.model.ConfigType;
import org.logicahealth.sandboxmanagerapi.repositories.ConfigRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.ConfigServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigServiceTest {

    private ConfigRepository configRepository = mock(ConfigRepository.class);

    private ConfigServiceImpl configService = new ConfigServiceImpl(configRepository);


    Config config = new Config();
    List<Config> configList = new ArrayList<>();
    @Before
    public void setup() {
        configList.add(config);
    }

    @Test
    public void saveTest() {
        when(configRepository.save(config)).thenReturn(config);
        Config returnedConfig = configService.save(config);
        assertEquals(config, returnedConfig);
    }

    @Test
    public void findByConfigType() {
        when(configRepository.findByConfigType(ConfigType.PROPERTY)).thenReturn(configList);
        List<Config> returnedConfigList = configService.findByConfigType(ConfigType.PROPERTY);
        assertEquals(configList, returnedConfigList);
    }

}
