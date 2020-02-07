package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.KeycloakUserAcknowledgment;
import org.hspconsortium.sandboxmanagerapi.repositories.KeycloakUserAcknowledgementRepository;
import org.hspconsortium.sandboxmanagerapi.services.KeycloakUserAcknowledgementService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class KeycloakUserAcknowledgmentServiceImpl implements KeycloakUserAcknowledgementService {

    private final KeycloakUserAcknowledgementRepository repository;

    @Inject
    public KeycloakUserAcknowledgmentServiceImpl(final KeycloakUserAcknowledgementRepository repository) {
        this.repository = repository;
    }

    @Override
    public String findBySbmUserId(final String sbmUserId) {
        return repository.findBySbmUserId(sbmUserId);
    }

    @Override
    public KeycloakUserAcknowledgment save(final KeycloakUserAcknowledgment keycloakUserAcknowledgment) {
        return repository.save(keycloakUserAcknowledgment);
    }
}
