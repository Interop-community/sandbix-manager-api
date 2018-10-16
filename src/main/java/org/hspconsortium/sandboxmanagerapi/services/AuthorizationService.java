package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;

import javax.servlet.http.HttpServletRequest;

public interface AuthorizationService {

    void checkUserAuthorization(final HttpServletRequest request, String userId);

    String getSystemUserId(final HttpServletRequest request);

    void checkCreatedByIsCurrentUserAuthorization(final HttpServletRequest request, String createdBySbmUserId);

    String checkSandboxUserReadAuthorization(final HttpServletRequest request, final Sandbox sandbox);

    String checkSandboxUserCreateAuthorization(final HttpServletRequest request, final Sandbox sandbox);

    String checkSandboxUserModifyAuthorization(final HttpServletRequest request, final Sandbox sandbox, final AbstractSandboxItem abstractSandboxItem);

    String checkSystemUserDeleteSandboxAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user);

    String checkSystemUserCanModifySandboxAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user);

    String checkSystemUserCanManageSandboxDataAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user);

    String checkSystemUserCanManageSandboxUsersAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user);

    void checkUserSystemRole(final User user, final SystemRole role);

    void checkUserSandboxRole(final HttpServletRequest request, final Sandbox sandbox, final Role role);

    void checkSystemUserCanMakeTransaction(Sandbox sandbox, User user);

    void checkIfPersonaAndHasAuthority(Sandbox sandbox, UserPersona userPersona);

    Visibility getDefaultVisibility(final User user, final Sandbox sandbox);

}
