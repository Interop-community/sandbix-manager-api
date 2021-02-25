package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.InviteStatus;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.SandboxInvite;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.repositories.SandboxInviteRepository;
import org.logicahealth.sandboxmanagerapi.services.*;
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
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(List<User> invitees) {
        repository.deleteAllByInviteeIn(invitees);
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
        return repository.findById(id).orElse(null);
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


