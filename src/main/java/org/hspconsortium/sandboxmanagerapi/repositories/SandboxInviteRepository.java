package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.SandboxInvite;
import org.hspconsortium.sandboxmanagerapi.model.InviteStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SandboxInviteRepository extends CrudRepository<SandboxInvite, Integer> {
    public List<SandboxInvite> findInvitesByInviteeId(@Param("inviteeId") String inviteeId);
    public List<SandboxInvite> findInvitesBySandboxId(@Param("sandboxId") String sandboxId);
    public List<SandboxInvite> findInvitesByInviteeIdAndSandboxId(@Param("inviteeId") String inviteeId, @Param("sandboxId") String sandboxId);
    public List<SandboxInvite> findInvitesByInviteeEmailAndSandboxId(@Param("inviteeEmail") String inviteeEmail, @Param("sandboxId") String sandboxId);
    public List<SandboxInvite> findInvitesByInviteeEmail(@Param("inviteeEmail") String inviteeEmail);
    public List<SandboxInvite> findInvitesByInviteeIdAndStatus(@Param("inviteeId") String inviteeId, @Param("status") InviteStatus status);
    public List<SandboxInvite> findInvitesBySandboxIdAndStatus(@Param("sandboxId") String sandboxId, @Param("status") InviteStatus status);
}
