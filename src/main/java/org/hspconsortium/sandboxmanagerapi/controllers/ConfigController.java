/*
 * #%L
 *
 * %%
 * Copyright (C) 2014 - 2015 Healthcare Services Platform Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.ConfigType;
import org.hspconsortium.sandboxmanagerapi.model.Config;
import org.hspconsortium.sandboxmanagerapi.services.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
@RequestMapping("/config")
public class ConfigController {
    private static Logger LOGGER = LoggerFactory.getLogger(ConfigController.class.getName());

    private final ConfigService configurationService;

    @Inject
    public ConfigController(final ConfigService configurationService) {
        this.configurationService = configurationService;
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.GET, produces ="application/json")
    public List<Config> getConfigValuesByType(@PathVariable int type) {
        ConfigType configType = ConfigType.fromInt(type);
        return configurationService.findByConfigType(configType);
    }

}
