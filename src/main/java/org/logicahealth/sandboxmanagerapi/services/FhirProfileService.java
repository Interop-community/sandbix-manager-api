package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.FhirProfile;

import java.util.List;

public interface FhirProfileService {

    void save(FhirProfile fhirProfile);

    List<FhirProfile> getAllResourcesForGivenProfileId(Integer fhirProfileId);

    List<FhirProfile> getAllSDsForGivenProfileId(Integer fhirProfileId);

    void delete(Integer fhirProfileId);

    List<FhirProfile> getFhirProfileWithASpecificTypeForAGivenSandbox(Integer fhirProfileId, String profileType);

    List<String> getAllProfileTypesForAGivenProfileId(Integer fhirProfileId);

}
