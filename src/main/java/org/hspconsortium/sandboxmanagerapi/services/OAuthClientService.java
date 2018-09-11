package org.hspconsortium.sandboxmanagerapi.services;

public interface OAuthClientService {

    String postOAuthClient(String clientJSON);

    String putOAuthClientWithClientId(String clientId, String clientJSON);

    String getOAuthClientWithClientId(String clientId);

    void deleteOAuthClientWithClientId(String clientId);
}
