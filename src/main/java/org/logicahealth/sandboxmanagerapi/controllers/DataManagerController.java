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
import org.logicahealth.sandboxmanagerapi.model.DataSet;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.SandboxImport;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping({"/fhirdata"})
public class DataManagerController {
    private static Logger LOGGER = LoggerFactory.getLogger(DataManagerController.class.getName());

    private final SandboxService sandboxService;
    private final UserService userService;
    private final DataManagerService dataManagerService;
    private final SandboxActivityLogService sandboxActivityLogService;
    private final AuthorizationService authorizationService;

    @Inject
    public DataManagerController(final UserService userService,
                                 final SandboxService sandboxService, final DataManagerService dataManagerService,
                                 final SandboxActivityLogService sandboxActivityLogService, final AuthorizationService authorizationService) {
        this.userService = userService;
        this.sandboxActivityLogService = sandboxActivityLogService;
        this.sandboxService = sandboxService;
        this.dataManagerService = dataManagerService;
        this.authorizationService = authorizationService;
    }

    @GetMapping(value = "/import", params = {"sandboxId"})
    @Transactional
    public @ResponseBody
    List<SandboxImport> getSandboxImports(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId)  throws UnsupportedEncodingException {

        LOGGER.info("Inside DataManagerController - getSandboxImports");
        
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkUserAuthorization(request, user.getSbmUserId());
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);

        return sandbox.getImports();
    }

    @PostMapping(value = "/import", params = {"sandboxId", "patientId", "endpoint", "fhirIdPrefix"})
    @Transactional
    public @ResponseBody String importAllPatientData(final HttpServletRequest request,
                                                     @RequestParam(value = "sandboxId") String sandboxId,
                                                     @RequestParam(value = "patientId") String patientId,
                                                     @RequestParam(value = "fhirIdPrefix") String fhirIdPrefix,
                                                     @RequestParam(value = "endpoint") String encodedEndpoint)  throws UnsupportedEncodingException {

        LOGGER.info("Inside DataManagerController - importAllPatientData");

        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkUserAuthorization(request, user.getSbmUserId());
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        authorizationService.checkSystemUserCanManageSandboxDataAuthorization(request, sandbox, user);
        sandboxActivityLogService.sandboxImport(sandbox, user);
        String endpoint = null;
        if (encodedEndpoint != null) {
            endpoint = java.net.URLDecoder.decode(encodedEndpoint, StandardCharsets.UTF_8.name());
        }

        return dataManagerService.importPatientData(sandbox, authorizationService.getBearerToken(request), endpoint, patientId, fhirIdPrefix);
    }

    @PostMapping(value = "/reset", params = {"sandboxId", "dataSet"})
    @Transactional
    public @ResponseBody String reset(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId, @RequestParam(value = "dataSet") DataSet dataSet)  throws UnsupportedEncodingException {

        LOGGER.info("Inside DataManagerController - reset");
        
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);

        sandboxActivityLogService.sandboxReset(sandbox, user);
        sandbox.setDataSet(dataSet);
        return dataManagerService.reset(sandbox, authorizationService.getBearerToken(request));
    }
}
