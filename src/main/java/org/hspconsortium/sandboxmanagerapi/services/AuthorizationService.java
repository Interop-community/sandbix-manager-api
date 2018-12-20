package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;

import javax.servlet.http.HttpServletRequest;

public interface AuthorizationService {

    String UNAUTHORIZED_ERROR = "Response Status : %s.\n" +
            "Response Detail : User not authorized to perform this action.";

    void checkUserAuthorization(final HttpServletRequest request, String userId);

    String getSystemUserId(final HttpServletRequest request);

    String getUserName(final HttpServletRequest request);

    String getEmail(final HttpServletRequest request);

    String getBearerToken(final HttpServletRequest request);

    String checkSandboxUserReadAuthorization(final HttpServletRequest request, final Sandbox sandbox);

    String checkSandboxUserModifyAuthorization(final HttpServletRequest request, final Sandbox sandbox, final AbstractSandboxItem abstractSandboxItem);

    String checkSystemUserDeleteSandboxAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user);

    String checkSystemUserCanModifySandboxAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user);

    String checkSystemUserCanManageSandboxDataAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user);

    String checkSystemUserCanManageSandboxUsersAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user);

    String checkSandboxUserNotReadOnlyAuthorization(final HttpServletRequest request, final Sandbox sandbox);

    void checkUserSystemRole(final User user, final SystemRole role);

    void checkSystemUserCanMakeTransaction(Sandbox sandbox, User user);

    void checkIfPersonaAndHasAuthority(Sandbox sandbox, UserPersona userPersona);

    Visibility getDefaultVisibility(final User user, final Sandbox sandbox);

    boolean checkUserHasSystemRole(final User user, final SystemRole role);

    String checkSystemUserCanRemoveUser(final HttpServletRequest request, final Sandbox sandbox, final User user);

}
