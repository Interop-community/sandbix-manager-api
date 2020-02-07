package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.KeycloakUserAcknowledgment;

public interface KeycloakUserAcknowledgementService {

    String findBySbmUserId(final String sbmUserId);

    KeycloakUserAcknowledgment save(final KeycloakUserAcknowledgment keycloakUserAcknowledgment);
}
