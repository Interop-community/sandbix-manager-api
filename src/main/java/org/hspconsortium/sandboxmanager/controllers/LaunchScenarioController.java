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

package org.hspconsortium.sandboxmanager.controllers;

import org.apache.http.HttpStatus;
import org.hspconsortium.sandboxmanager.model.*;
import org.hspconsortium.sandboxmanager.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/launchScenario")
public class LaunchScenarioController {

    private final LaunchScenarioService launchScenarioService;
    private final UserService userService;
    private final PatientService patientService;
    private final PersonaService personaService;
    private final AppService appService;
    private final SandboxService sandboxService;
    private final OAuthService oAuthUserService;

    @Inject
    public LaunchScenarioController(final LaunchScenarioService launchScenarioService,
                                    final PatientService patientService, final PersonaService personaService,
                                    final AppService appService, final UserService userService,
                                    final SandboxService sandboxService, final OAuthService oAuthUserService) {
        this.launchScenarioService = launchScenarioService;
        this.userService = userService;
        this.patientService = patientService;
        this.personaService = personaService;
        this.appService = appService;
        this.sandboxService = sandboxService;
        this.oAuthUserService = oAuthUserService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    public @ResponseBody LaunchScenario createLaunchScenario(HttpServletRequest request, @RequestBody @Valid final LaunchScenario launchScenario) {
        // A null sandbox is the HSPC sandbox
        Sandbox sandbox = null;
        if (launchScenario.getSandbox() != null) {
            sandbox = sandboxService.findBySandboxId(launchScenario.getSandbox().getSandboxId());
            checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
            launchScenario.setSandbox(sandbox);
        }

        checkUserAuthorization(request, launchScenario.getCreatedBy().getLdapId());
        User user = userService.findByLdapId(launchScenario.getCreatedBy().getLdapId());
        if (user == null) {
            user = userService.save(launchScenario.getCreatedBy());
        }
        launchScenario.setCreatedBy(user);
        List<User> users = new ArrayList<>(1);
        users.add(user);
        launchScenario.setUsers(users);

        Persona persona = null;
        if (launchScenario.getPersona() != null) {
            if (sandbox == null) {
                persona = personaService.findByFhirIdAndSandboxId(launchScenario.getPersona().getFhirId(), null);
            } else {
                persona = personaService.findByFhirIdAndSandboxId(launchScenario.getPersona().getFhirId(), sandbox.getSandboxId());
            }
        }
        if (persona == null && launchScenario.getPersona() != null) {
            persona = launchScenario.getPersona();
            persona.setSandbox(sandbox);
            persona = personaService.save(launchScenario.getPersona());
        }
        launchScenario.setPersona(persona);

        if (launchScenario.getPatient() != null) {
            Patient patient = null;
            if (sandbox == null) {
                patient = patientService.findByFhirIdAndSandboxId(launchScenario.getPatient().getFhirId(), null);
            } else {
                patient = patientService.findByFhirIdAndSandboxId(launchScenario.getPatient().getFhirId(), sandbox.getSandboxId());
            }
            if (patient == null) {
                patient = launchScenario.getPatient();
                patient.setSandbox(sandbox);
                patient = patientService.save(patient);
            }
            launchScenario.setPatient(patient);
        }

        App app = null;
        if (sandbox == null) {
            app = appService.findByClientIdAndSandboxId(launchScenario.getApp().getClient_id(), null);
        } else {
            app = appService.findByClientIdAndSandboxId(launchScenario.getApp().getClient_id(), sandbox.getSandboxId());
        }
        if (app == null) {
            app = launchScenario.getApp();
            app.setSandbox(sandbox);
            app = appService.save(app);
        }
        launchScenario.setApp(app);

        return launchScenarioService.save(launchScenario);
    }

    @RequestMapping(method = RequestMethod.PUT, produces ="application/json")
    public @ResponseBody LaunchScenario updateLaunchScenario(HttpServletRequest request, @RequestBody @Valid final LaunchScenario launchScenario) {
        checkUserAuthorization(request, launchScenario.getCreatedBy().getLdapId());
        LaunchScenario updateLaunchScenario = launchScenarioService.getById(launchScenario.getId());
        if (updateLaunchScenario != null) {
            updateLaunchScenario.setLastLaunchSeconds(launchScenario.getLastLaunchSeconds());
            return launchScenarioService.save(updateLaunchScenario);
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.DELETE, produces ="application/json")
    public @ResponseBody void deleteLaunchScenario(HttpServletRequest request, @RequestBody @Valid final LaunchScenario launchScenario) {
        checkUserAuthorization(request, launchScenario.getCreatedBy().getLdapId());
        launchScenarioService.delete(launchScenario);
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json",
            params = {"userId", "sandboxId"})
    public @ResponseBody Iterable<LaunchScenario> getLaunchScenarios(HttpServletRequest request,
        @RequestParam(value = "userId") String userIdEncoded, @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException{

        String userId = java.net.URLDecoder.decode(userIdEncoded, "UTF-8");
        checkUserAuthorization(request, userId);
        // A null sandbox is the HSPC sandbox
        if (sandboxId == null || sandboxId.isEmpty()) {
            return launchScenarioService.findByUserIdAndSandboxId(userId, null);
        } else {
            return launchScenarioService.findByUserIdAndSandboxId(userId, sandboxId);
        }
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    @ResponseStatus(code = org.springframework.http.HttpStatus.UNAUTHORIZED)
    public void handleAuthorizationException(HttpServletResponse response, Exception e) throws IOException {
        response.getWriter().write(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.getWriter().write(e.getMessage());
    }

    private void checkUserAuthorization(HttpServletRequest request, String userId) {
        String oauthUserId = oAuthUserService.getOAuthUserId(request);

        if (!userId.equalsIgnoreCase(oauthUserId)) {
            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                    "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }
}
