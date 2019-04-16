package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfile;

import java.util.List;

public interface FhirProfileService {

    void save(final List<FhirProfile> fhirProfiles);

    void delete(final String profileName);

    FhirProfile get(final String profileName); //TODO: start here
}
