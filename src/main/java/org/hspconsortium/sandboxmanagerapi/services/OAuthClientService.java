package org.hspconsortium.sandboxmanagerapi.services;

/**
 */
public interface OAuthClientService {

    String postOAuthClient(String clientJSON);

    String putOAuthClient(Integer id, String clientJSON);

    String getOAuthClient(Integer id);

    String getOAuthClientWithClientId(String clientId);

    void deleteOAuthClient(Integer id);
}
