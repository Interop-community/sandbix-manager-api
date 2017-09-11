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

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxInvite;
import org.hspconsortium.sandboxmanagerapi.model.SystemRole;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(AdminController.class.getName());

    private final UserService userService;
    private final SandboxService sandboxService;
    private final AdminService adminService;
    private final SandboxInviteService sandboxInviteService;

    @Inject
    public AdminController(final UserService userService, final OAuthService oAuthService,
                           final AdminService adminService, final SandboxService sandboxService,
                           final SandboxInviteService sandboxInviteService) {
        super(oAuthService);
        this.userService = userService;
        this.sandboxService = sandboxService;
        this.sandboxInviteService = sandboxInviteService;
        this.adminService = adminService;
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json", params = {"interval"})
    public @ResponseBody String getSandboxStatistics(HttpServletRequest request, @RequestParam(value = "interval") String intervalDays) throws UnsupportedEncodingException {
        User user = userService.findBySbmUserId(getSystemUserId(request));
        checkUserSystemRole(user, SystemRole.ADMIN);
        return adminService.getSandboxStatistics(intervalDays);
    }

    // Admin Level Sandbox Delete (originally for cleaning up orphaned sandboxes
    @RequestMapping(value = "/sandbox/{id}", method = RequestMethod.DELETE, produces ="application/json")
    @Transactional
    public void deleteSandboxById(HttpServletRequest request, @PathVariable String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        User user = userService.findBySbmUserId(getSystemUserId(request));
        checkUserSystemRole(user, SystemRole.ADMIN);

        //delete sandbox invites
        List<SandboxInvite> invites = sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId());
        for (SandboxInvite invite : invites) {
            sandboxInviteService.delete(invite);
        }

        sandboxService.delete(sandbox, oAuthService.getBearerToken(request), user);
    }

}
