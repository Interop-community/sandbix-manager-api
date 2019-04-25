package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfileDetail;
import org.hspconsortium.sandboxmanagerapi.model.ProfileTask;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;

public interface FhirProfileDetailService {

    FhirProfileDetail save(FhirProfileDetail fhirProfileDetail);

    FhirProfileDetail update(FhirProfileDetail fhirProfileDetail);

    FhirProfileDetail getFhirProfileDetail(Integer fhirProfileId);

    List<FhirProfileDetail> getAllProfilesForAGivenSandbox(String sandboxId);

    void delete(HttpServletRequest request, Integer fhirProfileId, String sandboxId);

    void delete(Integer fhirProfileId);

    void saveZipFile (FhirProfileDetail fhirProfileDetail, ZipFile zipFile, String authToken, String sandboxId, String id) throws IOException;

    ProfileTask getTaskRunning(String id);

    HashMap<String, ProfileTask> getIdProfileTask();

    void saveTGZfile (FhirProfileDetail fhirProfileDetail, InputStream fileInputStream, String authToken, String sandboxId, String id) throws IOException;

    FhirProfileDetail findByProfileIdAndSandboxId(String profileId, String sandboxId);

}
