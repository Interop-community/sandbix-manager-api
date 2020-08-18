package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirProfileDetailRepository;
import org.hspconsortium.sandboxmanagerapi.services.FhirProfileDetailService;
import org.hspconsortium.sandboxmanagerapi.services.FhirProfileService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class FhirProfileDetailServiceImpl implements FhirProfileDetailService {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${hspc.platform.api.fhir.profileResources}")
    private String[] profileResources;

    private FhirProfileDetailRepository repository;
    private FhirProfileService fhirProfileService;
    private SandboxService sandboxService;

    @Inject
    public FhirProfileDetailServiceImpl(final FhirProfileDetailRepository repository) {
        this.repository = repository;
    }

    @Inject
    public void setFhirProfileService(FhirProfileService fhirProfileService) {
        this.fhirProfileService = fhirProfileService;
    }

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    private HashMap<String, ProfileTask> idProfileTask = new HashMap<>();

    public ProfileTask getTaskRunning(String id) { return idProfileTask.get(id); }

    public HashMap<String, ProfileTask> getIdProfileTask() { return idProfileTask; }

    @Override
    @Transactional
    public FhirProfileDetail save(FhirProfileDetail fhirProfileDetail) {
        FhirProfileDetail fhirProfileDetailSaved = repository.save(fhirProfileDetail);
        List<FhirProfile> fhirProfiles = fhirProfileDetailSaved.getFhirProfiles();
        for (FhirProfile fhirProfile : fhirProfiles) {
            fhirProfile.setFhirProfileId(fhirProfileDetailSaved.getId());
            fhirProfileService.save(fhirProfile);
        }
        return fhirProfileDetailSaved;
    }

    public FhirProfileDetail findByProfileIdAndSandboxId(String profileId, String sandboxId) {
        return repository.findByProfileIdAndSandboxId(profileId, sandboxId);
    }

    @Override
    public FhirProfileDetail getFhirProfileDetail(Integer fhirProfileId) {
        return repository.findByFhirProfileId(fhirProfileId);
    }

    @Override
    public List<FhirProfileDetail> getAllProfilesForAGivenSandbox(String sandboxId) {
        return repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<FhirProfile> getFhirProfileWithASpecificTypeForAGivenSandbox(Integer fhirProfileId, String type) {
        return fhirProfileService.getFhirProfileWithASpecificTypeForAGivenSandbox(fhirProfileId, type);
    }

    @Override
    public List<Integer> getAllFhirProfileIdsAssociatedWithASandbox(String sandboxId) {
        return repository.findAllFhirProfileIdsBySandboxId(sandboxId);
    }

    @Override
    @Transactional
    @Async("taskExecutor")
    public void delete(HttpServletRequest request, Integer fhirProfileId, String sandboxId) {
        String authToken = request.getHeader("Authorization");
        List<FhirProfile> fhirProfiles = fhirProfileService.getAllResourcesForGivenProfileId(fhirProfileId);
        if (fhirProfiles.isEmpty()) {
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        HttpEntity entity = new HttpEntity(headers);
        String apiSchemaURL = sandboxService.getApiSchemaURL(sandboxService.findBySandboxId(sandboxId).getApiEndpointIndex());

        for (FhirProfile fhirProfile: fhirProfiles) {
            String url = apiSchemaURL + "/" + sandboxId + "/data/" + fhirProfile.getRelativeUrl();
            try {
                restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            } catch (Exception ignored) {

            }
        }
        delete(fhirProfileId);
    }

    @Override
    @Transactional
    public void delete(Integer fhirProfileId) {
        fhirProfileService.delete(fhirProfileId);
        repository.delete(getFhirProfileDetail(fhirProfileId));
    }

    @Async("taskExecutor")
    @Override
    public void saveZipFile (FhirProfileDetail fhirProfileDetail, ZipFile zipFile, String authToken, String sandboxId, String id) throws IOException {
        String apiEndpoint = sandboxService.findBySandboxId(sandboxId).getApiEndpointIndex();
        ProfileTask profileTask = addToProfileTask(id, true, new ArrayList<>(), new ArrayList<>(), 0, 0, 0 );
        List<FhirProfile> fhirProfiles = new ArrayList<>();
        Enumeration zipFileEntries = zipFile.entries();
        while(zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String fileName = entry.getName();
            if (fileName.endsWith(".json")) {
                InputStream inputStream = zipFile.getInputStream(entry);
                JSONObject profileTaskAndFhirProfile = saveProfileResource(sandboxService.getApiSchemaURL(apiEndpoint), authToken, sandboxId, apiEndpoint, id, inputStream, fileName, profileTask);
                profileTask = (ProfileTask) profileTaskAndFhirProfile.get("profileTask");
                if(!addToFhirProfiles(profileTaskAndFhirProfile, fhirProfiles)) {
                    break;
                }
            }
        }
        saveFhirProfileDetail(fhirProfileDetail, id, profileTask, fhirProfiles);
    }

    private void saveFhirProfileDetail(FhirProfileDetail fhirProfileDetail, String id, ProfileTask profileTask, List<FhirProfile> fhirProfiles) {
        profileTask.setStatus(false);
        idProfileTask.put(id, profileTask);
        if (profileTask.getError() == null && fhirProfiles.size() != 0) {
            fhirProfileDetail.setFhirProfiles(fhirProfiles);
            save(fhirProfileDetail);
        } else {
            throw new RuntimeException("Unable to open the file. The profile was not uploaded"); //TODO: ask about this exception
        }
    }

    private boolean addToFhirProfiles(JSONObject profileTaskAndFhirProfile, List<FhirProfile> fhirProfiles) {
        ProfileTask profileTask = (ProfileTask) profileTaskAndFhirProfile.get("profileTask");
        if (profileTask.getError() != null) {
            return false;
        }
        FhirProfile fhirProfile = (FhirProfile) profileTaskAndFhirProfile.get("fhirProfile");
        if (fhirProfile != null) {
            fhirProfiles.add(fhirProfile);
        }
        return true;
    }

    @Async("taskExecutor")
    @Override
    public void saveTGZfile (FhirProfileDetail fhirProfileDetail, InputStream fileInputStream, String authToken, String sandboxId, String id) throws IOException {
        String apiEndpoint = sandboxService.findBySandboxId(sandboxId).getApiEndpointIndex();
        ProfileTask profileTask = addToProfileTask(id, true, new ArrayList<>(), new ArrayList<>(), 0, 0, 0 );
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(fileInputStream));
        importFromTarball(fhirProfileDetail, fileInputStream, authToken, sandboxId, id, apiEndpoint, sandboxService.getApiSchemaURL(apiEndpoint), profileTask, new ArrayList<>(), tarArchiveInputStream);
    }

    private void importFromTarball(FhirProfileDetail fhirProfileDetail, InputStream fileInputStream, String authToken, String sandboxId, String id, String apiEndpoint, String apiSchemaURL, ProfileTask profileTask, List<FhirProfile> fhirProfiles, TarArchiveInputStream tarArchiveInputStream) throws IOException {
        TarArchiveEntry entry;
        while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            String fileName = entry.getName();
            String fileExtension = FilenameUtils.getExtension(fileName);
            if (fileExtension.equals("json")) {
                JSONObject profileTaskAndFhirProfile = saveProfileResource(apiSchemaURL, authToken, sandboxId, apiEndpoint, id, tarArchiveInputStream, fileName, profileTask);
                profileTask = (ProfileTask) profileTaskAndFhirProfile.get("profileTask");
                if (!addToFhirProfiles(profileTaskAndFhirProfile, fhirProfiles)) {
                    break;
                }
            }
        }
        tarArchiveInputStream.close();
        fileInputStream.close();
        saveFhirProfileDetail(fhirProfileDetail, id, profileTask, fhirProfiles);
    }

    @Async("taskExecutor")
    @Override
    public void saveTarballfile (FhirProfileDetail fhirProfileDetail, InputStream fileInputStream, String authToken, String sandboxId, String id) throws IOException {
        String apiEndpoint = sandboxService.findBySandboxId(sandboxId).getApiEndpointIndex();
        ProfileTask profileTask = addToProfileTask(id, true, new ArrayList<>(), new ArrayList<>(), 0, 0, 0 );
        List<FhirProfile> fhirProfiles = new ArrayList<>();
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(fileInputStream);
        importFromTarball(fhirProfileDetail, fileInputStream, authToken, sandboxId, id, apiEndpoint, sandboxService.getApiSchemaURL(apiEndpoint), profileTask, fhirProfiles, tarArchiveInputStream);
    }

    private JSONObject saveProfileResource(String apiSchemaURL, String authToken, String sandboxId, String apiEndpoint, String id, InputStream inputStream, String fileName, ProfileTask profileTask) {
        JSONObject profileTaskAndFhirProfile = new JSONObject();
        List<String> resourceSaved = profileTask.getResourceSaved();
        List<String> resourceNotSaved = profileTask.getResourceNotSaved();
        int totalCount = profileTask.getTotalCount();
        int resourceSavedCount = profileTask.getResourceSavedCount();
        int resourceNotSavedCount = profileTask.getResourceNotSavedCount();
        profileTask = addToProfileTask(id, true, resourceSaved, resourceNotSaved, totalCount, resourceSavedCount, resourceNotSavedCount);
        idProfileTask.put(id, profileTask);
        profileTaskAndFhirProfile.put("profileTask", profileTask);
        FhirProfile fhirProfile = new FhirProfile();
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject)jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
            String resourceType = jsonObject.get("resourceType").toString();
            String resourceId = "";
            String fullUrl = "";
            String fhirVersion = "";
            if (Arrays.stream(profileResources).anyMatch(resourceType::equals)) {
                if (jsonObject.containsKey("id")) {
                    resourceId = jsonObject.get("id").toString();
                }

                if (jsonObject.containsKey("url")) {
                    fullUrl = jsonObject.get("url").toString();
                } else {
                    profileTask.setError("The file name: " + fileName + " is missing url metadata. The profile was not saved.");
                    profileTask.setStatus(false);
                    idProfileTask.put(id, profileTask);
                    profileTaskAndFhirProfile.put("profileTask", profileTask);
                    return profileTaskAndFhirProfile;
                }

                if (resourceType.equals("StructureDefinition")) {
                    if (jsonObject.containsKey("fhirVersion")) {
                        fhirVersion = jsonObject.get("fhirVersion").toString();
                    } else {
                        profileTask.setError("The StructureDefinition under file name: " + fileName + " is missing fhirVersion. The profile was not saved.");
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        return profileTaskAndFhirProfile;
                    }

                    try {
                        String profileType = jsonObject.get("type").toString();
                        fhirProfile.setProfileType(profileType);
                    } catch (Exception e) {
                        profileTask.setError("The StructureDefinition under file name: " + fileName + " is missing 'type' parameter. The profile was not saved.");
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        return profileTaskAndFhirProfile;
                    }
                    String errorMessage = "";
                    if (apiEndpoint.equals("8") && !(fhirVersion.equals("1.0.1") || fhirVersion.equals("1.0.2"))) {
                        errorMessage = fileName + " FHIR version (" + fhirVersion + ") is incompatible with your current sandbox's FHIR version (1.0.2). The profile was not saved.";
                        profileTask.setError(errorMessage);
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        return profileTaskAndFhirProfile;
                    } else if (apiEndpoint.equals("9") && !(fhirVersion.equals("3.0.1") || fhirVersion.equals("3.0.2") || fhirVersion.equals("3.1.0") || fhirVersion.equals("3.0.0"))) {
                        errorMessage = fileName + " FHIR version (" + fhirVersion + ") is incompatible with your current sandbox's FHIR version (3.0.2). The profile was not saved.";
                        profileTask.setError(errorMessage);
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        return profileTaskAndFhirProfile;
                    } else if (apiEndpoint.equals("10") && !(fhirVersion.equals("4.0.0") || fhirVersion.equals("4.0.1") || fhirVersion.equals("1.8.0"))) {
                        errorMessage = fileName + " FHIR version (" + fhirVersion + ") is incompatible with your current sandbox's FHIR version (4.0.1). The profile was not saved.";
                        profileTask.setError(errorMessage);
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        return profileTaskAndFhirProfile;
                    }
                }
                String jsonBody = jsonObject.toString();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", authToken);
                headers.set("Content-Type", "application/json");
                HttpEntity entity = new HttpEntity(jsonBody, headers);
                String url = "";
                if (!resourceId.isEmpty()) {
                    url = apiSchemaURL + "/" + sandboxId + "/data/" + resourceType + "/" + resourceId;
                    try {
                        restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
                        resourceSaved.add(resourceType + " - " + resourceId);
                        totalCount++;
                        resourceSavedCount++;
                        profileTask = addToProfileTask(id, true, resourceSaved, resourceNotSaved, totalCount, resourceSavedCount, resourceNotSavedCount);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);

                        fhirProfile.setFullUrl(fullUrl);
                        fhirProfile.setRelativeUrl(resourceType + "/" + resourceId);
                        profileTaskAndFhirProfile.put("fhirProfile", fhirProfile);

                    } catch (HttpServerErrorException | HttpClientErrorException e) {
                        resourceNotSaved.add(resourceType + " - " + resourceId + " - " + e.getMessage());
                        totalCount++;
                        resourceNotSavedCount++;
                        profileTask = addToProfileTask(id, true, resourceSaved, resourceNotSaved, totalCount, resourceSavedCount, resourceNotSavedCount);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                    }
                } else {
                    url = apiSchemaURL + "/" + sandboxId + "/data/" + resourceType;
                    try {
                        ResponseEntity responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                        try {
                            JSONObject savedResource = (JSONObject)jsonParser.parse(responseEntity.getBody().toString());
                            resourceId = savedResource.get("id").toString();
                            resourceSaved.add(resourceType + " - " + resourceId);
                            totalCount++;
                            resourceSavedCount++;
                            profileTask = addToProfileTask(id, true, resourceSaved, resourceNotSaved, totalCount, resourceSavedCount, resourceNotSavedCount);
                            idProfileTask.put(id, profileTask);
                            profileTaskAndFhirProfile.put("profileTask", profileTask);

                            fhirProfile.setFullUrl(fullUrl);
                            fhirProfile.setRelativeUrl(resourceType + "/" + resourceId);
                            profileTaskAndFhirProfile.put("fhirProfile", fhirProfile);

                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    } catch (HttpServerErrorException | HttpClientErrorException e) {
                        resourceNotSaved.add(resourceType + " - " + resourceId + " - " + e.getMessage());
                        totalCount++;
                        resourceNotSavedCount++;
                        profileTask = addToProfileTask(id, true, resourceSaved, resourceNotSaved, totalCount, resourceSavedCount, resourceNotSavedCount);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                    }
                }
            }
        } catch (Exception ignored) {

        }
        return profileTaskAndFhirProfile;
    }

    private ProfileTask addToProfileTask(String id, Boolean runStatus, List<String> resourceSaved,
                                         List<String> resourceNotSaved, int totalCount, int resourceSavedCount,
                                         int resourceNotSavedCount){
        ProfileTask profileTask = new ProfileTask();
        profileTask.setId(id);
        profileTask.setStatus(runStatus);
        profileTask.setResourceSaved(resourceSaved);
        profileTask.setResourceNotSaved(resourceNotSaved);
        profileTask.setTotalCount(totalCount);
        profileTask.setResourceSavedCount(resourceSavedCount);
        profileTask.setResourceNotSavedCount(resourceNotSavedCount);
        return profileTask;
    }

}

