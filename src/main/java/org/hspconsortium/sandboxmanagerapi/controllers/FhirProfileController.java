package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfile;
import org.hspconsortium.sandboxmanagerapi.services.AuthorizationService;
import org.hspconsortium.sandboxmanagerapi.services.FhirProfileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping({"/fhir-profile"})
public class FhirProfileController {
    private final FhirProfileService fhirProfileService;
    private final AuthorizationService authorizationService;

    @Inject
    public FhirProfileController(final FhirProfileService fhirProfileService, final AuthorizationService authorizationService) {
        this.fhirProfileService = fhirProfileService;
        this.authorizationService = authorizationService;
    }

    @PostMapping(value = "/save")
    public void saveProfile(@RequestBody List<FhirProfile> fhirProfiles) {
        fhirProfileService.save(fhirProfiles);
    }

}
