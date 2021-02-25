package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.InviteStatus;
import org.logicahealth.sandboxmanagerapi.model.SandboxInvite;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SandboxInviteRepository extends CrudRepository<SandboxInvite, Integer> {
    List<SandboxInvite> findInvitesByInviteeId(@Param("inviteeId") String inviteeId);
    List<SandboxInvite> findInvitesBySandboxId(@Param("sandboxId") String sandboxId);
    List<SandboxInvite> findInvitesByInviteeIdAndSandboxId(@Param("inviteeId") String inviteeId, @Param("sandboxId") String sandboxId);
    List<SandboxInvite> findInvitesByInviteeEmailAndSandboxId(@Param("inviteeEmail") String inviteeEmail, @Param("sandboxId") String sandboxId);
    List<SandboxInvite> findInvitesByInviteeEmail(@Param("inviteeEmail") String inviteeEmail);
    List<SandboxInvite> findInvitesByInviteeIdAndStatus(@Param("inviteeId") String inviteeId, @Param("status") InviteStatus status);
    List<SandboxInvite> findInvitesBySandboxIdAndStatus(@Param("sandboxId") String sandboxId, @Param("status") InviteStatus status);
    void deleteAllByInviteeIn(List<User> staleInvitees);
}
