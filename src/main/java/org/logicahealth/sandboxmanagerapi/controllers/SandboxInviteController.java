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
import org.logicahealth.sandboxmanagerapi.dto.SecuredSandboxInviteDto;
import org.logicahealth.sandboxmanagerapi.model.InviteStatus;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.SandboxInvite;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/sandboxinvite")
public class SandboxInviteController {
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxInviteController.class.getName());

    @Autowired
    private ModelMapper modelMapper;

    private final SandboxInviteService sandboxInviteService;
    private final UserService userService;
    private final SandboxService sandboxService;
    private final EmailService emailService;
    private final SandboxActivityLogService sandboxActivityLogService;
    private final AuthorizationService authorizationService;
    private final UserAccessHistoryService userAccessHistoryService;

    @Inject
    public SandboxInviteController(final SandboxInviteService sandboxInviteService, final UserService userService,
                                   final SandboxService sandboxService,
                                   final EmailService emailService, final SandboxActivityLogService sandboxActivityLogService,
                                   final AuthorizationService authorizationService,
                                   final UserAccessHistoryService userAccessHistoryService) {
        this.sandboxInviteService = sandboxInviteService;
        this.userService = userService;
        this.sandboxService = sandboxService;
        this.emailService = emailService;
        this.sandboxActivityLogService = sandboxActivityLogService;
        this.authorizationService = authorizationService;
        this.userAccessHistoryService = userAccessHistoryService;
    }

    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody
    SandboxInvite createOrUpdateSandboxInvite(HttpServletRequest request, @RequestBody final SandboxInvite sandboxInvite) throws IOException {

        LOGGER.info("createOrUpdateSandboxInvite");
        
        // Make sure the inviter has rights to this sandbox
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxInvite.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        authorizationService.checkSystemUserCanManageSandboxUsersAuthorization(request, sandbox, user);
        SandboxInvite sandboxInviteReturned = new SandboxInvite();

        // Check for an existing invite for this invitee
        List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesByInviteeIdAndSandboxId(sandboxInvite.getInvitee().getSbmUserId(), sandboxInvite.getSandbox().getSandboxId());
        if (sandboxInvites.isEmpty()) {
            sandboxInvites = sandboxInviteService.findInvitesByInviteeEmailAndSandboxId(sandboxInvite.getInvitee().getEmail(), sandboxInvite.getSandbox().getSandboxId());
        }

        // Resend
        if (!sandboxInvites.isEmpty() && !sandboxService.isSandboxMember(sandbox, sandboxInvite.getInvitee())) {
            SandboxInvite existingSandboxInvite = sandboxInvites.get(0);
            existingSandboxInvite.setStatus(InviteStatus.PENDING);
            existingSandboxInvite.setInviteTimestamp(new Timestamp(new Date().getTime()));
            sandboxInviteReturned = sandboxInviteService.save(existingSandboxInvite);

            // Send an Email
            User inviter = userService.findBySbmUserId(sandboxInvite.getInvitedBy().getSbmUserId());
            User invitee;
            if (sandboxInvite.getInvitee().getSbmUserId() != null) {
                invitee = userService.findBySbmUserId(sandboxInvite.getInvitee().getSbmUserId());
            } else {
                invitee = userService.findByUserEmail(sandboxInvite.getInvitee().getEmail());
            }
            emailService.sendEmail(inviter, invitee, sandboxInvite.getSandbox(), sandboxInviteReturned.getId());
        } else if (sandboxInvites.isEmpty()) { // Create
            // Make sure the inviter is the authenticated user
            User invitedBy = userService.findBySbmUserId(sandboxInvite.getInvitedBy().getSbmUserId());
            authorizationService.checkUserAuthorization(request, invitedBy.getSbmUserId());
            sandboxInviteReturned = sandboxInviteService.create(sandboxInvite);
        }
        return sandboxInviteReturned;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"sbmUserId", "status"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    List<SecuredSandboxInviteDto> getSandboxInvitesByInvitee(HttpServletRequest request, @RequestParam(value = "sbmUserId") String sbmUserIdEncoded,
                                                             @RequestParam(value = "status") InviteStatus status) throws UnsupportedEncodingException {
        
        LOGGER.info("getSandboxInvitesByInvitee");
        
        String sbmUserId = java.net.URLDecoder.decode(sbmUserIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, sbmUserId);
//        if (status == null) {
//            List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesByInviteeId(sbmUserId);
//            if (sandboxInvites != null) {
//                return sandboxInvites;
//            }
//        } else {
        List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesByInviteeIdAndStatus(sbmUserId, status);
        if (sandboxInvites != null) {
            return sandboxInvites
                    .stream()
                    .map(sandboxInvite -> modelMapper.map(sandboxInvite, SecuredSandboxInviteDto.class))
                    .collect(Collectors.toList());
        }
//        }
        return Collections.emptyList();
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"sandboxId", "status"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    List<SecuredSandboxInviteDto> getSandboxInvitesBySandbox(HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId,
                                                             @RequestParam(value = "status") InviteStatus status) throws UnsupportedEncodingException {
        
        LOGGER.info("getSandboxInvitesBySandbox");
        
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        authorizationService.checkSystemUserCanManageSandboxUsersAuthorization(request, sandbox, user);

//        if (status == null) {
//            List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesBySandboxId(sandboxId);
//            if (sandboxInvites != null) {
//                return sandboxInvites;
//            }
//        } else {
        List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesBySandboxIdAndStatus(sandboxId, status);
        if (sandboxInvites != null) {
            return sandboxInvites
                    .stream()
                    .map(sandboxInvite -> modelMapper.map(sandboxInvite, SecuredSandboxInviteDto.class))
                    .collect(Collectors.toList());
        }
//        }
        return Collections.emptyList();
    }

    @PutMapping(value = "/{id}", params = {"status"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    void updateSandboxInvite(HttpServletRequest request, @PathVariable Integer id, @RequestParam(value = "status") InviteStatus status) throws UnsupportedEncodingException {
        
        LOGGER.info("updateSandboxInvite");
        
        SandboxInvite sandboxInvite = sandboxInviteService.getById(id);
        if (sandboxInvite == null) {
            throw new ResourceNotFoundException("SandboxInvite not found.");
        }
        if (sandboxInvite.getStatus() == InviteStatus.PENDING && (status == InviteStatus.ACCEPTED || status == InviteStatus.REJECTED)) {

            // Only invitee can accept or reject
            User invitee = userService.findBySbmUserId(sandboxInvite.getInvitee().getSbmUserId());
            if (invitee == null) {
                throw new ResourceNotFoundException("Invitee not found.");
            }
            authorizationService.checkUserAuthorization(request, invitee.getSbmUserId());

            if (status == InviteStatus.REJECTED) {
                sandboxActivityLogService.sandboxUserInviteRejected(sandboxInvite.getSandbox(), sandboxInvite.getInvitee());
                sandboxInvite.setStatus(InviteStatus.REJECTED);
                sandboxInviteService.save(sandboxInvite);
                return;
            }

            Sandbox sandbox = sandboxService.findBySandboxId(sandboxInvite.getSandbox().getSandboxId());
            if(!sandboxService.isSandboxMember(sandbox, invitee)) {
                sandboxService.addMember(sandbox, invitee);
            }
            sandboxActivityLogService.sandboxUserInviteAccepted(sandbox, invitee);
            userAccessHistoryService.saveUserAccessInstance(sandbox, invitee);

            sandboxInvite.setStatus(status);
            sandboxInviteService.save(sandboxInvite);
        } else if ((sandboxInvite.getStatus() == InviteStatus.PENDING || sandboxInvite.getStatus() == InviteStatus.REJECTED) && status == InviteStatus.REVOKED ) {

            // Revoking Invite
            User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
            authorizationService.checkSystemUserCanManageSandboxUsersAuthorization(request, sandboxInvite.getSandbox(), user);
            sandboxActivityLogService.sandboxUserInviteRevoked(sandboxInvite.getSandbox(), user);
            sandboxInvite.setStatus(status);
            sandboxInviteService.save(sandboxInvite);
        }
    }
}
