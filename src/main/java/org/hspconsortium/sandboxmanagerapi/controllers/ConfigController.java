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
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/config")
public class ConfigController {

    private final ConfigService configurationService;

    @Inject
    public ConfigController(final ConfigService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping(value = "/{type}", produces = APPLICATION_JSON_VALUE)
    public List<Config> getConfigValuesByType(@PathVariable int type) {
        ConfigType configType = ConfigType.fromInt(type);
        return configurationService.findByConfigType(configType);
    }

}
