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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/sandbox")
public class SandboxController {
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxController.class.getName());

    @Value("${hspc.platform.templateSandboxIds}")
    private String[] templateSandboxIds;

    private final SandboxService sandboxService;
    private final UserService userService;
    private final SandboxInviteService sandboxInviteService;
    private final UserAccessHistoryService userAccessHistoryService;
    private final SandboxActivityLogService sandboxActivityLogService;
    private final AuthorizationService authorizationService;

    @Inject
    public SandboxController(final SandboxService sandboxService, final UserService userService,
                             final SandboxInviteService sandboxInviteService,
                             final UserAccessHistoryService userAccessHistoryService, final SandboxActivityLogService sandboxActivityLogService,
                             final AuthorizationService authorizationService) {
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.sandboxInviteService = sandboxInviteService;
        this.userAccessHistoryService = userAccessHistoryService;
        this.sandboxActivityLogService = sandboxActivityLogService;
        this.authorizationService = authorizationService;
    }

    @PostMapping(value = "/clone", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody Sandbox cloneSandbox(HttpServletRequest request, @RequestBody final HashMap<String, Sandbox> sandboxes) throws UnsupportedEncodingException {
        Sandbox newSandbox = sandboxes.get("newSandbox");
        // TODO: add code to add the sandboxName to the concurrentSandboxNames table
        if (newSandbox.getName().equalsIgnoreCase("test")) {
            throw new IllegalArgumentException("Test is a reserved sandbox name. Please change your sandbox name and try again.");
        }
        Sandbox clonedSandbox = sandboxes.get("clonedSandbox");
        // Don't need to check authorization of who created the template sandboxes
        if (!Arrays.asList(templateSandboxIds).contains(clonedSandbox.getSandboxId())) {
            authorizationService.checkUserAuthorization(request, clonedSandbox.getCreatedBy().getSbmUserId());
        }
        authorizationService.checkUserAuthorization(request, newSandbox.getCreatedBy().getSbmUserId());
        User user = userService.findBySbmUserId(newSandbox.getCreatedBy().getSbmUserId());
        authorizationService.checkUserSystemRole(user, SystemRole.CREATE_SANDBOX);
        return sandboxService.clone(newSandbox, clonedSandbox.getSandboxId(), user, authorizationService.getBearerToken(request));
    }

    @GetMapping(params = {"lookUpId"}, produces = APPLICATION_JSON_VALUE)
    public @ResponseBody String checkForSandboxById(@RequestParam(value = "lookUpId")  String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox != null) {
            return  "{\"sandboxId\": \"" + sandbox.getSandboxId() + "\"}";
        }
        return null;
    }

    @GetMapping(params = {"sandboxId"}, produces = APPLICATION_JSON_VALUE)
    public @ResponseBody String getSandboxById(@RequestParam(value = "sandboxId")  String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox != null) {
            return  "{\"sandboxId\": \"" + sandbox.getSandboxId() + "\",\"apiEndpointIndex\": \"" + sandbox.getApiEndpointIndex() + "\",\"allowOpenAccess\": \"" + sandbox.isAllowOpenAccess() + "\"}";
        }
        return null;
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody Sandbox getSandboxById(HttpServletRequest request, @PathVariable String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (!sandboxService.isSandboxMember(sandbox, user) && sandbox.getVisibility() == Visibility.PUBLIC ) {
            sandboxService.addMember(sandbox, user);
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        return sandbox;
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public void deleteSandboxById(HttpServletRequest request, @PathVariable String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkSystemUserDeleteSandboxAuthorization(request, sandbox, user);

        //delete sandbox invites
        List<SandboxInvite> invites = sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId());
        for (SandboxInvite invite : invites) {
            sandboxInviteService.delete(invite);
        }

        sandboxService.delete(sandbox, authorizationService.getBearerToken(request));
    }

    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public void updateSandboxById(HttpServletRequest request, @PathVariable String id, @RequestBody final Sandbox sandbox) throws UnsupportedEncodingException {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
        sandboxService.update(sandbox, user, authorizationService.getBearerToken(request));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"userId"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    List<Sandbox> getSandboxesByMember(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        return sandboxService.getAllowedSandboxes(user);
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, params = {"removeUserId"})
    @Transactional
    public void removeSandboxMember(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "removeUserId") String userIdEncoded) throws UnsupportedEncodingException {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkSystemUserCanRemoveUser(request, sandbox, user);
        String removeUserId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        User removedUser = userService.findBySbmUserId(removeUserId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        if(canRemoveUser(sandbox, removedUser)){
            sandboxService.removeMember(sandbox, removedUser, authorizationService.getBearerToken(request));
        }
    }


    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, params = {"editUserRole", "role", "add"})
    @Transactional
    public void updateSandboxMemberRole(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "editUserRole") String userIdEncoded,
                @RequestParam(value = "role") Role role, @RequestParam(value = "add") boolean add) throws UnsupportedEncodingException {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
        String modifyUserId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());

        User modifyUser = userService.findBySbmUserId(modifyUserId);
        // Don't allow the Sandbox creator to be modified
        // TODO: either keep or get rid of the following if/else statement
        if (!modifyUser.equals(sandbox.getCreatedBy())) {
            if (add) {
                sandboxService.addMemberRole(sandbox, modifyUser, role);
            } else {
                sandboxService.removeMemberRole(sandbox, modifyUser, role);
            }
        } else {
            throw new UnsupportedEncodingException("Can't change role of Sandbox creator.");
        }
    }

    @PutMapping(value = "/{id}/changePayer", params = {"newPayerId"})
    @Transactional
    public void changePayerForSandbox(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "newPayerId") String newPayerIdEncoded) throws UnsupportedEncodingException {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User newPayer = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        // TODO: maybe any admin can perform this task, not just the operating user
        if (!newPayer.getSbmUserId().equals(newPayerIdEncoded)) {
            throw new UserDeniedAuthorizationException("User not authorized.");
        }
        authorizationService.checkSystemUserCanModifySandboxAuthorization(request, sandbox, newPayer);
        sandboxService.changePayerForSandbox(sandbox, newPayer);
    }

    @PostMapping(value = "/{id}/login", params = {"userId"})
    @Transactional
    public void sandboxLogin(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException{
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(userId);
        userAccessHistoryService.saveUserAccessInstance(sandbox, user);

        if (!sandboxService.isSandboxMember(sandbox, user) && sandbox.getVisibility() == Visibility.PUBLIC ) {
            sandboxService.addMember(sandbox, user);
        }

        sandboxService.sandboxLogin(id, userId);
    }

    @GetMapping(value = "/all")
    @Transactional
    public @ResponseBody
    Iterable<Sandbox> getAllSandboxes(final HttpServletRequest request) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return sandboxService.findAll();
    }

    /**
     * A user can be removed from a sandbox if they are
     *  - not an {@link Role#ADMIN } user
     *  - more than one {@link Role#ADMIN} users exist
     *
     * @param sandbox - the sandbox to remove a user from
     * @param removedUser the user to remove
     * @return true if the user can be removed
     */
    private boolean canRemoveUser(Sandbox sandbox, User removedUser) {
        Optional<UserRole> first = sandbox.getUserRoles()
                .stream()
                .filter(u -> u.getUser().getId().equals(removedUser.getId())
                        && isAdminUser(u))
                .findFirst();

        return !first.isPresent() || sandbox.getUserRoles().stream().filter(this::isAdminUser).count() > 1;
    }

    private boolean isAdminUser(UserRole u) {
        return Role.ADMIN.equals(u.getRole());
    }
}
