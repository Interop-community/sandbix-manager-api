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

package org.logicahealth.sandboxmanagerapi.controllers;

import org.logicahealth.sandboxmanagerapi.model.ConfigType;
import org.logicahealth.sandboxmanagerapi.model.Config;
import org.logicahealth.sandboxmanagerapi.services.ConfigService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

@RestController
@RequestMapping("/config")
public class ConfigController {

    private final ConfigService configurationService;

    @Inject
    public ConfigController(final ConfigService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping(value = "/{type}")
    public @ResponseBody List<Config> getConfigValuesByType(@PathVariable int type) {
        ConfigType configType = ConfigType.fromInt(type);
        return configurationService.findByConfigType(configType);
    }

}
