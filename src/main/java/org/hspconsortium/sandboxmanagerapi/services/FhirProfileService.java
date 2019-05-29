package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfile;
import org.hspconsortium.sandboxmanagerapi.model.FhirProfileDetail;
import org.hspconsortium.sandboxmanagerapi.model.ProfileTask;
import org.json.simple.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;

public interface FhirProfileService {

    void save(FhirProfile fhirProfile);

    List<FhirProfile> getAllResourcesForGivenProfileId(Integer fhirProfileId);

    List<FhirProfile> getAllSDsForGivenProfileId(Integer fhirProfileId);

    void delete(Integer fhirProfileId);

    FhirProfile getFhirProfileWithASpecificTypeForAGivenSandbox(Integer fhirProfileId, String profileType);

}
