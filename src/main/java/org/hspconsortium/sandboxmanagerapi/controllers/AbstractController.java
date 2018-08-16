/*
 * #%L
 *
 * %%
 * Copyright (C) 2014 - 2015 Healthcare Services Platform Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.hspconsortium.sandboxmanagerapi.controllers;

import org.apache.http.HttpStatus;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.hspconsortium.sandboxmanagerapi.services.UserPersonaService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

abstract class AbstractController {
    public static final String UNAUTHORIZED_ERROR = "Response Status : %s.\n" +
                    "Response Detail : User not authorized to perform this action.";
    final OAuthService oAuthService;

    @Inject
    public AbstractController(final OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    // Check that the userId matches the authorized user in the request
    void checkUserAuthorization(final HttpServletRequest request, String userId) {
        String oauthUserId = oAuthService.getOAuthUserId(request);

        if (!userId.equalsIgnoreCase(oauthUserId)) {
            throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
        }
    }

    // Return userId of the authorized user in the request
    String getSystemUserId(final HttpServletRequest request) {
        return oAuthService.getOAuthUserId(request);
    }

    void checkCreatedByIsCurrentUserAuthorization(final HttpServletRequest request, String createdBySbmUserId) {
        checkUserAuthorization(request, createdBySbmUserId);
    }

    // Checks to see if the authorized User is a member of the Sandbox
    String checkSandboxUserReadAuthorization(final HttpServletRequest request, final Sandbox sandbox) {
        return checkSandboxMember(sandbox, oAuthService.getOAuthUserId(request));
    }

    // Checks to see if a User has can create a Sandbox Item
    String checkSandboxUserCreateAuthorization(final HttpServletRequest request, final Sandbox sandbox) {
        return checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
    }

    // Can a User modify a given Item in a given Sandbox
    // 1) The User must be a member of the Sandbox
    // 2) The User must have the right to modify the Item
    //    a) If the Item is Private, the user must be the creator of the Item
    //    b) If the Item is Public
    //       i) If the Sandbox is Private, the User must have non-read-only rights to the Sandbox
    //       ii) If the Sandbox is Public, the User mush be a Sandbox Admin
    String checkSandboxUserModifyAuthorization(final HttpServletRequest request, final Sandbox sandbox, final AbstractSandboxItem abstractSandboxItem) {

        //Fast fail for non-sandbox members
        String oauthUserId = checkSandboxUserReadAuthorization(request, sandbox);

        if (abstractSandboxItem.getVisibility() == Visibility.PRIVATE) {
            if (abstractSandboxItem.getCreatedBy().getSbmUserId().equalsIgnoreCase(oauthUserId)) {
                return oauthUserId;
            }
        } else { // Item is PUBLIC
            if (sandbox.getVisibility() == Visibility.PRIVATE) {
                return checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
            } else { // Sandbox is PUBLIC
                if (checkUserHasSandboxRole(request, sandbox, Role.ADMIN)) {
                    return oauthUserId;
                }
            }
        }
        throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
    }

    String checkSystemUserDeleteSandboxAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user) {
        String oauthUserId = oAuthService.getOAuthUserId(request);

        // If the sandbox is PRIVATE, only the creator can delete.
        // If the sandbox is PUBLIC, a system sandbox creator or system admin can delete.
        if (checkSystemUserCanModifySandbox(oauthUserId, sandbox, user) &&
                (sandbox.getVisibility() == Visibility.PRIVATE && sandbox.getCreatedBy().getSbmUserId().equalsIgnoreCase(oauthUserId))) {
            return oauthUserId;
        }
        throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
    }

    // Sandbox Admin rights
    String checkSystemUserCanModifySandboxAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user) {
        String oauthUserId = oAuthService.getOAuthUserId(request);

        // If the Sandbox is PRIVATE, only an Admin can modify.
        // If the Sandbox is PUBLIC, a system sandbox creator or system Admin can modify.
        if (checkSystemUserCanModifySandbox(oauthUserId, sandbox, user)) {
            return oauthUserId;
        }

        throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
    }

    String checkSystemUserCanManageSandboxDataAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user) {
        String oauthUserId = oAuthService.getOAuthUserId(request);

        // If the Sandbox is PRIVATE, only an Admin or data manager can manage data.
        // If the Sandbox is PUBLIC, a system sandbox creator or system Admin can manage data.
        if (checkSystemUserCanModifySandbox(oauthUserId, sandbox, user) ||
                (sandbox.getVisibility() == Visibility.PRIVATE && checkUserHasSandboxRole(oauthUserId, sandbox, Role.MANAGE_DATA))) {
            return oauthUserId;
        }

        throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
    }

    // Can manage user's is for inviting users to a sandbox
    // Only Admin's can delete Users
    String checkSystemUserCanManageSandboxUsersAuthorization(final HttpServletRequest request, final Sandbox sandbox, final User user) {
        String oauthUserId = oAuthService.getOAuthUserId(request);

        // If the Sandbox is PRIVATE, only an Admin or data manager can manage users.
        // If the Sandbox is PUBLIC, a system sandbox creator or system Admin can manage users.
        if (checkSystemUserCanModifySandbox(oauthUserId, sandbox, user) ||
                ((sandbox.getVisibility() == Visibility.PRIVATE && checkUserHasSandboxRole(oauthUserId, sandbox, Role.MANAGE_USERS)))) {
            return oauthUserId;
        }

        throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
    }

    void checkUserSystemRole(final User user, final SystemRole role) {
        if (!checkUserHasSystemRole(user, role)) {

            throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
        }
    }

    void checkUserSandboxRole(final HttpServletRequest request, final Sandbox sandbox, final Role role) {
        if (!checkUserHasSandboxRole(request, sandbox, role)) {

            throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
        }
    }

    void checkSystemUserCanMakeTransaction(Sandbox sandbox, User user) {
        List<Sandbox> sandboxes = user.getSandboxes();
        if (!sandboxes.contains(sandbox)) {
            throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
        }
    }

    void checkIfPersonaAndHasAuthority(Sandbox sandbox, UserPersona userPersona) {
        if (!sandbox.equals(userPersona.getSandbox())) {
            throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
        }
    }

//                                  Default Sandbox Item Visibility
//            *-------------------------------------------------------------------------------------*
//            |                       |                           |                                 |
//            |                       |      Private Sandbox      |          Public Sandbox         |
//            *-------------------------------------------------------------------------------------*
//            |                       |                           |                                 |
//  Sandbox   |         USER          |          PUBLIC           |              PRIVATE            |
//   Role     |                       |                           |                                 |
//            *-------------------------------------------------------------------------------------*
//            |                       |                           |                                 |
//            |        ADMIN          |          PUBLIC           |              PUBLIC             |
//            |                       |                           |                                 |
//            *-------------------------------------------------------------------------------------*

    Visibility getDefaultVisibility(final User user, final Sandbox sandbox) {

        // For a PRIVATE sandbox, non-readonly user's default visibility is PUBLIC.
        // For a PUBLIC sandbox, only ADMIN's have default visibility of PUBLIC.
        if ((sandbox.getVisibility() == Visibility.PRIVATE && !checkUserHasSandboxRole(user.getSbmUserId(), sandbox, Role.READONLY)) ||
            checkUserHasSandboxRole(user.getSbmUserId(), sandbox, Role.ADMIN)) {
            return Visibility.PUBLIC;
        }
        return Visibility.PRIVATE;
    }

    private boolean checkSystemUserCanModifySandbox(final String oauthUserId, final Sandbox sandbox, final User user) {
        // If the Sandbox is PRIVATE, only an Admin can modify.
        // If the Sandbox is PUBLIC, a system sandbox creator or system Admin can modify.
        return  (user.getSbmUserId().equalsIgnoreCase(oauthUserId) &&
                ((sandbox.getVisibility() == Visibility.PRIVATE && checkUserHasSandboxRole(oauthUserId, sandbox, Role.ADMIN)) ||
                        checkUserHasSystemRole(user, SystemRole.ADMIN)));
    }

    private String checkSandboxUserNotReadOnlyAuthorization(final HttpServletRequest request, final Sandbox sandbox) {

        String oauthUserId = oAuthService.getOAuthUserId(request);
        if (!checkUserHasSandboxRole(oauthUserId, sandbox, Role.READONLY)) {
            return oauthUserId;
        }

        throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
    }

    private String checkSandboxMember(final Sandbox sandbox, final String sbmUserId) {
        for(UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser().getSbmUserId().equalsIgnoreCase(sbmUserId)) {
                return sbmUserId;
            }
        }
        throw new UnauthorizedException(String.format(UNAUTHORIZED_ERROR, HttpStatus.SC_UNAUTHORIZED));
    }

    private boolean checkUserHasSystemRole(final User user, final SystemRole role) {
        for(SystemRole systemRole : user.getSystemRoles()) {
            if (systemRole == role) {
                return true;
            }
        }
        return false;
    }

    private boolean checkUserHasSandboxRole(final HttpServletRequest request, final Sandbox sandbox, final Role role) {
        String oauthUserId = oAuthService.getOAuthUserId(request);
        return checkUserHasSandboxRole(oauthUserId, sandbox, role);
    }

    private boolean checkUserHasSandboxRole(final String oauthUserId, final Sandbox sandbox, final Role role) {
        for(UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser().getSbmUserId().equalsIgnoreCase(oauthUserId) && userRole.getRole() == role) {
                return true;
            }
        }
        return false;
    }

}
