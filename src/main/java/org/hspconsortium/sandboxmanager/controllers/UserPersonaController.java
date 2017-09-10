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

import org.hspconsortium.sandboxmanager.controllers.dto.UserPersonaCredentials;
import org.hspconsortium.sandboxmanager.controllers.dto.UserPersonaDto;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.model.UserPersona;
import org.hspconsortium.sandboxmanager.model.Visibility;
import org.hspconsortium.sandboxmanager.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

@RestController
@RequestMapping("/userPersona")
public class UserPersonaController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(UserPersonaController.class.getName());

    private final SandboxService sandboxService;
    private final UserService userService;
    private final UserPersonaService userPersonaService;
    private final JwtService jwtService;

    @Inject
    public UserPersonaController(final SandboxService sandboxService, final UserPersonaService userPersonaService,
                                 final UserService userService, final OAuthService oAuthService, final JwtService jwtService) {
        super(oAuthService);
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.userPersonaService = userPersonaService;
        this.jwtService = jwtService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    @Transactional
    public @ResponseBody UserPersona createUserPersona(HttpServletRequest request, @RequestBody final UserPersona userPersona) {

        Sandbox sandbox = sandboxService.findBySandboxId(userPersona.getSandbox().getSandboxId());
        String sbmUserId = checkSandboxUserCreateAuthorization(request, sandbox);
        userPersona.setSandbox(sandbox);
        User user = userService.findBySbmUserId(sbmUserId);
        userPersona.setVisibility(getDefaultVisibility(user, sandbox));
        userPersona.setCreatedBy(user);
        return userPersonaService.create(userPersona);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = "application/json", produces ="application/json")
    @Transactional
    public @ResponseBody UserPersona updateUserPersona(HttpServletRequest request, @RequestBody final UserPersona userPersona) {

        Sandbox sandbox = sandboxService.findBySandboxId(userPersona.getSandbox().getSandboxId());
        checkSandboxUserModifyAuthorization(request, sandbox, userPersona);
        return userPersonaService.update(userPersona);
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", params = {"sandboxId"})
    @SuppressWarnings("unchecked")
    public @ResponseBody Iterable<UserPersona> getSandboxUserPersona(HttpServletRequest request,
                                                                     @RequestParam(value = "sandboxId") String sandboxId) {

        String oauthUserId = oAuthService.getOAuthUserId(request);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        checkSandboxUserReadAuthorization(request, sandbox);
        return userPersonaService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, oauthUserId, Visibility.PUBLIC);
    }

    @RequestMapping(value = "/default", method = RequestMethod.GET, produces = "application/json", params = {"sandboxId"})
    @SuppressWarnings("unchecked")
    public @ResponseBody UserPersona getSandboxDefaultUserPersona(HttpServletRequest request,
                                                                     @RequestParam(value = "sandboxId") String sandboxId) {

        String oauthUserId = oAuthService.getOAuthUserId(request);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        checkSandboxUserReadAuthorization(request, sandbox);
        return userPersonaService.findDefaultBySandboxId(sandboxId, oauthUserId, Visibility.PUBLIC);
    }

    @RequestMapping(method = RequestMethod.GET, params = {"lookUpId"})
    public @ResponseBody String checkForUserPersonaById(@RequestParam(value = "lookUpId")  String id) {
        UserPersona userPersona = userPersonaService.findByPersonaUserId(id);
        return (userPersona == null) ? null : userPersona.getPersonaUserId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces ="application/json")
    @Transactional
    public void deleteSandboxUserPersona(HttpServletRequest request, @PathVariable Integer id) {
        UserPersona userPersona = userPersonaService.getById(id);
        checkSandboxUserModifyAuthorization(request, userPersona.getSandbox(), userPersona);

        userPersonaService.delete(userPersona);
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.GET, produces ="application/json")
    public @ResponseBody UserPersonaDto readUserPersona(HttpServletResponse response, @PathVariable String username) {
        UserPersona userPersona = userPersonaService.findByPersonaUserId(username);
        if(userPersona == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // sanitize so we're just sending back partial info
        UserPersonaDto userPersonaDto = new UserPersonaDto();
        userPersonaDto.setName(userPersona.getFhirName());
        userPersonaDto.setUsername(userPersona.getPersonaUserId());
        userPersonaDto.setResourceUrl(userPersona.getResourceUrl());

        return userPersonaDto;
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value="/authenticate", method = RequestMethod.POST, produces="application/json")
    public ResponseEntity authenticateUserPersona(@RequestBody UserPersonaCredentials userPersonaCredentials){

        if(userPersonaCredentials == null ||
                userPersonaCredentials.getUsername() == null ||
                StringUtils.isEmpty(userPersonaCredentials.getUsername())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Username is required.\"}");
        }

        UserPersona userPersona = userPersonaService.findByPersonaUserId(userPersonaCredentials.getUsername());

        if (userPersona == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Cannot find user persona with that username.\"}");
        }

        if (userPersona.getPassword().equals(userPersonaCredentials.getPassword())) {
            String jwt = jwtService.createSignedJwt(userPersonaCredentials.getUsername());
            userPersonaCredentials.setJwt(jwt);
            return ResponseEntity.ok(userPersonaCredentials);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"message\": \"Authentication failed, bad username/password.\"}");
    }
}