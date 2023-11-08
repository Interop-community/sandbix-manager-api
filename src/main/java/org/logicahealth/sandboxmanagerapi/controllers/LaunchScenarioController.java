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

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.apache.http.HttpStatus;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/launchScenario")
public class LaunchScenarioController {
    private static Logger LOGGER = LoggerFactory.getLogger(LaunchScenarioController.class.getName());

    private final LaunchScenarioService launchScenarioService;
    private final UserService userService;
    private final AppService appService;
    private final UserPersonaService userPersonaService;
    private final SandboxService sandboxService;
    private final UserLaunchService userLaunchService;
    private final AuthorizationService authorizationService;
    private final CdsHookService cdsHookService;
    private final CdsServiceEndpointService cdsServiceEndpointService;

    @Inject
    public LaunchScenarioController(final LaunchScenarioService launchScenarioService,
                                    final AppService appService,
                                    final UserService userService,
                                    final UserPersonaService userPersonaService,
                                    final SandboxService sandboxService,
                                    final UserLaunchService userLaunchService,
                                    final AuthorizationService authorizationService,
                                    final CdsHookService cdsHookService,
                                    final CdsServiceEndpointService cdsServiceEndpointService) {
        this.launchScenarioService = launchScenarioService;
        this.userService = userService;
        this.appService = appService;
        this.userPersonaService = userPersonaService;
        this.sandboxService = sandboxService;
        this.userLaunchService = userLaunchService;
        this.authorizationService = authorizationService;
        this.cdsHookService = cdsHookService;
        this.cdsServiceEndpointService = cdsServiceEndpointService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody
    LaunchScenario createLaunchScenario(HttpServletRequest request, @RequestBody final LaunchScenario launchScenario) {

        LOGGER.info("createLaunchScenario");
        
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(launchScenario.getCreatedBy().getSbmUserId());
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
        authorizationService.checkUserAuthorization(request, launchScenario.getCreatedBy().getSbmUserId());

        launchScenario.setSandbox(sandbox);

        launchScenario.setVisibility(authorizationService.getDefaultVisibility(user, sandbox));
        launchScenario.setCreatedBy(user);

        LaunchScenario createdLaunchScenario = launchScenarioService.create(launchScenario);
        userLaunchService.create(new UserLaunch(user, createdLaunchScenario, new Timestamp(new Date().getTime())));
        return createdLaunchScenario;
    }

    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody LaunchScenario updateLaunchScenario(HttpServletRequest request, @PathVariable Integer id, @RequestBody final LaunchScenario launchScenario) {
        
        LOGGER.info("updateLaunchScenario");

        LaunchScenario existingLaunchScenario = launchScenarioService.getById(id);
        if (existingLaunchScenario == null || id.intValue() != launchScenario.getId().intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : Launch Scenario Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, launchScenario);
        return launchScenarioService.update(launchScenario);
    }

    @PutMapping(value = "/{id}/launched", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public void updateLaunchTimestamp(HttpServletRequest request, @PathVariable Integer id, @RequestBody final LaunchScenario launchScenario) {
        
        LOGGER.info("updateLaunchTimestamp");
        
        LaunchScenario existingLaunchScenario = launchScenarioService.getById(id);
        if (existingLaunchScenario == null || id.intValue() != launchScenario.getId().intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : Launch Scenario Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        UserLaunch userLaunch = userLaunchService.findByUserIdAndLaunchScenarioId(authorizationService.getSystemUserId(request), existingLaunchScenario.getId());
        if (userLaunch == null) {
            User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
            userLaunchService.create(new UserLaunch(user, existingLaunchScenario, new Timestamp(new Date().getTime())));
        } else {
            userLaunchService.update(userLaunch);
        }
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"appId"})
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenariosForApp(HttpServletRequest request,
                                                                           @RequestParam(value = "appId") int appId) {
        
        LOGGER.info("getLaunchScenariosForApp");

        App app = appService.getById(appId);
        if (app == null) {
            throw new ResourceNotFoundException("App not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, app.getSandbox());

        return launchScenarioService.findByAppIdAndSandboxId(app.getId(), app.getSandbox().getSandboxId());
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"cdsHookId"})
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenariosForCdsHook(HttpServletRequest request,
                                                                           @RequestParam(value = "cdsHookId") int cdsHookId) {
        
        LOGGER.info("getLaunchScenariosForCdsHook");
        
        CdsHook cdsHook = cdsHookService.getById(cdsHookId);
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(cdsHook.getCdsServiceEndpointId());
        if (cdsHook == null) {
            throw new ResourceNotFoundException("CDS-Hook not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, cdsServiceEndpoint.getSandbox());

        return launchScenarioService.findByCdsHookIdAndSandboxId(cdsHook.getId(), cdsServiceEndpoint.getSandbox().getSandboxId());
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"userPersonaId"})
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenariosForPersona(HttpServletRequest request,
                                                                           @RequestParam(value = "userPersonaId") int personaId) {

        LOGGER.info("getLaunchScenariosForPersona");
        
        UserPersona userPersona = userPersonaService.getById(personaId);
        if (userPersona == null) {
            throw new ResourceNotFoundException("UserPersona not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, userPersona.getSandbox());

        return launchScenarioService.findByUserPersonaIdAndSandboxId(userPersona.getId(), userPersona.getSandbox().getSandboxId());
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody void deleteLaunchScenario(HttpServletRequest request, @PathVariable Integer id) {
        
        LOGGER.info("deleteLaunchScenario");
        
        LaunchScenario launchScenario = launchScenarioService.getById(id);
        if (launchScenario == null) {
            throw new ResourceNotFoundException("LaunchScenario not found.");
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, launchScenario);
        launchScenarioService.delete(launchScenario);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"sandboxId"})
    @SuppressWarnings("unchecked")
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenarios(HttpServletRequest request,
        @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException{

        LOGGER.info("getLaunchScenarios");
        
        String oauthUserId = authorizationService.getSystemUserId(request);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        List<LaunchScenario> launchScenarios = launchScenarioService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, oauthUserId, Visibility.PUBLIC);
        // Modify the lastLaunchSeconds field of each launch scenario to match when this user last launched each launch scenario
        return launchScenarioService.updateLastLaunchForCurrentUser(launchScenarios, userService.findBySbmUserId(oauthUserId));
    }
}
