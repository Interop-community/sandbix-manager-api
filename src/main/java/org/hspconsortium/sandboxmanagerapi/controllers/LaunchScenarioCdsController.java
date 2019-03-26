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
@RequestMapping("/launchScenarioCds")
public class LaunchScenarioCdsController {

    private final LaunchScenarioCdsService launchScenarioCdsService;
    private final UserService userService;
    private final CdsServiceEndpointService cdsServiceEndpointService;
    private final CdsHookService cdsHookService;
    private final UserPersonaService userPersonaService;
    private final SandboxService sandboxService;
    private final UserLaunchService userLaunchService;
    private final AuthorizationService authorizationService;

    @Inject
    public LaunchScenarioCdsController(final LaunchScenarioCdsService launchScenarioCdsService,
                                    final CdsServiceEndpointService cdsServiceEndpointService,
                                    final CdsHookService cdsHookService,
                                    final UserService userService,
                                    final UserPersonaService userPersonaService,
                                    final SandboxService sandboxService,
                                    final UserLaunchService userLaunchService,
                                    final AuthorizationService authorizationService) {
        this.launchScenarioCdsService = launchScenarioCdsService;
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
    public LaunchScenarioCds createLaunchScenarioCds(HttpServletRequest request, @RequestBody final LaunchScenarioCds launchScenarioCds) {

        Sandbox sandbox = sandboxService.findBySandboxId(launchScenarioCds.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(launchScenarioCds.getCreatedBy().getSbmUserId());
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
        authorizationService.checkUserAuthorization(request, launchScenarioCds.getCreatedBy().getSbmUserId());

        launchScenarioCds.setSandbox(sandbox);

        launchScenarioCds.setVisibility(authorizationService.getDefaultVisibility(user, sandbox));
        launchScenarioCds.setCreatedBy(user);

        LaunchScenarioCds createdLaunchScenarioCds = launchScenarioCdsService.create(launchScenarioCds);
        userLaunchService.create(new UserLaunch(user, null, createdLaunchScenarioCds, new Timestamp(new Date().getTime())));
        return createdLaunchScenarioCds;
    }

    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public LaunchScenarioCds updateLaunchScenarioCds(HttpServletRequest request, @PathVariable Integer id, @RequestBody final LaunchScenarioCds launchScenarioCds) {
        LaunchScenarioCds existinglaunchScenarioCds = launchScenarioCdsService.getById(id);
        if (existinglaunchScenarioCds == null || id.intValue() != launchScenarioCds.getId().intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : Launch Scenario Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenarioCds.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, launchScenarioCds);
        return launchScenarioCdsService.update(launchScenarioCds);
    }

    @PutMapping(value = "/{id}/launched", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public void updateLaunchTimestamp(HttpServletRequest request, @PathVariable Integer id, @RequestBody final LaunchScenarioCds launchScenarioCds) {
        LaunchScenarioCds existingLaunchScenarioCds = launchScenarioCdsService.getById(id);
        if (existingLaunchScenarioCds == null || id.intValue() != launchScenarioCds.getId().intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : Launch Scenario Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenarioCds.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        UserLaunch userLaunch = userLaunchService.findByUserIdAndLaunchScenarioCdsId(authorizationService.getSystemUserId(request), existingLaunchScenarioCds.getId());
        if (userLaunch == null) {
            User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
            userLaunchService.create(new UserLaunch(user, null, existingLaunchScenarioCds, new Timestamp(new Date().getTime())));
        } else {
            userLaunchService.update(userLaunch);
        }
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"cdsId"})
    @ResponseBody
    public Iterable<LaunchScenarioCds> getLaunchScenariosForCdsEndpointService(HttpServletRequest request,
                                                                           @RequestParam(value = "cdsId") int cdsId) {
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(cdsId);
        if (cdsServiceEndpoint == null) {
            throw new ResourceNotFoundException("CDS not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, cdsServiceEndpoint.getSandbox());

        return launchScenarioCdsService.findByCdsIdAndSandboxId(cdsServiceEndpoint.getId(), cdsServiceEndpoint.getSandbox().getSandboxId());
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"userPersonaId"})
    @ResponseBody
    public Iterable<LaunchScenarioCds> getLaunchScenariosForPersona(HttpServletRequest request,
                                                                               @RequestParam(value = "userPersonaId") int personaId) {

        UserPersona userPersona = userPersonaService.getById(personaId);
        if (userPersona == null) {
            throw new ResourceNotFoundException("UserPersona not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, userPersona.getSandbox());

        return launchScenarioCdsService.findByUserPersonaIdAndSandboxId(userPersona.getId(), userPersona.getSandbox().getSandboxId());
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public void deleteLaunchScenarioCds(HttpServletRequest request, @PathVariable Integer id) {
        LaunchScenarioCds launchScenarioCds = launchScenarioCdsService.getById(id);
        if (launchScenarioCds == null) {
            throw new ResourceNotFoundException("LaunchScenario not found.");
        }
        Sandbox sandbox = sandboxService.findBySandboxId(launchScenarioCds.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserModifyAuthorization(request, sandbox, launchScenarioCds);
        launchScenarioCdsService.delete(launchScenarioCds);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"sandboxId"})
    @SuppressWarnings("unchecked")
    public @ResponseBody Iterable<LaunchScenarioCds> getLaunchScenariosCds(HttpServletRequest request,
                                                                     @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException {

        String oauthUserId = authorizationService.getSystemUserId(request);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        List<LaunchScenarioCds> launchScenariosCdsList = launchScenarioCdsService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, oauthUserId, Visibility.PUBLIC);
        // Modify the lastLaunchSeconds field of each launch scenario to match when this user last launched each launch scenario
        return launchScenarioCdsService.updateLastLaunchForCurrentUser(launchScenariosCdsList, userService.findBySbmUserId(oauthUserId));
    }
}

