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
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
    private final SandboxEncryptionService sandboxEncryptionService;

    public static final int UNSECURE_PROTOCOL_PORT = 80;
    public static final String SECURE_PROTOCOL = "https";
    public static final int SECURE_PROTOCOL_PORT = 443;

    @Inject
    public SandboxController(final SandboxService sandboxService, final UserService userService,
                             final SandboxInviteService sandboxInviteService,
                             final UserAccessHistoryService userAccessHistoryService, final SandboxActivityLogService sandboxActivityLogService,
                             final AuthorizationService authorizationService, SandboxEncryptionService sandboxEncryptionService) {
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.sandboxInviteService = sandboxInviteService;
        this.userAccessHistoryService = userAccessHistoryService;
        this.sandboxActivityLogService = sandboxActivityLogService;
        this.authorizationService = authorizationService;
        this.sandboxEncryptionService = sandboxEncryptionService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody
    Sandbox createSandbox(HttpServletRequest request, @RequestBody final Sandbox sandbox) throws UnsupportedEncodingException {

        Sandbox existingSandbox = sandboxService.findBySandboxId(sandbox.getSandboxId());
        if (existingSandbox != null) {
            throw new IllegalArgumentException("Sandbox with id " + sandbox.getSandboxId() + " already exists.");
        }
        authorizationService.checkUserAuthorization(request, sandbox.getCreatedBy().getSbmUserId());
        LOGGER.info("Creating sandbox: " + sandbox.getName());
        User user = userService.findBySbmUserId(sandbox.getCreatedBy().getSbmUserId());
        authorizationService.checkUserSystemRole(user, SystemRole.CREATE_SANDBOX);
        return sandboxService.create(sandbox, user, authorizationService.getBearerToken(request));
    }

    @PostMapping(value = "/clone", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void cloneSandbox(HttpServletRequest request, @RequestBody final HashMap<String, Sandbox> sandboxes) throws UnsupportedEncodingException {
        Sandbox newSandbox = sandboxes.get("newSandbox");
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
        sandboxService.clone(newSandbox, clonedSandbox.getSandboxId(), user, authorizationService.getBearerToken(request));
    }

    @GetMapping(value = "/creationStatus/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    SandboxCreationStatusQueueOrder getSandboxCreationStatus(HttpServletRequest request, @PathVariable(value = "id") String sandboxId) {
        authorizationService.checkUserAuthorization(request, authorizationService.getSystemUserId(request));
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        return sandboxService.getQueuedCreationStatus(sandbox.getSandboxId());
    }

    @GetMapping(value = "/download/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void downloadSandboxAndApps(HttpServletRequest request, @PathVariable(value = "id") String sandboxId, HttpServletResponse response) throws IOException {
        var sbmUserId = authorizationService.getSystemUserId(request);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        checkExportAllowedOnlyForAdminUsers(sandbox, userService.findBySbmUserId(sbmUserId));
        sandboxService.exportSandbox(sandbox, sbmUserId, authorizationService.getBearerToken(request), getServer(request));
    }

    private void checkExportAllowedOnlyForAdminUsers(Sandbox sandbox, User user) {
        var adminUser = sandbox.getUserRoles()
                               .stream()
                               .filter(userRole -> userRole.getUser().getSbmUserId().equals(user.getSbmUserId()))
                               .filter(userRole -> userRole.getRole() == Role.ADMIN)
                               .findFirst();
        if (adminUser.isEmpty()) {
            throw new UnauthorizedUserException("User is not a sandbox administrator");
        }
    }

    @PostMapping(value = "/import")
    @ResponseStatus(HttpStatus.CREATED)
    public void importSandboxAndApps(HttpServletRequest request, @RequestParam("zipFile") MultipartFile zipFile) {
        try{
        var requestingUser = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkUserAuthorization(request, requestingUser.getSbmUserId());
        sandboxService.importSandbox(zipFile, requestingUser, authorizationService.getBearerToken(request), getServer(request));
        }
        catch(Exception e)
        {
            e.printStackTrace(System.out);
        }
    }

    private String getServer(HttpServletRequest request) {
        var server = request.getScheme() + "://" + request.getServerName();
        var serverPort = request.getServerPort();
        if (server.startsWith(SECURE_PROTOCOL) && serverPort == SECURE_PROTOCOL_PORT || !server.startsWith(SECURE_PROTOCOL) && serverPort == UNSECURE_PROTOCOL_PORT) {
            return server;
        }
        return server + ":" + serverPort;
    }

    @PostMapping(value = "/decryptSignature")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody String decryptSignature(@RequestBody String signature) {
        return sandboxEncryptionService.decryptSignature(signature);
    }

    @GetMapping(params = {"lookUpId"}, produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    String checkForSandboxById(@RequestParam(value = "lookUpId") String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox != null) {
            return "{\"sandboxId\": \"" + sandbox.getSandboxId() + "\"}";
        }
        return null;
    }

    @GetMapping(params = {"sandboxId"}, produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    String getSandboxById(@RequestParam(value = "sandboxId") String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox != null) {
            return "{\"sandboxId\": \"" + sandbox.getSandboxId() + "\",\"apiEndpointIndex\": \"" + sandbox.getApiEndpointIndex() + "\",\"allowOpenAccess\": \"" + sandbox.isAllowOpenAccess() + "\"}";
        }
        return null;
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody
    Sandbox getSandboxById(HttpServletRequest request, @PathVariable String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (!sandboxService.isSandboxMember(sandbox, user) && sandbox.getVisibility() == Visibility.PUBLIC) {
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
        if (canRemoveUser(sandbox, removedUser)) {
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
    public void sandboxLogin(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(userId);
        userAccessHistoryService.saveUserAccessInstance(sandbox, user);

        if (!sandboxService.isSandboxMember(sandbox, user) && sandbox.getVisibility() == Visibility.PUBLIC) {
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
     * - not an {@link Role#ADMIN } user
     * - more than one {@link Role#ADMIN} users exist
     *
     * @param sandbox     - the sandbox to remove a user from
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
