package org.logicahealth.sandboxmanagerapi.services.impl;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.logicahealth.sandboxmanagerapi.model.FhirProfile;
import org.logicahealth.sandboxmanagerapi.model.FhirProfileDetail;
import org.logicahealth.sandboxmanagerapi.model.FhirProfileStatus;
import org.logicahealth.sandboxmanagerapi.model.ProfileTask;
import org.logicahealth.sandboxmanagerapi.repositories.FhirProfileDetailRepository;
import org.logicahealth.sandboxmanagerapi.services.EmailService;
import org.logicahealth.sandboxmanagerapi.services.FhirProfileDetailService;
import org.logicahealth.sandboxmanagerapi.services.FhirProfileService;
import org.logicahealth.sandboxmanagerapi.services.SandboxService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FhirProfileDetailServiceImpl implements FhirProfileDetailService {
    private static Logger LOGGER = LoggerFactory.getLogger(FhirProfileDetailServiceImpl.class.getName());

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

    public ProfileTask getTaskRunning(String id) {
        
        LOGGER.info("getTaskRunning");

        LOGGER.debug("getTaskRunning: "
        +"Parameters: id = "+id+"; Return value = "+idProfileTask.get(id));

        return idProfileTask.get(id);
    }

    public HashMap<String, ProfileTask> getIdProfileTask() {
        LOGGER.info("getIdProfileTask");

        LOGGER.debug("getIdProfileTask: "
        +"No input parameters; Return value = "+idProfileTask);

        return idProfileTask;
    }

    @Override
    @Transactional
    public FhirProfileDetail save(FhirProfileDetail fhirProfileDetail) {
        
        LOGGER.info("save");

        FhirProfileDetail fhirProfileDetailSaved = repository.save(fhirProfileDetail);
        List<FhirProfile> fhirProfiles = fhirProfileDetailSaved.getFhirProfiles();
        for (FhirProfile fhirProfile : fhirProfiles) {
            fhirProfile.setFhirProfileId(fhirProfileDetailSaved.getId());
            fhirProfileService.save(fhirProfile);
        }

        LOGGER.debug("save: "
        +"Parameters: fhirProfileDetail = "+fhirProfileDetail+"; Return value = "+fhirProfileDetailSaved);

        return fhirProfileDetailSaved;
    }

    public FhirProfileDetail findByProfileIdAndSandboxId(String profileId, String sandboxId) {

        LOGGER.info("findByProfileIdAndSandboxId");

        LOGGER.debug("findByProfileIdAndSandboxId: "
        +"Parameters: profileId = "+profileId+", sandboxId = "+sandboxId
        +"; Return value = "+repository.findByProfileIdAndSandboxId(profileId, sandboxId));

        return repository.findByProfileIdAndSandboxId(profileId, sandboxId);
    }

    @Override
    public FhirProfileDetail getFhirProfileDetail(Integer fhirProfileId) {
        LOGGER.info("getFhirProfileDetail");

        LOGGER.debug("getFhirProfileDetail: "
        +"Parameters: fhirProfileId = "+fhirProfileId
        +"; Return value = "+repository.findByFhirProfileId(fhirProfileId));

        return repository.findByFhirProfileId(fhirProfileId);
    }

    @Override
    public List<FhirProfileDetail> getAllProfilesForAGivenSandbox(String sandboxId) {

        LOGGER.info("getAllProfilesForAGivenSandbox");

        LOGGER.debug("getAllProfilesForAGivenSandbox: "
        +"Parameters: sandboxId = "+sandboxId+"; Return value = "+repository.findBySandboxId(sandboxId));

        return repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<FhirProfile> getFhirProfileWithASpecificTypeForAGivenSandbox(Integer fhirProfileId, String type) {

        LOGGER.info("getFhirProfileWithASpecificTypeForAGivenSandbox");

        LOGGER.debug("getFhirProfileWithASpecificTypeForAGivenSandbox: "
        +"Parameters: fhirProfileId = "+fhirProfileId+", type = "+type
        +"; Return value = "+fhirProfileService.getFhirProfileWithASpecificTypeForAGivenSandbox(fhirProfileId, type));

        return fhirProfileService.getFhirProfileWithASpecificTypeForAGivenSandbox(fhirProfileId, type);
    }

    @Override
    public List<Integer> getAllFhirProfileIdsAssociatedWithASandbox(String sandboxId) {

        LOGGER.info("getAllFhirProfileIdsAssociatedWithASandbox");

        LOGGER.debug("getAllFhirProfileIdsAssociatedWithASandbox: "
        +"Parameters: sandboxId = "+sandboxId+"; Return value = "+repository.findAllFhirProfileIdsBySandboxId(sandboxId));

        return repository.findAllFhirProfileIdsBySandboxId(sandboxId);
    }

    @Override
    @Transactional
    public void markAsDeleted(Integer fhirProfileId) {
        
        LOGGER.info("markAsDeleted");
        
        var fhirProfileDetail = repository.findByFhirProfileId(fhirProfileId);
        if (fhirProfileDetail != null) {
            fhirProfileDetail.setStatus(FhirProfileStatus.DELETED);
            repository.save(fhirProfileDetail);
        }

        LOGGER.debug("markAsDeleted: "
        +"Parameters: fhirProfileId = "+fhirProfileId+"; No return value");

    }

    @Override
    @Transactional
    @Async("taskExecutor")
    public void backgroundDelete(HttpServletRequest request, Integer fhirProfileId, String sandboxId) {
        
        LOGGER.info("backgroundDelete");

        String authToken = request.getHeader("Authorization");
        List<FhirProfile> fhirProfiles = fhirProfileService.getAllResourcesForGivenProfileId(fhirProfileId);
        if (fhirProfiles.isEmpty()) {

            LOGGER.debug("backgroundDelete: "
            +"Parameters: request = "+request+", fhirProfileId = "+fhirProfileId+", sandboxId = "+sandboxId
            +"; No return value");

            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        HttpEntity entity = new HttpEntity(headers);
        String apiSchemaURL = sandboxService.getApiSchemaURL(sandboxService.findBySandboxId(sandboxId)
                                                                           .getApiEndpointIndex());

        for (FhirProfile fhirProfile : fhirProfiles) {
            String url = apiSchemaURL + "/" + sandboxId + "/data/" + fhirProfile.getRelativeUrl();
            try {
                restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            } catch (Exception ignored) {

            }
        }
        delete(fhirProfileId);

        LOGGER.debug("backgroundDelete: "
            +"Parameters: request = "+request+", fhirProfileId = "+fhirProfileId+", sandboxId = "+sandboxId
            +"; No return value");
    }

    @Override
    @Transactional
    public void delete(Integer fhirProfileId) {

        LOGGER.info("delete");

        fhirProfileService.delete(fhirProfileId);
        repository.delete(getFhirProfileDetail(fhirProfileId));

        LOGGER.debug("delete: "
        +"Parameters: fhirProfileId = "+fhirProfileId+"; No return value");

    }

    @Async("taskExecutor")
    @Override
    public void saveZipFile(FhirProfileDetail fhirProfileDetail, ZipFile zipFile, String authToken, String sandboxId, String id) throws IOException {
        
        LOGGER.info("saveZipFile");

        String apiEndpoint = sandboxService.findBySandboxId(sandboxId)
                                           .getApiEndpointIndex();
        String apiSchemaURL = sandboxService.getApiSchemaURL(apiEndpoint);
        ProfileTask profileTask = addToProfileTask(id, true, new HashMap<>(), new HashMap<>(), 0, 0, 0);
        List<FhirProfile> fhirProfiles = new ArrayList<>();
        Enumeration zipFileEntries = zipFile.entries();
        while (zipFileEntries.hasMoreElements()) {
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

        LOGGER.debug("saveZipFile: "
        +"Parameters: fhirProfileDetail = "+fhirProfileDetail+", zipFile = "+zipFile
        +", authToken = "+authToken+", sandboxId = "+sandboxId+", id = "+id+"; No return value");

    }

    private void saveFhirProfileDetail(FhirProfileDetail fhirProfileDetail, String id, ProfileTask profileTask, List<FhirProfile> fhirProfiles) {
        
        LOGGER.info("saveFhirProfileDetail");

        LOGGER.debug("saveFhirProfileDetail: "
        +"(BEFORE) Parameters: fhirProfileDetail: "+fhirProfileDetail+", id = "+id+", profileTask = "+profileTask);

        profileTask.setStatus(false);
        idProfileTask.put(id, profileTask);
        if (profileTask.getError() == null && fhirProfiles.size() != 0) {
            fhirProfileDetail.setFhirProfiles(fhirProfiles);
            save(fhirProfileDetail);
        } else {
            throw new RuntimeException("Unable to open the file. The profile was not uploaded"); //TODO: ask about this exception
        }

        LOGGER.debug("saveFhirProfileDetail: "
        +"(AFTER) Parameters: fhirProfileDetail: "+fhirProfileDetail+", id = "+id+", profileTask = "+profileTask
        +", fhirProfiles = "+fhirProfiles+"; No return value");

    }

    private boolean addToFhirProfiles(JSONObject profileTaskAndFhirProfile, List<FhirProfile> fhirProfiles) {
        
        LOGGER.info("addToFhirProfiles");

        LOGGER.debug("addToFhirProfiles: "
        +"(BEFORE) Parameters: profileTaskAndFhirProfile = "+profileTaskAndFhirProfile+", fhirProfiles = "+fhirProfiles);

        ProfileTask profileTask = (ProfileTask) profileTaskAndFhirProfile.get("profileTask");
        if (profileTask.getError() != null) {

            LOGGER.debug("addToFhirProfiles: "
            +"(AFTER) Parameters: profileTaskAndFhirProfile = "+profileTaskAndFhirProfile+", fhirProfiles = "+fhirProfiles
            +"; Return value = false");

            return false;
        }
        FhirProfile fhirProfile = (FhirProfile) profileTaskAndFhirProfile.get("fhirProfile");
        if (fhirProfile != null) {
            fhirProfiles.add(fhirProfile);
        }

        LOGGER.debug("addToFhirProfiles: "
        +"(AFTER) Parameters: profileTaskAndFhirProfile = "+profileTaskAndFhirProfile+", fhirProfiles = "+fhirProfiles
        +"; Return value = true");

        return true;
    }

    @Async("taskExecutor")
    @Override
    public void saveTGZfile(FhirProfileDetail fhirProfileDetail, InputStream fileInputStream, String authToken, String sandboxId, String id) throws IOException {

        LOGGER.info("saveTGZfile");

        String apiEndpoint = sandboxService.findBySandboxId(sandboxId)
                                           .getApiEndpointIndex();
        String apiSchemaURL = sandboxService.getApiSchemaURL(apiEndpoint);
        ProfileTask profileTask = addToProfileTask(id, true, new HashMap<>(), new HashMap<>(), 0, 0, 0);
        List<FhirProfile> fhirProfiles = new ArrayList<>();
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(fileInputStream));
        importFromTarball(fhirProfileDetail, fileInputStream, authToken, sandboxId, id, apiEndpoint, sandboxService.getApiSchemaURL(apiEndpoint), profileTask, new ArrayList<>(), tarArchiveInputStream);

        LOGGER.debug("saveTGZfile: "
        +"Parameters: fhirProfileDetail = "+fhirProfileDetail+", fileInputStream = "+fileInputStream
        +", authToken = "+authToken+", sandboxId = "+sandboxId+", id = "+id
        +"; No return value");

    }

    private void importFromTarball(FhirProfileDetail fhirProfileDetail, InputStream fileInputStream, String authToken, String sandboxId, String id, String apiEndpoint, String apiSchemaURL, ProfileTask profileTask, List<FhirProfile> fhirProfiles, TarArchiveInputStream tarArchiveInputStream) throws IOException {
        
        LOGGER.info("importFromTarball");

        LOGGER.debug("importFromTarball: "
        +"(BEFORE) Parameters: fhirProfileData = "+fhirProfileDetail+", fileInputStream = "+fileInputStream
        +", authToken = "+authToken+", sandboxId = "+sandboxId+", id = "+id+", apiEndpoint = "+apiEndpoint
        +", apiSchemaURL = "+apiSchemaURL+", profileTask = "+profileTask+", fhirProfiles = "+fhirProfiles
        +", tarArchiveInputStream = "+tarArchiveInputStream);

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

        LOGGER.debug("importFromTarball: "
        +"(AFTER) Parameters: fhirProfileData = "+fhirProfileDetail+", fileInputStream = "+fileInputStream
        +", authToken = "+authToken+", sandboxId = "+sandboxId+", id = "+id+", apiEndpoint = "+apiEndpoint
        +", apiSchemaURL = "+apiSchemaURL+", profileTask = "+profileTask+", fhirProfiles = "+fhirProfiles
        +", tarArchiveInputStream = "+tarArchiveInputStream+"; No return value");

    }

    @Async("taskExecutor")
    @Override
    public void saveTarballfile (FhirProfileDetail fhirProfileDetail, InputStream fileInputStream, String authToken, String sandboxId, String id) throws IOException {
        
        LOGGER.info("saveTarballfile");

        String apiEndpoint = sandboxService.findBySandboxId(sandboxId).getApiEndpointIndex();
        ProfileTask profileTask = addToProfileTask(id, true, new HashMap<>(), new HashMap<>(), 0, 0, 0 );
        List<FhirProfile> fhirProfiles = new ArrayList<>();
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(fileInputStream);
        importFromTarball(fhirProfileDetail, fileInputStream, authToken, sandboxId, id, apiEndpoint, sandboxService.getApiSchemaURL(apiEndpoint), profileTask, fhirProfiles, tarArchiveInputStream);

        LOGGER.debug("saveTarballfile: "
        +"Parameters: fhirProfileDetail = "+fhirProfileDetail+", fileInputStream = "+fileInputStream
        +", authToken = "+authToken+", sandboxId = "+sandboxId+", id = "+id+"; No return value");

    }

    private JSONObject saveProfileResource(String apiSchemaURL, String authToken, String sandboxId, String apiEndpoint, String id, InputStream inputStream, String fileName, ProfileTask profileTask) {
        
        LOGGER.info("saveProfileResource");

        LOGGER.debug("saveProfileResource: "
        +"(BEFORE) Parameters: apiSchemaURL = "+apiSchemaURL+", authToken = "+authToken
        +", sandboxId = "+sandboxId+", apiEndpoint = "+apiEndpoint+", id = "+id
        +", inputStream = "+inputStream+", fileName = "+fileName+", profileTask = "+profileTask);

        JSONObject profileTaskAndFhirProfile = new JSONObject();
        var resourceSaved = profileTask.getResourceSaved();
        var resourceNotSaved = profileTask.getResourceNotSaved();
        int totalCount = profileTask.getTotalCount();
        int resourceSavedCount = profileTask.getResourceSavedCount();
        int resourceNotSavedCount = profileTask.getResourceNotSavedCount();
        profileTask = addToProfileTask(id, true, resourceSaved, resourceNotSaved, totalCount, resourceSavedCount, resourceNotSavedCount);
        idProfileTask.put(id, profileTask);
        profileTaskAndFhirProfile.put("profileTask", profileTask);
        FhirProfile fhirProfile = new FhirProfile();
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
            String resourceType = jsonObject.get("resourceType")
                                            .toString();
            String resourceId = "";
            String fullUrl = "";
            String fhirVersion = "";
            if (Arrays.stream(profileResources)
                      .anyMatch(resourceType::equals)) {
                if (jsonObject.containsKey("id")) {
                    resourceId = jsonObject.get("id")
                                           .toString();
                }

                if (jsonObject.containsKey("url")) {
                    fullUrl = jsonObject.get("url")
                                        .toString();
                } else {
                    profileTask.setError("The file name: " + fileName + " is missing url metadata. The profile was not saved.");
                    profileTask.setStatus(false);
                    idProfileTask.put(id, profileTask);
                    profileTaskAndFhirProfile.put("profileTask", profileTask);

                    LOGGER.debug("saveProfileResource: "
                    +"(AFTER) Parameters: apiSchemaURL = "+apiSchemaURL+", authToken = "+authToken
                    +", sandboxId = "+sandboxId+", apiEndpoint = "+apiEndpoint+", id = "+id
                    +", inputStream = "+inputStream+", fileName = "+fileName+", profileTask = "+profileTask
                    +"; Return value = "+profileTaskAndFhirProfile);

                    return profileTaskAndFhirProfile;
                }

                if (resourceType.equals("StructureDefinition")) {
                    if (jsonObject.containsKey("fhirVersion")) {
                        fhirVersion = jsonObject.get("fhirVersion")
                                                .toString();
                    } else {
                        profileTask.setError("The StructureDefinition under file name: " + fileName + " is missing fhirVersion. The profile was not saved.");
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        
                        LOGGER.debug("saveProfileResource: "
                        +"(AFTER) Parameters: apiSchemaURL = "+apiSchemaURL+", authToken = "+authToken
                        +", sandboxId = "+sandboxId+", apiEndpoint = "+apiEndpoint+", id = "+id
                        +", inputStream = "+inputStream+", fileName = "+fileName+", profileTask = "+profileTask
                        +"; Return value = "+profileTaskAndFhirProfile);

                        return profileTaskAndFhirProfile;
                    }

                    try {
                        String profileType = jsonObject.get("type")
                                                       .toString();
                        fhirProfile.setProfileType(profileType);
                    } catch (Exception e) {
                        profileTask.setError("The StructureDefinition under file name: " + fileName + " is missing 'type' parameter. The profile was not saved.");
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        
                        LOGGER.debug("saveProfileResource: "
                        +"(AFTER) Parameters: apiSchemaURL = "+apiSchemaURL+", authToken = "+authToken
                        +", sandboxId = "+sandboxId+", apiEndpoint = "+apiEndpoint+", id = "+id
                        +", inputStream = "+inputStream+", fileName = "+fileName+", profileTask = "+profileTask
                        +"; Return value = "+profileTaskAndFhirProfile);

                        return profileTaskAndFhirProfile;
                    }
                    String errorMessage = "";
                    if (apiEndpoint.equals("8") && !(fhirVersion.equals("1.0.1") || fhirVersion.equals("1.0.2"))) {
                        errorMessage = fileName + " FHIR version (" + fhirVersion + ") is incompatible with your current sandbox's FHIR version (1.0.2). The profile was not saved.";
                        profileTask.setError(errorMessage);
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        
                        LOGGER.debug("saveProfileResource: "
                        +"(AFTER) Parameters: apiSchemaURL = "+apiSchemaURL+", authToken = "+authToken
                        +", sandboxId = "+sandboxId+", apiEndpoint = "+apiEndpoint+", id = "+id
                        +", inputStream = "+inputStream+", fileName = "+fileName+", profileTask = "+profileTask
                        +"; Return value = "+profileTaskAndFhirProfile);

                        return profileTaskAndFhirProfile;
                    } else if (apiEndpoint.equals("9") && !(fhirVersion.equals("3.0.1") || fhirVersion.equals("3.0.2") || fhirVersion.equals("3.1.0") || fhirVersion.equals("3.0.0"))) {
                        errorMessage = fileName + " FHIR version (" + fhirVersion + ") is incompatible with your current sandbox's FHIR version (3.0.2). The profile was not saved.";
                        profileTask.setError(errorMessage);
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);

                        LOGGER.debug("saveProfileResource: "
                        +"(AFTER) Parameters: apiSchemaURL = "+apiSchemaURL+", authToken = "+authToken
                        +", sandboxId = "+sandboxId+", apiEndpoint = "+apiEndpoint+", id = "+id
                        +", inputStream = "+inputStream+", fileName = "+fileName+", profileTask = "+profileTask
                        +"; Return value = "+profileTaskAndFhirProfile);

                        return profileTaskAndFhirProfile;
                    } else if (apiEndpoint.equals("10") && !(fhirVersion.equals("4.0.0") || fhirVersion.equals("4.0.1") || fhirVersion.equals("1.8.0"))) {
                        errorMessage = fileName + " FHIR version (" + fhirVersion + ") is incompatible with your current sandbox's FHIR version (4.0.1). The profile was not saved.";
                        profileTask.setError(errorMessage);
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);

                        LOGGER.debug("saveProfileResource: "
                        +"(AFTER) Parameters: apiSchemaURL = "+apiSchemaURL+", authToken = "+authToken
                        +", sandboxId = "+sandboxId+", apiEndpoint = "+apiEndpoint+", id = "+id
                        +", inputStream = "+inputStream+", fileName = "+fileName+", profileTask = "+profileTask
                        +"; Return value = "+profileTaskAndFhirProfile);

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
                        resourceSaved.computeIfAbsent(resourceType, k -> new ArrayList<>())
                                     .add(resourceId);
                        totalCount++;
                        resourceSavedCount++;
                        profileTask = addToProfileTask(id, true, resourceSaved, resourceNotSaved, totalCount, resourceSavedCount, resourceNotSavedCount);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);

                        fhirProfile.setFullUrl(fullUrl);
                        fhirProfile.setRelativeUrl(resourceType + "/" + resourceId);
                        profileTaskAndFhirProfile.put("fhirProfile", fhirProfile);

                    } catch (HttpServerErrorException | HttpClientErrorException e) {
                        resourceNotSaved.computeIfAbsent(resourceType, k -> new ArrayList<>())
                                        .add(resourceId + " - " + e.getMessage());
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
                            JSONObject savedResource = (JSONObject) jsonParser.parse(responseEntity.getBody()
                                                                                                   .toString());
                            resourceId = savedResource.get("id")
                                                      .toString();
                            resourceSaved.computeIfAbsent(resourceType, k -> new ArrayList<>())
                                         .add(resourceId);
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
                        resourceNotSaved.computeIfAbsent(resourceType, k -> new ArrayList<>())
                                        .add(resourceId + " - " + e.getMessage());
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

        LOGGER.debug("saveProfileResource: "
        +"(AFTER) Parameters: apiSchemaURL = "+apiSchemaURL+", authToken = "+authToken
        +", sandboxId = "+sandboxId+", apiEndpoint = "+apiEndpoint+", id = "+id
        +", inputStream = "+inputStream+", fileName = "+fileName+", profileTask = "+profileTask
        +"; Return value = "+profileTaskAndFhirProfile);

        return profileTaskAndFhirProfile;
    }

    private ProfileTask addToProfileTask(String id, Boolean runStatus, Map<String, List<String>> resourceSaved,
                                         Map<String, List<String>> resourceNotSaved, int totalCount, int resourceSavedCount,
                                         int resourceNotSavedCount) {
        
        LOGGER.info("addToProfileTask");

        ProfileTask profileTask = new ProfileTask();
        profileTask.setId(id);
        profileTask.setStatus(runStatus);
        profileTask.setResourceSaved(resourceSaved);
        profileTask.setResourceNotSaved(resourceNotSaved);
        profileTask.setTotalCount(totalCount);
        profileTask.setResourceSavedCount(resourceSavedCount);
        profileTask.setResourceNotSavedCount(resourceNotSavedCount);

        LOGGER.debug("addToProfileTask: "
        +"Parameters: id = "+id+", runStatus = "+runStatus+", resourceSaved = "+resourceSaved
        +", resourceNotSaved = "+resourceNotSaved+", totalCount = "+totalCount
        +",resourceSavedCount = "+resourceSavedCount+", resourceNotSavedCount = "+resourceNotSavedCount
        +"; Return value = "+profileTask);

        return profileTask;
    }

}

