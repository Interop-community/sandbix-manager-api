package org.logicahealth.sandboxmanagerapi.services;

import org.junit.Before;
import org.junit.Test;
import org.logicahealth.sandboxmanagerapi.model.InviteStatus;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.SandboxInvite;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.repositories.SandboxInviteRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.SandboxInviteServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SandboxInviteServiceTest {

    private SandboxInviteRepository repository = mock(SandboxInviteRepository.class);
    private UserService userService = mock(UserService.class);
    private SandboxService sandboxService = mock(SandboxService.class);
    private EmailService emailService = mock(EmailService.class);
    private SandboxActivityLogService sandboxActivityLogService = mock(SandboxActivityLogService.class);
    private RuleService ruleService = mock(RuleService.class);

    private SandboxInviteServiceImpl sandboxInviteService = new SandboxInviteServiceImpl(repository);

    private SandboxInvite sandboxInvite;
    private List<SandboxInvite> sandboxInvites;
    private Sandbox sandbox;
    private User invitee;
    private User inviter;
    private String oauthUserEmail = "oauthUser@Email.com";

    @Before
    public void setup() {
        sandboxInviteService.setEmailService(emailService);
        sandboxInviteService.setRuleService(ruleService);
        sandboxInviteService.setSandboxActivityLogService(sandboxActivityLogService);
        sandboxInviteService.setSandboxService(sandboxService);
        sandboxInviteService.setUserService(userService);

        sandboxInvite = new SandboxInvite();
        sandboxInvite.setId(1);
        sandboxInvites = new ArrayList<>();
        sandboxInvites.add(sandboxInvite);

        sandbox = new Sandbox();
        sandbox.setSandboxId("sandboxId");
        sandboxInvite.setSandbox(sandbox);

        invitee = new User();
        invitee.setSbmUserId("invitee");
        invitee.setEmail("invitee@email.com");
        inviter = new User();
        inviter.setSbmUserId("inviter");
        sandboxInvite.setInvitedBy(inviter);
        sandboxInvite.setInvitee(invitee);
    }

    @Test
    public void saveTest() {
        when(repository.save(sandboxInvite)).thenReturn(sandboxInvite);
        SandboxInvite returnedSandboxInvite = sandboxInviteService.save(sandboxInvite);
        assertEquals(sandboxInvite, returnedSandboxInvite);
    }

    @Test
    public void deleteTest() {
        sandboxInviteService.delete(sandboxInvite.getId());
        verify(repository).deleteById(sandboxInvite.getId());
    }

    @Test
    public void createTest() throws IOException {
        when(sandboxService.findBySandboxId(sandboxInvite.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(sandboxInvite.getInvitedBy().getSbmUserId())).thenReturn(inviter);
        when(ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId())).thenReturn(true);
        when(userService.findBySbmUserId(sandboxInvite.getInvitee().getSbmUserId())).thenReturn(invitee);
        when(sandboxInviteService.save(sandboxInvite)).thenReturn(sandboxInvite);
        sandboxInviteService.create(sandboxInvite);
        verify(emailService).sendEmail(any(), any(), any(), anyInt());
        verify(sandboxActivityLogService).sandboxUserInvited(any(), any(), any());
    }

    @Test
    public void createTestCantCreate() throws IOException {
        when(sandboxService.findBySandboxId(sandboxInvite.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(sandboxInvite.getInvitedBy().getSbmUserId())).thenReturn(inviter);
        when(ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId())).thenReturn(false);
        when(sandboxInviteService.save(sandboxInvite)).thenReturn(sandboxInvite);
        SandboxInvite returnedSandboxInvite = sandboxInviteService.create(sandboxInvite);
        assertNull(returnedSandboxInvite);
    }

    @Test
    public void createTestCheckInviteeIsNullAndAlreadyMember() throws IOException {
        when(sandboxService.findBySandboxId(sandboxInvite.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(sandboxInvite.getInvitedBy().getSbmUserId())).thenReturn(inviter);
        when(ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId())).thenReturn(true);
        when(userService.findBySbmUserId(sandboxInvite.getInvitee().getSbmUserId())).thenReturn(invitee);
        when(sandboxService.isSandboxMember(any(), any())).thenReturn(true);
        when(sandboxInviteService.save(sandboxInvite)).thenReturn(sandboxInvite);
        SandboxInvite returnedSandboxInvite = sandboxInviteService.create(sandboxInvite);
        assertNull(returnedSandboxInvite);
    }

    @Test
    public void createTestUseEmailToFindInvitee() throws IOException {
        invitee.setSbmUserId(null);
        sandboxInvite.setInvitee(invitee);
        when(sandboxService.findBySandboxId(sandboxInvite.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(sandboxInvite.getInvitedBy().getSbmUserId())).thenReturn(inviter);
        when(ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId())).thenReturn(true);
        when(userService.findByUserEmail(sandboxInvite.getInvitee().getEmail())).thenReturn(invitee);
        when(sandboxInviteService.save(sandboxInvite)).thenReturn(sandboxInvite);
        sandboxInviteService.create(sandboxInvite);
        verify(userService).findByUserEmail(sandboxInvite.getInvitee().getEmail());
    }

    @Test
    public void createTestCreateInvitee() throws IOException {
        invitee.setSbmUserId(null);
        sandboxInvite.setInvitee(invitee);
        when(sandboxService.findBySandboxId(sandboxInvite.getSandbox().getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(sandboxInvite.getInvitedBy().getSbmUserId())).thenReturn(inviter);
        when(ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId())).thenReturn(true);
        when(userService.findByUserEmail(sandboxInvite.getInvitee().getSbmUserId())).thenReturn(null);
        when(sandboxInviteService.save(sandboxInvite)).thenReturn(sandboxInvite);
        sandboxInviteService.create(sandboxInvite);
        verify(userService).save(any());
    }

    @Test
    public void mergeSandboxInvitesTest() {
        inviter.setSbmUserId(null);
        when(userService.findByUserEmail(oauthUserEmail)).thenReturn(inviter);
        sandboxInviteService.mergeSandboxInvites(invitee, oauthUserEmail);
        verify(userService).delete(any());
    }

    @Test
    public void getByIdTest() {
        when(repository.findById(sandboxInvite.getId())).thenReturn(of(sandboxInvite));
        SandboxInvite returnedSandboxInvite = sandboxInviteService.getById(sandboxInvite.getId());
        assertEquals(sandboxInvite, returnedSandboxInvite);
    }

    @Test
    public void findInvitesByInviteeIdTest() {
        when(repository.findInvitesByInviteeId(invitee.getSbmUserId())).thenReturn(sandboxInvites);
        List<SandboxInvite> returnedSandboxInvites = sandboxInviteService.findInvitesByInviteeId(invitee.getSbmUserId());
        assertEquals(sandboxInvites, returnedSandboxInvites);
    }

    @Test
    public void findInvitesBySandboxIdTest() {
        when(repository.findInvitesBySandboxId(sandbox.getSandboxId())).thenReturn(sandboxInvites);
        List<SandboxInvite> returnedSandboxInvites = sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId());
        assertEquals(sandboxInvites, returnedSandboxInvites);
    }

    @Test
    public void findInvitesByInviteeIdAndSandboxIdTest() {
        when(repository.findInvitesByInviteeIdAndSandboxId(invitee.getSbmUserId(), sandbox.getSandboxId())).thenReturn(sandboxInvites);
        List<SandboxInvite> returnedSandboxInvites = sandboxInviteService.findInvitesByInviteeIdAndSandboxId(invitee.getSbmUserId(), sandbox.getSandboxId());
        assertEquals(sandboxInvites, returnedSandboxInvites);
    }

    @Test
    public void findInvitesByInviteeEmailAndSandboxIdTest() {
        when(repository.findInvitesByInviteeEmailAndSandboxId(invitee.getEmail(), sandbox.getSandboxId())).thenReturn(sandboxInvites);
        List<SandboxInvite> returnedSandboxInvites = sandboxInviteService.findInvitesByInviteeEmailAndSandboxId(invitee.getEmail(), sandbox.getSandboxId());
        assertEquals(sandboxInvites, returnedSandboxInvites);
    }

    @Test
    public void findInvitesByInviteeEmailTest() {
        when(repository.findInvitesByInviteeEmail(invitee.getEmail())).thenReturn(sandboxInvites);
        List<SandboxInvite> returnedSandboxInvites = sandboxInviteService.findInvitesByInviteeEmail(invitee.getEmail());
        assertEquals(sandboxInvites, returnedSandboxInvites);
    }

    @Test
    public void findInvitesByInviteeIdAndStatusTest() {
        when(repository.findInvitesByInviteeIdAndStatus(invitee.getSbmUserId(), InviteStatus.PENDING)).thenReturn(sandboxInvites);
        List<SandboxInvite> returnedSandboxInvites = sandboxInviteService.findInvitesByInviteeIdAndStatus(invitee.getSbmUserId(), InviteStatus.PENDING);
        assertEquals(sandboxInvites, returnedSandboxInvites);
    }

    @Test
    public void findInvitesBySandboxIdAndStatusTest() {
        when(repository.findInvitesBySandboxIdAndStatus(sandbox.getSandboxId(), InviteStatus.PENDING)).thenReturn(sandboxInvites);
        List<SandboxInvite> returnedSandboxInvites = sandboxInviteService.findInvitesBySandboxIdAndStatus(sandbox.getSandboxId(), InviteStatus.PENDING);
        assertEquals(sandboxInvites, returnedSandboxInvites);
    }
}
