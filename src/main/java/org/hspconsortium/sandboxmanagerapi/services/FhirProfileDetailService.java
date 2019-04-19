package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.FhirProfileDetail;
import org.hspconsortium.sandboxmanagerapi.model.ProfileTask;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;

public interface FhirProfileDetailService {

    FhirProfileDetail save(FhirProfileDetail fhirProfileDetail);

    FhirProfileDetail update(FhirProfileDetail fhirProfileDetail);

    FhirProfileDetail getFhirProfileDetail(Integer fhirProfileId);

    List<FhirProfileDetail> getFhirProfileDetails(String sandboxId);

    void delete(Integer fhirProfileId);

    void saveZipFile (ZipFile zipFile, HttpServletRequest request, String sandboxId, String apiEndpoint, String id, String profileName, String profileId, User user, Visibility visibility) throws IOException;

    ProfileTask getTaskRunning(String id);

    HashMap<String, ProfileTask> getIdProfileTask();

    void saveTGZfile (MultipartFile file, HttpServletRequest request, String sandboxId, String apiEndpoint, String id, String profileName, String profileId, User user, Visibility visibility) throws IOException;

}
