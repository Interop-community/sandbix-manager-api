package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.services.KeycloakUserAcknowledgementService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.transaction.Transactional;

@RestController
@RequestMapping({"/keycloakUserAcknowledgment"})
public class KeycloakUserAcknowledgmentController {

    private final KeycloakUserAcknowledgementService service;

    @Inject
    public KeycloakUserAcknowledgmentController(final KeycloakUserAcknowledgementService service) {
        this.service = service;
    }

    @GetMapping(params = {"sbmUserId"})
    @Transactional
    public @ResponseBody Boolean userAcknowledged(@RequestParam(value = "sbmUserId") String sbmUserId) {
        Boolean userAcknowledged = false;
        if (service.findBySbmUserId(sbmUserId) != null) {
            userAcknowledged = true;
        }
        return userAcknowledged;
    }



}
