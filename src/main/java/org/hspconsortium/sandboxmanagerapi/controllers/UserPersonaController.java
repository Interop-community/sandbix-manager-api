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

import org.hspconsortium.sandboxmanagerapi.controllers.dto.UserPersonaCredentials;
import org.hspconsortium.sandboxmanagerapi.controllers.dto.UserPersonaDto;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.UserPersona;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/userPersona")
public class UserPersonaController extends AbstractController {
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

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
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

    @PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody UserPersona updateUserPersona(HttpServletRequest request, @RequestBody final UserPersona userPersona) {

        Sandbox sandbox = sandboxService.findBySandboxId(userPersona.getSandbox().getSandboxId());
        checkSandboxUserModifyAuthorization(request, sandbox, userPersona);
        return userPersonaService.update(userPersona);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"sandboxId"})
    @SuppressWarnings("unchecked")
    public @ResponseBody Iterable<UserPersona> getSandboxUserPersona(HttpServletRequest request,
                                                                     @RequestParam(value = "sandboxId") String sandboxId) {

        String oauthUserId = oAuthService.getOAuthUserId(request);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        checkSandboxUserReadAuthorization(request, sandbox);
        return userPersonaService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, oauthUserId, Visibility.PUBLIC);
    }

    @GetMapping(value = "/default", produces = APPLICATION_JSON_VALUE, params = {"sandboxId"})
    @SuppressWarnings("unchecked")
    public @ResponseBody UserPersona getSandboxDefaultUserPersona(HttpServletRequest request,
                                                                     @RequestParam(value = "sandboxId") String sandboxId) {

        String oauthUserId = oAuthService.getOAuthUserId(request);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        checkSandboxUserReadAuthorization(request, sandbox);
        return userPersonaService.findDefaultBySandboxId(sandboxId, oauthUserId, Visibility.PUBLIC);
    }

    @GetMapping(params = {"lookUpId"})
    public @ResponseBody String checkForUserPersonaById(@RequestParam(value = "lookUpId")  String id) {
        UserPersona userPersona = userPersonaService.findByPersonaUserId(id);
        return (userPersona == null) ? null : userPersona.getPersonaUserId();
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public void deleteSandboxUserPersona(HttpServletRequest request, @PathVariable Integer id) {
        UserPersona userPersona = userPersonaService.getById(id);
        checkSandboxUserModifyAuthorization(request, userPersona.getSandbox(), userPersona);

        userPersonaService.delete(userPersona);
    }

    @GetMapping(value = "/{username}", produces = APPLICATION_JSON_VALUE)
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

    @PostMapping(value="/authenticate", produces = APPLICATION_JSON_VALUE)
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