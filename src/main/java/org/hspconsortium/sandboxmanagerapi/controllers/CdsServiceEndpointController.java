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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.*;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@RestController
@RequestMapping({"/cds-services"})
public class CdsServiceEndpointController {
    private static Logger LOGGER = LoggerFactory.getLogger(AppController.class.getName());

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
        this.authorizationService = authorizationService;
        this.cdsServiceEndpointService = cdsServiceEndpointService;
        this.cdsHookService = cdsHookService;
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.ruleService = ruleService;
    }

    @PostMapping
    @Transactional
    @ResponseBody
    public CdsServiceEndpoint createCdsServiceEndpoint(final HttpServletRequest request,
                                        @RequestBody CdsServiceEndpoint cdsServiceEndpoint) {
        Sandbox sandbox = sandboxService.findBySandboxId(cdsServiceEndpoint.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox specified in CDS-Service not found.");
        }
        if (!ruleService.checkIfUserCanCreateApp(sandbox)) {
            return null;
        }
        String sbmUserId = authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
        authorizationService.checkUserAuthorization(request, cdsServiceEndpoint.getCreatedBy().getSbmUserId());

        cdsServiceEndpoint.setSandbox(sandbox);
        User user = userService.findBySbmUserId(sbmUserId);
        cdsServiceEndpoint.setVisibility(authorizationService.getDefaultVisibility(user, sandbox));
        cdsServiceEndpoint.setCreatedBy(user);
        return cdsServiceEndpointService.create(cdsServiceEndpoint, sandbox);
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
        return cdsServiceEndpointService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, sbmUserId, Visibility.PUBLIC);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody CdsServiceEndpoint getCDS(final HttpServletRequest request, @PathVariable Integer id) {
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(id);
        if (cdsServiceEndpoint != null) {
            authorizationService.checkSandboxUserReadAuthorization(request, cdsServiceEndpoint.getSandbox());
            return cdsServiceEndpoint;
        } else {
            throw new ResourceNotFoundException("CDS Service Endpoint was not found");
        }
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody void deleteCdsServiceEndpoint(final HttpServletRequest request, @PathVariable Integer id) {
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(id);
        CdsHook cdsHook = cdsHookService.getById(id);
        if (cdsServiceEndpoint != null) {
            authorizationService.checkSandboxUserModifyAuthorization(request, cdsServiceEndpoint.getSandbox(), cdsServiceEndpoint);
            cdsServiceEndpointService.delete(cdsServiceEndpoint);
        } else {
            throw new ResourceNotFoundException("Could not find the CDS-Service.");
        }
    }

    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody CdsServiceEndpoint updateCdsServiceEndpoint(final HttpServletRequest request,
                                                                     @PathVariable Integer id,
                                                                     @RequestBody CdsServiceEndpoint cdsServiceEndpoint) {
        CdsServiceEndpoint existingCdsServiceEndpoint = cdsServiceEndpointService.getById(id);
        if (existingCdsServiceEndpoint == null || existingCdsServiceEndpoint.getId().intValue() != id.intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : CDS-Service Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        authorizationService.checkSandboxUserModifyAuthorization(request, existingCdsServiceEndpoint.getSandbox(), existingCdsServiceEndpoint);
        return cdsServiceEndpointService.update(cdsServiceEndpoint);
    }


    @GetMapping(value = "/{id}/image", produces ={IMAGE_GIF_VALUE, IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE, "image/jpg"})
    public @ResponseBody void getFullImage(final HttpServletResponse response, @PathVariable Integer id) {
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(id);
        CdsHook cdsHook = cdsHookService.getById(id);
        if (cdsServiceEndpoint == null) {
            throw new ResourceNotFoundException("CDS-Service not found.");
        }
        try {
            response.setHeader("Content-Type", cdsHook.getLogo().getContentType());
            response.getOutputStream().write(cdsHook.getLogo().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/{id}/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE} )
    @Transactional
    public @ResponseBody void putFullImage(final HttpServletRequest request, @PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(id);
        CdsHook cdsHook = cdsHookService.getById(id);
        if (cdsServiceEndpoint == null) {
            throw new ResourceNotFoundException("CDS-Service does not exist. Cannot upload image.");
        }
        authorizationService.checkSandboxUserModifyAuthorization(request, cdsServiceEndpoint.getSandbox(), cdsServiceEndpoint);
        cdsHook.setLogoUri(request.getRequestURL().toString());
        cdsServiceEndpointService.save(cdsServiceEndpoint);
        try {
            Image image = new Image();
            image.setBytes(file.getBytes());
            image.setContentType(file.getContentType());
            cdsHookService.updateCdsHookImage(cdsHook, image);
        } catch (IOException e) {
            if(LOGGER.isErrorEnabled()){
                LOGGER.error("Unable to update image", e);
            }
        }
    }

    @DeleteMapping(value = "/{id}/image")
    @Transactional
    public CdsHook deleteFullImage(final HttpServletRequest request, @PathVariable Integer id) {
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.getById(id);
        CdsHook cdsHook = cdsHookService.getById(id);
        if (cdsServiceEndpoint == null) {
            throw new ResourceNotFoundException("CDS-Service does not exist. Cannot delete image.");
        }
        authorizationService.checkSandboxUserModifyAuthorization(request, cdsServiceEndpoint.getSandbox(), cdsServiceEndpoint);
        return cdsHookService.deleteCdsHookImage(cdsHook);
    }
}
