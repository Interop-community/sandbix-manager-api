package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.InviteStatus;
import org.hspconsortium.sandboxmanagerapi.model.SandboxInvite;
import org.hspconsortium.sandboxmanagerapi.model.User;

import java.io.IOException;
import java.util.List;

public interface SandboxInviteService {

    SandboxInvite save(final SandboxInvite sandboxInvite);

    void delete(final int id);

    void delete(SandboxInvite sandboxInvite);

    SandboxInvite create(SandboxInvite sandboxInvite) throws IOException;

    void mergeSandboxInvites(final User user, final String oauthUserEmail);

    SandboxInvite getById(final int id);

    List<SandboxInvite> findInvitesByInviteeId(final String inviteeId);

    List<SandboxInvite> findInvitesBySandboxId(final String sandboxId);

    List<SandboxInvite> findInvitesByInviteeIdAndSandboxId(final String inviteeId, final String sandboxId);

    List<SandboxInvite> findInvitesByInviteeEmailAndSandboxId(final String inviteeEmail, final String sandboxId);

    List<SandboxInvite> findInvitesByInviteeEmail(final String inviteeEmail);

    List<SandboxInvite> findInvitesByInviteeIdAndStatus(final String inviteeId, final InviteStatus status);

    List<SandboxInvite> findInvitesBySandboxIdAndStatus(final String sandboxId, final InviteStatus status);
}
