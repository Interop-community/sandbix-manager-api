package org.hspconsortium.sandboxmanagerapi.services;

import javax.servlet.http.HttpServletRequest;

/**
 */
public interface OAuthService {

    String getBearerToken(HttpServletRequest request);

    String getOAuthUserId(HttpServletRequest request);

    String getOAuthUserName(HttpServletRequest request);

    String getOAuthUserEmail(HttpServletRequest request);

}
