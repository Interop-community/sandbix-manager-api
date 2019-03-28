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

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.apache.http.HttpStatus;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/launchScenarioCdsServiceEndpoint")
public class LaunchScenarioCdsServiceEndpointController {

    private final LaunchScenarioCdsServiceEndpointService launchScenarioCdsServiceEndpointService;
    private final UserService userService;
    private final CdsServiceEndpointService cdsServiceEndpointService;
    private final CdsHookService cdsHookService;
    private final UserPersonaService userPersonaService;
    private final SandboxService sandboxService;
    private final UserLaunchCdsServiceEndpointService userLaunchService;
    private final AuthorizationService authorizationService;

    @Inject
    public LaunchScenarioCdsServiceEndpointController(final LaunchScenarioCdsServiceEndpointService launchScenarioCdsServiceEndpointService,
                                                      final CdsServiceEndpointService cdsServiceEndpointService,
                                                      final CdsHookService cdsHookService,
                                                      final UserService userService,
                                                      final UserPersonaService userPersonaService,
                                                      final SandboxService sandboxService,
                                                      final UserLaunchCdsServiceEndpointService userLaunchService,
                                                      final AuthorizationService authorizationService) {
        this.launchScenarioCdsServiceEndpointService = launchScenarioCdsServiceEndpointService;
        this.userService = userService;
        this.cdsServiceEndpointService = cdsServiceEndpointService;
        this.cdsHookService = cdsHookService;
        this.userPersonaService = userPersonaService;
        this.sandboxService = sandboxService;
        this.userLaunchService = userLaunchService;
        this.authorizationService = authorizationService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public LaunchScenarioCdsServiceEndpoint createLaunchScenarioCds(HttpServletRequest request, @RequestBody final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint) {

        Sandbox sandbox = sandboxService.findBySandboxId(launchScenarioCdsServiceEndpoint.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(launchScenarioCdsServiceEndpoint.getCreatedBy().getSbmUserId());
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
        authorizationService.checkUserAuthorization(request, launchScenarioCdsServiceEndpoint.getCreatedBy().getSbmUserId());

        launchScenarioCdsServiceEndpoint.setSandbox(sandbox);

        launchScenarioCdsServiceEndpoint.setVisibility(authorizationService.getDefaultVisibility(user, sandbox));
        launchScenarioCdsServiceEndpoint.setCreatedBy(user);

        LaunchScenarioCdsServiceEndpoint createdLaunchScenarioCds = launchScenarioCdsServiceEndpointService.create(launchScenarioCdsServiceEndpoint);
        userLaunchService.create(new UserLaunchCdsServiceEndpoint(user, createdLaunchScenarioCds, new Timestamp(new Date().getTime())));
        return createdLaunchScenarioCds;
    }

    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public LaunchScenarioCdsServiceEndpoint updateLaunchScenarioCds(HttpServletRequest request, @PathVariable Integer id, @RequestBody final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint) {
        LaunchScenarioCdsServiceEndpoint existinglaunchScenarioCds = launchScenarioCdsServiceEndpointService.getById(id);
        if (existinglaunchScenarioCds == null || id.intValue() != launchScenarioCdsServiceEndpoint.getId().intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : Launch Scenario Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenarioCdsServiceEndpoint.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, launchScenarioCdsServiceEndpoint);
        return launchScenarioCdsServiceEndpointService.update(launchScenarioCdsServiceEndpoint);
    }

    @PutMapping(value = "/{id}/launched", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public void updateLaunchTimestamp(HttpServletRequest request, @PathVariable Integer id, @RequestBody final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint) {
        LaunchScenarioCdsServiceEndpoint existingLaunchScenarioCds = launchScenarioCdsServiceEndpointService.getById(id);
        if (existingLaunchScenarioCds == null || id.intValue() != launchScenarioCdsServiceEndpoint.getId().intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : Launch Scenario Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenarioCdsServiceEndpoint.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        UserLaunchCdsServiceEndpoint userLaunch = userLaunchService.findByUserIdAndLaunchScenarioCdsServiceEndpointId(authorizationService.getSystemUserId(request), existingLaunchScenarioCds.getId());
        if (userLaunch == null) {
            User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
            userLaunchService.create(new UserLaunchCdsServiceEndpoint(user, existingLaunchScenarioCds, new Timestamp(new Date().getTime())));
        } else {
            userLaunchService.update(userLaunch);
        }
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"cdsId"})
    @ResponseBody
    public Iterable<LaunchScenarioCdsServiceEndpoint> getLaunchScenariosForCdsEndpointService(HttpServletRequest request,
                                                                           @RequestParam(value = "cdsId") int cdsId) {
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(cdsId);
        if (cdsServiceEndpoint == null) {
            throw new ResourceNotFoundException("CDS not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, cdsServiceEndpoint.getSandbox());

        return launchScenarioCdsServiceEndpointService.findByCdsIdAndSandboxId(cdsServiceEndpoint.getId(), cdsServiceEndpoint.getSandbox().getSandboxId());
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"userPersonaId"})
    @ResponseBody
    public Iterable<LaunchScenarioCdsServiceEndpoint> getLaunchScenariosForPersona(HttpServletRequest request,
                                                                               @RequestParam(value = "userPersonaId") int personaId) {

        UserPersona userPersona = userPersonaService.getById(personaId);
        if (userPersona == null) {
            throw new ResourceNotFoundException("UserPersona not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, userPersona.getSandbox());

        return launchScenarioCdsServiceEndpointService.findByUserPersonaIdAndSandboxId(userPersona.getId(), userPersona.getSandbox().getSandboxId());
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public void deleteLaunchScenarioCds(HttpServletRequest request, @PathVariable Integer id) {
        LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint = launchScenarioCdsServiceEndpointService.getById(id);
        if (launchScenarioCdsServiceEndpoint == null) {
            throw new ResourceNotFoundException("LaunchScenario not found.");
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenarioCdsServiceEndpoint.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, launchScenarioCdsServiceEndpoint);
        launchScenarioCdsServiceEndpointService.delete(launchScenarioCdsServiceEndpoint);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"sandboxId"})
    @SuppressWarnings("unchecked")
    public @ResponseBody Iterable<LaunchScenarioCdsServiceEndpoint> getLaunchScenariosCds(HttpServletRequest request,
                                                                     @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException {

        String oauthUserId = authorizationService.getSystemUserId(request);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        List<LaunchScenarioCdsServiceEndpoint> launchScenariosCdsList = launchScenarioCdsServiceEndpointService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, oauthUserId, Visibility.PUBLIC);
        // Modify the lastLaunchSeconds field of each launch scenario to match when this user last launched each launch scenario
        return launchScenarioCdsServiceEndpointService.updateLastLaunchForCurrentUser(launchScenariosCdsList, userService.findBySbmUserId(oauthUserId));
    }
}

