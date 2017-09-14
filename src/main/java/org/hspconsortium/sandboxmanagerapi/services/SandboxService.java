package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;

public interface SandboxService {

    Sandbox save(final Sandbox sandbox);

    void delete(final int id);

    void delete(final Sandbox sandbox, final String bearerToken);

    void delete(final Sandbox sandbox, final String bearerToken, final User isAdmin);

    Sandbox create(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException;

    Sandbox update(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException;

    void removeMember(final Sandbox sandbox, final User user, final String bearerToken);

    void addMember(final Sandbox sandbox, final User user);

    void addMember(final Sandbox sandbox, final User user, final Role role);

    void addMemberRole(final Sandbox sandbox, final User user, final Role role);

    void removeMemberRole(final Sandbox sandbox, final User user, final Role role);

    boolean hasMemberRole(final Sandbox sandbox, final User user, final Role role);

    void addSandboxImport(final Sandbox sandbox, final SandboxImport sandboxImport);

    void reset(final Sandbox sandboxId, final String bearerToken);

    void sandboxLogin(final String sandboxId, final String userId);

    boolean isSandboxMember(final Sandbox sandbox, final User user);

    String getSandboxApiURL(final Sandbox sandbox);

    List<Sandbox> getAllowedSandboxes(final User user);

    Sandbox findBySandboxId(final String sandboxId);

    List<Sandbox> findByVisibility(final Visibility visibility);

    String fullCount();

    String schemaCount(final String apiEndpointIndex);

    String intervalCount(final Timestamp intervalTime);
}
