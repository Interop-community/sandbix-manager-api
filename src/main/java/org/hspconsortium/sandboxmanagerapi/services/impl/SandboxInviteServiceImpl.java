package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.InviteStatus;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxInvite;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxInviteRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class SandboxInviteServiceImpl implements SandboxInviteService {

    private SandboxInviteRepository repository;
    private UserService userService;
    private SandboxService sandboxService;
    private EmailService emailService;
    private SandboxActivityLogService sandboxActivityLogService;
    private RuleService ruleService;

    @Autowired
    public SandboxInviteServiceImpl(final SandboxInviteRepository repository) {
        this.repository = repository;
    }

    public SandboxInviteServiceImpl() {
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Inject
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Inject
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Inject
    public void setSandboxActivityLogService(SandboxActivityLogService sandboxActivityLogService) {
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
        if (!ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId())) {
            return null;
        }
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

            SandboxInvite sandboxInviteSaved = save(sandboxInvite);

            // Send an Email
            emailService.sendEmail(invitedBy, invitee, sandboxInvite.getSandbox(), sandboxInviteSaved.getId());

            sandboxActivityLogService.sandboxUserInvited(sandbox, invitedBy, invitee);
            return sandboxInviteSaved;
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
        return repository.findOne(id);
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeId(final String inviteeId) {
        List<SandboxInvite> sandboxInvites = repository.findInvitesByInviteeId(inviteeId);
//        clearSandboxInformation(sandboxInvites);
        return sandboxInvites;
    }

    @Override
    public List<SandboxInvite> findInvitesBySandboxId(final String sandboxId) {
        List<SandboxInvite> sandboxInvites = repository.findInvitesBySandboxId(sandboxId);
//        clearSandboxInformation(sandboxInvites);
        return sandboxInvites;
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeIdAndSandboxId(final String inviteeId, final String sandboxId) {
        List<SandboxInvite> sandboxInvites = repository.findInvitesByInviteeIdAndSandboxId(inviteeId, sandboxId);
//        clearSandboxInformation(sandboxInvites);
        return sandboxInvites;
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeEmailAndSandboxId(final String inviteeEmail, final String sandboxId) {
        List<SandboxInvite> sandboxInvites = repository.findInvitesByInviteeEmailAndSandboxId(inviteeEmail, sandboxId);
//        clearSandboxInformation(sandboxInvites);
        return sandboxInvites;
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeEmail(final String inviteeEmail) {
        List<SandboxInvite> sandboxInvites = repository.findInvitesByInviteeEmail(inviteeEmail);
//        clearSandboxInformation(sandboxInvites);
        return sandboxInvites;
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeIdAndStatus(final String inviteeId, final InviteStatus status) {
        List<SandboxInvite> sandboxInvites = repository.findInvitesByInviteeIdAndStatus(inviteeId, status);
//        clearSandboxInformation(sandboxInvites);
        return sandboxInvites;
    }

    @Override
    public List<SandboxInvite> findInvitesBySandboxIdAndStatus(final String sandboxId, final InviteStatus status) {
        List<SandboxInvite> sandboxInvites = repository.findInvitesBySandboxIdAndStatus(sandboxId, status);
//        clearSandboxInformation(sandboxInvites);
        return sandboxInvites;
    }

    private void clearSandboxInformation(List<SandboxInvite> sandboxInvites) {
        for (SandboxInvite sandboxInvite: sandboxInvites) {
            sandboxInvite.getInvitedBy().getSandboxes().clear();
        }
    }

}


