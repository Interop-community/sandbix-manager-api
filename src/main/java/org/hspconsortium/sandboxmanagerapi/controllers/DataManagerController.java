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
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping({"/fhirdata"})
public class DataManagerController {

    // DON'T NEED THIS AS WE DON'T RESET SANDBOXES ANYMORE
//    private final SandboxService sandboxService;
//    private final UserService userService;
//    private final DataManagerService dataManagerService;
//    private final SandboxActivityLogService sandboxActivityLogService;
//    private final AuthorizationService authorizationService;
//
//    @Inject
//    public DataManagerController(final UserService userService,
//                                 final SandboxService sandboxService, final DataManagerService dataManagerService,
//                                 final SandboxActivityLogService sandboxActivityLogService, final AuthorizationService authorizationService) {
//        this.userService = userService;
//        this.sandboxActivityLogService = sandboxActivityLogService;
//        this.sandboxService = sandboxService;
//        this.dataManagerService = dataManagerService;
//        this.authorizationService = authorizationService;
//    }
//
//    @GetMapping(value = "/import", params = {"sandboxId"})
//    @Transactional
//    public @ResponseBody
//    List<SandboxImport> getSandboxImports(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId)  throws UnsupportedEncodingException {
//
//        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
//        authorizationService.checkUserAuthorization(request, user.getSbmUserId());
//        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
//        if (sandbox == null) {
//            throw new ResourceNotFoundException("Sandbox not found.");
//        }
//        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
//
//        return sandbox.getImports();
//    }
//
//    @PostMapping(value = "/import", params = {"sandboxId", "patientId", "endpoint", "fhirIdPrefix"})
//    @Transactional
//    public @ResponseBody String importAllPatientData(final HttpServletRequest request,
//                                                     @RequestParam(value = "sandboxId") String sandboxId,
//                                                     @RequestParam(value = "patientId") String patientId,
//                                                     @RequestParam(value = "fhirIdPrefix") String fhirIdPrefix,
//                                                     @RequestParam(value = "endpoint") String encodedEndpoint)  throws UnsupportedEncodingException {
//
//        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
//        authorizationService.checkUserAuthorization(request, user.getSbmUserId());
//        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
//        authorizationService.checkSystemUserCanManageSandboxDataAuthorization(request, sandbox, user);
//        sandboxActivityLogService.sandboxImport(sandbox, user);
//        String endpoint = null;
//        if (encodedEndpoint != null) {
//            endpoint = java.net.URLDecoder.decode(encodedEndpoint, StandardCharsets.UTF_8.name());
//        }
//
//        return dataManagerService.importPatientData(sandbox, authorizationService.getBearerToken(request), endpoint, patientId, fhirIdPrefix);
//    }
//
//    @PostMapping(value = "/reset", params = {"sandboxId", "dataSet"})
//    @Transactional
//    public @ResponseBody String reset(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId, @RequestParam(value = "dataSet") DataSet dataSet)  throws UnsupportedEncodingException {
//
//        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
//        if (sandbox == null) {
//            throw new ResourceNotFoundException("Sandbox not found.");
//        }
//        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
//        authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
//
//        sandboxActivityLogService.sandboxReset(sandbox, user);
//        sandbox.setDataSet(dataSet);
//        return dataManagerService.reset(sandbox, authorizationService.getBearerToken(request));
//    }
}
