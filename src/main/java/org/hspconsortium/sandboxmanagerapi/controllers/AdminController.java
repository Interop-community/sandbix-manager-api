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
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxInvite;
import org.hspconsortium.sandboxmanagerapi.model.SystemRole;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final SandboxService sandboxService;
    private final AdminService adminService;
    private final SandboxInviteService sandboxInviteService;
    private final AuthorizationService authorizationService;

    @Inject
    public AdminController(final UserService userService,
                           final AdminService adminService, final SandboxService sandboxService,
                           final SandboxInviteService sandboxInviteService, final AuthorizationService authorizationService) {
        this.userService = userService;
        this.sandboxService = sandboxService;
        this.sandboxInviteService = sandboxInviteService;
        this.adminService = adminService;
        this.authorizationService = authorizationService;
    }

    // Admin Level Sandbox Delete (originally for cleaning up orphaned sandboxes
    @DeleteMapping(value = "/sandbox/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public void deleteSandboxById(HttpServletRequest request, @PathVariable String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);

        //delete sandbox invites
        List<SandboxInvite> invites = sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId());
        invites.forEach(sandboxInviteService::delete);

        sandboxService.delete(sandbox, authorizationService.getBearerToken(request), user, false);
    }

    // SS Admin Level Sandbox Delete to delete sandboxes not used in a year
    @DeleteMapping(value = "/deleteUnused")
    @ResponseBody
    public Set<String> deleteUnusedSandboxes(HttpServletRequest request) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);

        return adminService.deleteUnusedSandboxes(user, authorizationService.getBearerToken(request));

    }

    @GetMapping(value = "/sandbox-differences/$list")
    @Transactional
    public HashMap<String, Object> listSandboxManagerReferenceApiDiscrepencies(HttpServletRequest request) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return adminService.syncSandboxManagerandReferenceApi(false, authorizationService.getBearerToken(request));
    }

    @GetMapping(value = "/sandbox-differences/$sync")
    @Transactional
    public HashMap<String, Object> syncSandboxManagerReferenceApiDiscrepencies(HttpServletRequest request) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return adminService.syncSandboxManagerandReferenceApi(true, authorizationService.getBearerToken(request));
    }

}
