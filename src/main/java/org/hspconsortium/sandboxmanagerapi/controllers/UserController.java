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
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SystemRole;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.UserPersona;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Semaphore;

@RestController
@RequestMapping({"/user"})
public class UserController extends AbstractController {

    @Value("${hspc.platform.defaultSystemRoles}")
    private String[] defaultSystemRoles;

    @Value("${hspc.platform.templateSandboxIds}")
    private String[] templateSandboxIds;

    private static Logger LOGGER = LoggerFactory.getLogger(UserController.class.getName());

    private final UserService userService;
    private final SandboxInviteService sandboxInviteService;
    private final UserPersonaService userPersonaService;
    private final SandboxActivityLogService sandboxActivityLogService;
    private final SandboxService sandboxService;

    private static Semaphore semaphore = new Semaphore(1);

    @Inject
    public UserController(final OAuthService oAuthService, final UserService userService,
                          final SandboxActivityLogService sandboxActivityLogService,
                          final SandboxInviteService sandboxInviteService,
                          final UserPersonaService userPersonaService, final SandboxService sandboxService) {
        super(oAuthService);
        this.userService = userService;
        this.sandboxInviteService = sandboxInviteService;
        this.userPersonaService = userPersonaService;
        this.sandboxActivityLogService = sandboxActivityLogService;
        this.sandboxService = sandboxService;
    }

    @GetMapping(params = {"sbmUserId"})
    @Transactional
    public @ResponseBody
    User getUser(final HttpServletRequest request, @RequestParam(value = "sbmUserId") String sbmUserId) {
        checkUserAuthorization(request, sbmUserId);
        String oauthUsername = oAuthService.getOAuthUserName(request);
        String oauthUserEmail = oAuthService.getOAuthUserEmail(request);
        User user = null;
        try {
            semaphore.acquire();
            user = createUserIfNotExists(sbmUserId, oauthUsername, oauthUserEmail);
        } catch (InterruptedException e) {
            LOGGER.error("User create thread interrupted.", e);
        } catch(Exception e) {
            LOGGER.error("Exception handling the creation of a user.", e);
        } finally {
            // thread will be released in the event of an exception or successful user return
            semaphore.release();
        }
        return user;
    }

    @GetMapping(value = "/all", params = {"sbmUserId"})
    @Transactional
    public @ResponseBody
    Iterable<User> getAllUsers(final HttpServletRequest request, @RequestParam(value = "sbmUserId") String sbmUserId) {
        checkUserAuthorization(request, sbmUserId);
        User user = userService.findBySbmUserId(sbmUserId);
        Boolean isSystemAdmin = checkUserHasSystemRole(user, SystemRole.ADMIN);
        if (isSystemAdmin) {
            return userService.findAll();
        } else {
            throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
        }
    }

    @PostMapping(value = "/acceptterms", params = {"sbmUserId", "termsId"})
    @Transactional
    public void acceptTermsOfUse(final HttpServletRequest request, @RequestParam(value = "sbmUserId") String sbmUserId,
                                 @RequestParam(value = "termsId") String termsId) {

        checkUserAuthorization(request, sbmUserId);
        User user = userService.findBySbmUserId(sbmUserId);
        userService.acceptTermsOfUse(user, termsId);
    }

    @PostMapping(value = "/authorize")
    @Transactional
    public ResponseEntity authorizeUserForReferenceApi(final HttpServletRequest request, @RequestBody String sandboxJSONString) {
        String userId = oAuthService.getOAuthUserId(request);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        Sandbox sandbox;

        try {
            JSONObject sandboxJSON = new JSONObject(sandboxJSONString);
            String sandboxId = sandboxJSON.getString("sandbox");
            sandbox = sandboxService.findBySandboxId(sandboxId);
            if (Arrays.asList(templateSandboxIds).contains(sandboxId)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body("No need to authorize.");
            }
        } catch (JSONException e) {
            LOGGER.error("JSON Error reading entity: " + sandboxJSONString, e);
            throw new RuntimeException(e);
        }
        try {
            if (!checkUserHasSystemRole(user, SystemRole.ADMIN)) {
                checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
            }

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body("User is not authorized.");
        }
        return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body("User is authorized.");
    }

    private User createUserIfNotExists(String sbmUserId, String oauthUsername, String oauthUserEmail) {
        User user = userService.findBySbmUserId(sbmUserId);
        if (user == null) {
            user = userService.findByUserEmail(oauthUserEmail);
            if (user != null) {
                user.setSbmUserId(sbmUserId);
            }
        }
        // Create User if needed (if it's the first login to the system)
        if (user == null) {
            UserPersona userPersona = userPersonaService.findByPersonaUserId(sbmUserId);
            if (userPersona != null) {
                //This is a user persona. A user persona cannot be a sandbox user also
                return null;
            }

            user = new User();
            user.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            user.setSbmUserId(sbmUserId);
            user.setName(oauthUsername);
            user.setEmail(oauthUserEmail);
            user.setHasAcceptedLatestTermsOfUse(false);
            sandboxActivityLogService.systemUserCreated(null, user);

            Set<SystemRole> systemRoles = new HashSet<>();
            for (String roleName : defaultSystemRoles) {
                SystemRole role = SystemRole.valueOf(roleName);
                systemRoles.add(role);
                sandboxActivityLogService.systemUserRoleChange(user, role, true);
            }
            user.setSystemRoles(systemRoles);
            userService.save(user);
        } else if (StringUtils.isEmpty(user.getName()) || !user.getName().equalsIgnoreCase(oauthUsername) ||
                StringUtils.isEmpty(user.getEmail()) || !user.getEmail().equalsIgnoreCase(oauthUserEmail)) {

            Set<SystemRole> curSystemRoles = user.getSystemRoles();
            if (curSystemRoles.isEmpty()) {
                Set<SystemRole> systemRoles = new HashSet<>();
                for (String roleName : defaultSystemRoles) {
                    SystemRole role = SystemRole.valueOf(roleName);
                    systemRoles.add(role);
                    sandboxActivityLogService.systemUserRoleChange(user, role, true);
                }
                user.setSystemRoles(systemRoles);
            }
            // Set or Update Name
            user.setName(oauthUsername);

            if (!user.getEmail().equalsIgnoreCase(oauthUserEmail)) {
                // If the user's email is changing, merge any sandbox invites sent
                // to the new email to the existing, "full" user
                sandboxInviteService.mergeSandboxInvites(user, oauthUserEmail);
            }
            user.setEmail(oauthUserEmail);
            userService.save(user);
        }
        return user;
    }
}
