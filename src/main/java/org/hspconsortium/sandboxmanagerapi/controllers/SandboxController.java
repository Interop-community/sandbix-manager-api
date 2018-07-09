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
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/sandbox")
public class SandboxController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxController.class.getName());

    private final SandboxService sandboxService;
    private final UserService userService;
    private final SandboxInviteService sandboxInviteService;
    private final AnalyticsService analyticsService;

    @Inject
    public SandboxController(final SandboxService sandboxService, final UserService userService,
                             final SandboxInviteService sandboxInviteService, final OAuthService oAuthService,
                             final AnalyticsService analyticsService) {
        super(oAuthService);
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.sandboxInviteService = sandboxInviteService;
        this.analyticsService = analyticsService;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody Sandbox createSandbox(HttpServletRequest request, @RequestBody final Sandbox sandbox) throws UnsupportedEncodingException{

        Sandbox existingSandbox = sandboxService.findBySandboxId(sandbox.getSandboxId());
        if (existingSandbox != null) {
            return existingSandbox;
        }

        LOGGER.info("Creating sandbox: " + sandbox.getName());
        checkCreatedByIsCurrentUserAuthorization(request, sandbox.getCreatedBy().getSbmUserId());
        User user = userService.findBySbmUserId(sandbox.getCreatedBy().getSbmUserId());
        checkUserSystemRole(user, SystemRole.CREATE_SANDBOX);
        return sandboxService.create(sandbox, user, oAuthService.getBearerToken(request));
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
        User user = userService.findBySbmUserId(getSystemUserId(request));
        if (!sandboxService.isSandboxMember(sandbox, user) && sandbox.getVisibility() == Visibility.PUBLIC ) {
            sandboxService.addMember(sandbox, user);
        }
        checkSandboxUserReadAuthorization(request, sandbox);
        analyticsService.recordUserAccessHistory(sandbox, user);
        return sandbox;
    }

//    public @ResponseBody List<SmartApp>

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public void deleteSandboxById(HttpServletRequest request, @PathVariable String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        User user = userService.findBySbmUserId(getSystemUserId(request));
        checkSystemUserDeleteSandboxAuthorization(request, sandbox, user);

        //delete sandbox invites
        List<SandboxInvite> invites = sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId());
        for (SandboxInvite invite : invites) {
            sandboxInviteService.delete(invite);
        }

        sandboxService.delete(sandbox, oAuthService.getBearerToken(request));
    }

    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public void updateSandboxById(HttpServletRequest request, @PathVariable String id, @RequestBody final Sandbox sandbox) throws UnsupportedEncodingException {
        User user = userService.findBySbmUserId(getSystemUserId(request));
        checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
        sandboxService.update(sandbox, user, oAuthService.getBearerToken(request));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"userId"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    List<Sandbox> getSandboxesByMember(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        return sandboxService.getAllowedSandboxes(user);
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, params = {"removeUserId"})
    @Transactional
    public void removeSandboxMember(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "removeUserId") String userIdEncoded) throws UnsupportedEncodingException {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        User user = userService.findBySbmUserId(getSystemUserId(request));

        checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
        String removeUserId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        User removedUser = userService.findBySbmUserId(removeUserId);
        if(canRemoveUser(sandbox, removedUser)){
            sandboxService.removeMember(sandbox, removedUser, oAuthService.getBearerToken(request));
        }
    }


    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, params = {"editUserRole", "role", "add"})
    @Transactional
    public void updateSandboxMemberRole(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "editUserRole") String userIdEncoded,
                @RequestParam(value = "role") Role role, @RequestParam(value = "add") boolean add) throws UnsupportedEncodingException {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        User user = userService.findBySbmUserId(getSystemUserId(request));

        checkSystemUserCanModifySandboxAuthorization(request, sandbox, user);
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

    @PostMapping(value = "/{id}/login", params = {"userId"})
    @Transactional
    public void sandboxLogin(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException{
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        checkUserAuthorization(request, userId);
        sandboxService.sandboxLogin(id, userId);
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
