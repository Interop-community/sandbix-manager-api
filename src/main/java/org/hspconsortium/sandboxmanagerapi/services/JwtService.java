package org.hspconsortium.sandboxmanagerapi.services;

/**
 * Service to create and validate JWT
 */
public interface JwtService {

    String createSignedJwt(String subject);

//    String createSignedHookJwt(String subject);
}
