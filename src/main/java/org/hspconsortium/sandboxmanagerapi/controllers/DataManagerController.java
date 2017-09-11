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
@RequestMapping({"/api/fhirdata"})
public class DataManagerController extends AbstractController {

    private static Logger LOGGER = LoggerFactory.getLogger(DataManagerController.class.getName());

    private final SandboxService sandboxService;
    private final UserService userService;
    private final DataManagerService dataManagerService;
    private final SandboxActivityLogService sandboxActivityLogService;

    @Inject
    public DataManagerController(final OAuthService oAuthService, final UserService userService,
                                 final SandboxService sandboxService, final DataManagerService dataManagerService,
                                 final SandboxActivityLogService sandboxActivityLogService) {
        super(oAuthService);
        this.userService = userService;
        this.sandboxActivityLogService = sandboxActivityLogService;
        this.sandboxService = sandboxService;
        this.dataManagerService = dataManagerService;
    }

    @RequestMapping(value = "/import", method = RequestMethod.GET, params = {"sandboxId"})
    @Transactional
    public @ResponseBody
    List<SandboxImport> getSandboxImports(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId)  throws UnsupportedEncodingException {

        User user = userService.findBySbmUserId(getSystemUserId(request));
        checkUserAuthorization(request, user.getSbmUserId());
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        checkSandboxUserReadAuthorization(request, sandbox);

        return sandbox.getImports();
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST, params = {"sandboxId", "patientId", "endpoint", "fhirIdPrefix"})
    @Transactional
    public @ResponseBody String importAllPatientData(final HttpServletRequest request,
                                                     @RequestParam(value = "sandboxId") String sandboxId,
                                                     @RequestParam(value = "patientId") String patientId,
                                                     @RequestParam(value = "fhirIdPrefix") String fhirIdPrefix,
                                                     @RequestParam(value = "endpoint") String encodedEndpoint)  throws UnsupportedEncodingException {

        User user = userService.findBySbmUserId(getSystemUserId(request));
        checkUserAuthorization(request, user.getSbmUserId());
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        checkSystemUserCanManageSandboxDataAuthorization(request, sandbox, user);
        sandboxActivityLogService.sandboxImport(sandbox, user);
        String endpoint = null;
        if (encodedEndpoint != null) {
            endpoint = java.net.URLDecoder.decode(encodedEndpoint, StandardCharsets.UTF_8.name());
        }

        return dataManagerService.importPatientData(sandbox, oAuthService.getBearerToken(request), endpoint, patientId, fhirIdPrefix);
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST, params = {"sandboxId", "dataSet"})
    @Transactional
    public @ResponseBody String reset(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId, @RequestParam(value = "dataSet") DataSet dataSet)  throws UnsupportedEncodingException {

        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        User user = userService.findBySbmUserId(getSystemUserId(request));
        checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);

        sandboxActivityLogService.sandboxReset(sandbox, user);
        sandbox.setDataSet(dataSet);
        return dataManagerService.reset(sandbox, oAuthService.getBearerToken(request));
    }
}
