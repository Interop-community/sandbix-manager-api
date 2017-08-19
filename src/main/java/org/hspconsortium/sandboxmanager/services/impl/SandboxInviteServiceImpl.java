package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.InviteStatus;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.SandboxInvite;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.repositories.SandboxInviteRepository;
import org.hspconsortium.sandboxmanager.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class SandboxInviteServiceImpl implements SandboxInviteService {

    private final SandboxInviteRepository repository;
    private final UserService userService;
    private final SandboxService sandboxService;
    private final EmailService emailService;
    private final SandboxActivityLogService sandboxActivityLogService;

    @Inject
    public SandboxInviteServiceImpl(final SandboxInviteRepository repository,final UserService userService,
                                    final SandboxService sandboxService, final EmailService emailService,
                                    final SandboxActivityLogService sandboxActivityLogService) {
        this.repository = repository;
        this.userService = userService;
        this.sandboxService = sandboxService;
        this.emailService = emailService;
        this.sandboxActivityLogService = sandboxActivityLogService;
    }

    @Override
    @Transactional
    public SandboxInvite save(final SandboxInvite sandboxInvite) {
        return repository.save(sandboxInvite);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final SandboxInvite sandboxInvite) {
        delete(sandboxInvite.getId());
    }

    @Override
    @Transactional
    public SandboxInvite create(final SandboxInvite sandboxInvite) throws IOException {
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxInvite.getSandbox().getSandboxId());
        User invitedBy = userService.findBySbmUserId(sandboxInvite.getInvitedBy().getSbmUserId());
        User checkInvitee = null;
        if (sandboxInvite.getInvitee().getSbmUserId() != null) {
            checkInvitee = userService.findBySbmUserId(sandboxInvite.getInvitee().getSbmUserId());
        }
        if (checkInvitee == null || !sandboxService.isSandboxMember(sandbox, checkInvitee)) {  // Don't invite a user already in the sandbox
            sandboxInvite.setSandbox(sandbox);
            sandboxInvite.setInvitedBy(invitedBy);
            sandboxInvite.setInviteTimestamp(new Timestamp(new Date().getTime()));
            sandboxInvite.setStatus(InviteStatus.PENDING);

            // Invitee may not exist, create if needed
            User invitee;
            if (sandboxInvite.getInvitee().getSbmUserId() != null) {
                invitee = userService.findBySbmUserId(sandboxInvite.getInvitee().getSbmUserId());
            } else {
                invitee = userService.findByUserEmail(sandboxInvite.getInvitee().getEmail());
            }

            // If no user exists for the invitee, create one
            if (invitee == null) {
                sandboxInvite.getInvitee().setCreatedTimestamp(new Timestamp(new Date().getTime()));
                invitee = userService.save(sandboxInvite.getInvitee());
            }
            sandboxInvite.setInvitee(invitee);

            // Send an Email
            emailService.sendEmail(invitedBy, invitee, sandboxInvite.getSandbox());
            sandboxActivityLogService.sandboxUserInvited(sandbox, invitedBy, invitee);
            return save(sandboxInvite);
        }
        return null;
    }

    @Override
    @Transactional
    public void mergeSandboxInvites(final User user, final String oauthUserEmail) {
        User tempUser = userService.findByUserEmail(oauthUserEmail);

        // If there's already a "temp" user with the new email, move any invites to the "full" user
        if (tempUser != null && tempUser.getSbmUserId() == null) {
            List<SandboxInvite> invites = findInvitesByInviteeEmail(oauthUserEmail);
            for (SandboxInvite invite : invites) {
                invite.setInvitee(user);
                save(invite);
            }
            userService.delete(tempUser);
        }
    }

    @Override
    public SandboxInvite getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeId(final String inviteeId) {
        return repository.findInvitesByInviteeId(inviteeId);
    }

    @Override
    public List<SandboxInvite> findInvitesBySandboxId(final String sandboxId) {
        return repository.findInvitesBySandboxId(sandboxId);
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeIdAndSandboxId(final String inviteeId, final String sandboxId) {
        return repository.findInvitesByInviteeIdAndSandboxId(inviteeId, sandboxId);
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeEmailAndSandboxId(final String inviteeEmail, final String sandboxId) {
        return repository.findInvitesByInviteeEmailAndSandboxId(inviteeEmail, sandboxId);
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeEmail(final String inviteeEmail) {
        return repository.findInvitesByInviteeEmail(inviteeEmail);
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeIdAndStatus(final String inviteeId, final InviteStatus status) {
        return repository.findInvitesByInviteeIdAndStatus(inviteeId, status);
    }

    @Override
    public List<SandboxInvite> findInvitesBySandboxIdAndStatus(final String sandboxId, final InviteStatus status) {
        return repository.findInvitesBySandboxIdAndStatus(sandboxId, status);
    }
}


