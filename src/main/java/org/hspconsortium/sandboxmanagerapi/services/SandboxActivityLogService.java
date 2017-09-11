package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;

import java.sql.Timestamp;
import java.util.List;

public interface SandboxActivityLogService {

    SandboxActivityLog save(final SandboxActivityLog sandboxActivityLog);

    void delete(final SandboxActivityLog sandboxActivityLog);

    SandboxActivityLog sandboxCreate(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxLogin(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxDelete(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserInviteAccepted(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserInviteRevoked(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserInviteRejected(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserRemoved(final Sandbox sandbox, final User user, final User removedUser);

    SandboxActivityLog sandboxUserInvited(final Sandbox sandbox, final User user, final User invitedUser);

    SandboxActivityLog sandboxOpenEndpoint(final Sandbox sandbox, final User user, final Boolean openEndpoint);

    SandboxActivityLog sandboxUserAdded(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserRoleChange(final Sandbox sandbox, final User user, final Role role, final boolean roleAdded);

    SandboxActivityLog sandboxImport(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxReset(final Sandbox sandbox, final User user);

    SandboxActivityLog systemUserCreated(final Sandbox sandbox, final User user);

    SandboxActivityLog systemUserRoleChange(final User user, final SystemRole systemRole, final boolean roleAdded);

    SandboxActivityLog userDelete(final User user);

    List<SandboxActivityLog> findBySandboxId(final String sandboxId);

    List<SandboxActivityLog> findByUserSbmUserId(final String sbmUserId);

    List<SandboxActivityLog> findByUserId(final int userId);

    List<SandboxActivityLog> findBySandboxActivity(final SandboxActivity sandboxActivity);

    String intervalActive(final Timestamp intervalTime);
}
