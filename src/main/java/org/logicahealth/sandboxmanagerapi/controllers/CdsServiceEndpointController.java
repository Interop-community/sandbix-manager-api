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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping({"/cds-services"})
public class CdsServiceEndpointController {

    private final CdsServiceEndpointService cdsServiceEndpointService;
    private final CdsHookService cdsHookService;
    private final SandboxService sandboxService;
    private final UserService userService;
    private final RuleService ruleService;
    private final AuthorizationService authorizationService;

    @Inject
    public CdsServiceEndpointController(final CdsServiceEndpointService cdsServiceEndpointService,
                                        final CdsHookService cdsHookService, final SandboxService sandboxService,
                                        final UserService userService, final RuleService ruleService,
                                        final AuthorizationService authorizationService) {
        this.cdsServiceEndpointService = cdsServiceEndpointService;
        this.cdsHookService = cdsHookService;
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.ruleService = ruleService;
        this.authorizationService = authorizationService;
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public List<CdsServiceEndpoint> createCdsServiceEndpoint(final HttpServletRequest request,
                                                             @RequestBody CdsServiceEndpoint cdsServiceEndpoint) {

        checkUserAuthorizationAndModifyCdsServiceEndpoint(request, cdsServiceEndpoint);
        authorizationService.checkUserAuthorization(request, cdsServiceEndpoint.getCreatedBy().getSbmUserId());

        return cdsServiceEndpointService.create(cdsServiceEndpoint, sandboxService.findBySandboxId(cdsServiceEndpoint.getSandbox().getSandboxId()));
    }

    private CdsServiceEndpoint checkUserAuthorizationAndModifyCdsServiceEndpoint (HttpServletRequest request,
                                                                                  CdsServiceEndpoint cdsServiceEndpoint) {
        Sandbox sandbox = sandboxService.findBySandboxId(cdsServiceEndpoint.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox specified in CDS-Service not found.");
        }
        if (!ruleService.checkIfUserCanCreateApp(sandbox)) {
            return null;
        }
        String sbmUserId = authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox);

        cdsServiceEndpoint.setSandbox(sandbox);
        User user = userService.findBySbmUserId(sbmUserId);
        cdsServiceEndpoint.setVisibility(authorizationService.getDefaultVisibility(user, sandbox));
        cdsServiceEndpoint.setCreatedBy(user);
        return cdsServiceEndpoint;
    }

    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public CdsServiceEndpoint updateCdsServiceEndpoint(final HttpServletRequest request,
                                                       @PathVariable Integer id,
                                                       @RequestBody CdsServiceEndpoint cdsServiceEndpoint) {
        CdsServiceEndpoint existingCdsServiceEndpoint = cdsServiceEndpointService.getById(id);
        if (existingCdsServiceEndpoint == null || existingCdsServiceEndpoint.getId().intValue() != id.intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : CDS-Service Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        checkUserAuthorizationAndModifyCdsServiceEndpoint(request, cdsServiceEndpoint);
        authorizationService.checkSandboxUserModifyAuthorization(request, existingCdsServiceEndpoint.getSandbox(), existingCdsServiceEndpoint);
        return cdsServiceEndpointService.update(cdsServiceEndpoint);
    }

    @GetMapping(params = {"sandboxId"})
    @ResponseBody
    public List<CdsServiceEndpoint> getCdsServiceEndpoints(final HttpServletRequest request,
                                               @RequestParam(value = "sandboxId") String sandboxId) {
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        String sbmUserId = authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        List<CdsServiceEndpoint> cdsServiceEndpoints = cdsServiceEndpointService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, sbmUserId, Visibility.PUBLIC);
        for (CdsServiceEndpoint cdsServiceEndpoint: cdsServiceEndpoints) {
            List<CdsHook> cdsHooks = cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId());
            cdsServiceEndpoint.setCdsHooks(cdsHooks);
        }
        return cdsServiceEndpoints;
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public CdsServiceEndpoint getCdsServicEndpoint(final HttpServletRequest request, @PathVariable Integer id) {
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(id);
        List<CdsHook> cdsHooks = cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId());
        cdsServiceEndpoint.setCdsHooks(cdsHooks);
        if (cdsServiceEndpoint != null) {
            authorizationService.checkSandboxUserReadAuthorization(request, cdsServiceEndpoint.getSandbox());
            return cdsServiceEndpoint;
        } else {
            throw new ResourceNotFoundException("CDS Service Endpoint was not found");
        }
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public void deleteCdsServiceEndpoint(final HttpServletRequest request, @PathVariable Integer id) {
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(id);
        if (cdsServiceEndpoint != null) {
            authorizationService.checkSandboxUserModifyAuthorization(request, cdsServiceEndpoint.getSandbox(), cdsServiceEndpoint);
            cdsServiceEndpointService.delete(cdsServiceEndpoint);
        } else {
            throw new ResourceNotFoundException("Could not find the CDS Service Endpoint");
        }
    }
}
