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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class FhirProfileDetailServiceImpl implements FhirProfileDetailService {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${hspc.platform.api.fhir.profileResources}")
    private String[] profileResources;

    @Value("${hspc.platform.api.sandboxManagerApi.url}") //TODO: make the url
    private String sandboxManagerApiUrl;

    private FhirProfileDetailRepository repository;
    private FhirProfileService fhirProfileService;
    private SandboxService sandboxService;

    @Inject
    public FhirProfileDetailServiceImpl(FhirProfileDetailRepository repository, FhirProfileService fhirProfileService, SandboxService sandboxService) {
        this.repository = repository;
        this.fhirProfileService = fhirProfileService;
        this.sandboxService = sandboxService;
    }

    private HashMap<String, ProfileTask> idProfileTask = new HashMap<>();

    public ProfileTask getTaskRunning(String id) { return idProfileTask.get(id); }

    public HashMap<String, ProfileTask> getIdProfileTask() { return idProfileTask; }

    @Override
    @Transactional
    public FhirProfileDetail save(FhirProfileDetail fhirProfileDetail) {
        Sandbox sandbox = fhirProfileDetail.getSandbox();
        FhirProfileDetail existingFhirProfileDetail = repository.findByProfileIdAndSandboxId(fhirProfileDetail.getProfileId(), sandbox.getSandboxId());
        if (existingFhirProfileDetail != null) {
            existingFhirProfileDetail.setLastUpdated(new Timestamp(new Date().getTime()));
            saveFhirProfile(fhirProfileDetail.getFhirProfiles());
            return repository.save(existingFhirProfileDetail);
        } else {
            saveFhirProfile(fhirProfileDetail.getFhirProfiles());
            return repository.save(fhirProfileDetail);
        }
    }

    private void saveFhirProfile(List<FhirProfile> fhirProfiles) {
        for (FhirProfile fhirProfile : fhirProfiles) {
            fhirProfileService.save(fhirProfile);
        }
    }

    @Override
    @Transactional
    public FhirProfileDetail update(FhirProfileDetail fhirProfileDetail) {
        //TODO: maybe not needed
        Sandbox sandbox = fhirProfileDetail.getSandbox();
        FhirProfileDetail existingFhirProfileDetail = repository.findByProfileIdAndSandboxId(fhirProfileDetail.getProfileId(), sandbox.getSandboxId());
        existingFhirProfileDetail.setLastUpdated(new Timestamp(new Date().getTime()));
        return repository.save(existingFhirProfileDetail);
    }

    @Override
    public FhirProfileDetail getFhirProfileDetail(Integer fhirProfileId) {
        return repository.findByFhirProfileId(fhirProfileId);
    }

    @Override
    public List<FhirProfileDetail> getFhirProfileDetails(String sandboxId) {
        return repository.findBySandboxId(sandboxId);
    }

    @Override
    @Transactional
    public void delete(final Integer fhirProfileId) {
        FhirProfileDetail fhirProfileDetail = repository.findByFhirProfileId(fhirProfileId);
        repository.delete(fhirProfileDetail);
        fhirProfileService.delete(fhirProfileId);
    }

    @Async("taskExecutor")
    @Override
    public void saveZipFile (ZipFile zipFile, HttpServletRequest request, String sandboxId, String apiEndpoint, String id, String profileName, String profileId, User user, Visibility visibility) throws IOException {
        String authToken = request.getHeader("Authorization");
        List<String> resourceSaved = new ArrayList<>();
        List<String> resourceNotSaved = new ArrayList<>();
        int totalCount = 0;
        int resourceSavedCount = 0;
        int resourceNotSavedCount = 0;
        ProfileTask profileTask = addToProfileTask(id, true, resourceSaved, resourceNotSaved, totalCount, resourceSavedCount, resourceNotSavedCount );
        List<FhirProfile> fhirProfiles = new ArrayList<>();
        Enumeration zipFileEntries = zipFile.entries();
        while(zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String fileName = entry.getName();
            if (fileName.endsWith(".json")) {
                InputStream inputStream = zipFile.getInputStream(entry);
                JSONObject profileTaskAndFhirProfile = saveProfileResource(authToken, sandboxId, apiEndpoint, id, inputStream, fileName, profileTask, profileName, profileId);
                profileTask = (ProfileTask) profileTaskAndFhirProfile.get("profileTask");
                if (profileTask.getError() != null) {
                    break;
                }
                FhirProfile fhirProfile = (FhirProfile) profileTaskAndFhirProfile.get("fhirProfile");
                if (fhirProfile != null) {
                    fhirProfiles.add(fhirProfile);
                }
            }
        }
        profileTask.setStatus(false);
        idProfileTask.put(id, profileTask);

        Timestamp timestamp = new Timestamp(new Date().getTime());
        FhirProfileDetail fhirProfileDetail = new FhirProfileDetail();
        fhirProfileDetail.setFhirProfiles(fhirProfiles);
        fhirProfileDetail.setLastUpdated(timestamp);
        fhirProfileDetail.setCreatedTimestamp(timestamp);  //todo: figure out the right place to set this
        fhirProfileDetail.setProfileName(profileName);
        fhirProfileDetail.setProfileId(profileId);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        fhirProfileDetail.setSandbox(sandbox);
        fhirProfileDetail.setCreatedBy(user);
        fhirProfileDetail.setVisibility(visibility);
        save(fhirProfileDetail);
    }

    @Async("taskExecutor")
    @Override
    public void saveTGZfile (MultipartFile file, HttpServletRequest request, String sandboxId, String apiEndpoint, String id, String profileName, String profileId, User user, Visibility visibility) throws IOException {
        //TODO: implement the new changes as saveZipFile
        String authToken = request.getHeader("Authorization");
        List<String> resourceSaved = new ArrayList<>();
        List<String> resourceNotSaved = new ArrayList<>();
        int totalCount = 0;
        int resourceSavedCount = 0;
        int resourceNotSavedCount = 0;
        ProfileTask profileTask = addToProfileTask(id, true, resourceSaved, resourceNotSaved, totalCount, resourceSavedCount, resourceNotSavedCount );
        InputStream fileInputStream = file.getInputStream();
        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(fileInputStream));
        TarArchiveEntry entry;
        while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            String fileName = entry.getName();
            String fileExtension = FilenameUtils.getExtension(fileName);
            if (fileExtension.equals("json")) {
                profileTask = (ProfileTask) saveProfileResource(authToken, sandboxId, apiEndpoint, id, tarArchiveInputStream, fileName, profileTask, profileName, profileId).get("profileTask");
                if (profileTask.getError() != null) {
                    break;
                }
            }
        }
        tarArchiveInputStream.close();
        profileTask.setStatus(false);
        idProfileTask.put(id, profileTask);
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

    private JSONObject saveProfileResource(String authToken, String sandboxId, String apiEndpoint, String id, InputStream inputStream, String fileName, ProfileTask profileTask, String profileName, String profileId) {
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
            if (Arrays.stream(profileResources).anyMatch(resourceType::equals)) {
                String resourceId = jsonObject.get("id").toString();
                String fullUrl = jsonObject.get("url").toString();
                if (resourceType.equals("StructureDefinition")) {
                    String fhirVersion = jsonObject.get("fhirVersion").toString();
                    String profileType = jsonObject.get("type").toString();
                    fhirProfile.setProfileType(profileType);
                    String errorMessage = "";
                    if (apiEndpoint.equals("8") && !fhirVersion.equals("1.0.2")) {
                        errorMessage = fileName + " FHIR version (" + fhirVersion + ") is incompatible with your current sandbox's FHIR version (1.0.2). The profile was not saved.";
                        profileTask.setError(errorMessage);
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        return profileTaskAndFhirProfile;
                    } else if (apiEndpoint.equals("9") && !fhirVersion.equals("3.0.1")) {
                        errorMessage = fileName + " FHIR version (" + fhirVersion + ") is incompatible with your current sandbox's FHIR version (3.0.1). The profile was not saved.";
                        profileTask.setError(errorMessage);
                        profileTask.setStatus(false);
                        idProfileTask.put(id, profileTask);
                        profileTaskAndFhirProfile.put("profileTask", profileTask);
                        return profileTaskAndFhirProfile;
                    } else if (apiEndpoint.equals("10") && !fhirVersion.equals("4.0.0")) {
                        errorMessage = fileName + " FHIR version (" + fhirVersion + ") is incompatible with your current sandbox's FHIR version (4.0.0). The profile was not saved.";
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
                String url = localhost + "/" + sandboxId + "/data/" + resourceType + "/" + resourceId;
                HttpEntity entity = new HttpEntity(jsonBody, headers);
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
            }
        } catch (Exception e) {

        }
        return profileTaskAndFhirProfile;
    }

}




//            if (fileName.endsWith(".xml")) {
//                beginsWith = fileName.substring(0, fileName.indexOf("-"));
//                if(beginsWith.equals("StructureDefinition") || (beginsWith.equals("ValueSet")) || (beginsWith.equals("CodeSystem"))) {
//                    InputStream inputStream = zipFile.getInputStream(entry);
//
//                    try {
//                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//                        StringBuilder stringBuilder = new StringBuilder();
//                        String inline = "";
//                        while ((inline = inputReader.readLine()) != null) {
//                            stringBuilder.append(inline);
//                        }
//                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//                        DocumentBuilder builder = factory.newDocumentBuilder();
//                        InputSource source = new InputSource();
//                        source.setCharacterStream(new StringReader(stringBuilder.toString()));
//
//                        Document doc = builder.parse(source);
//
////                        Document doc = builder.parse(new InputSource (new ByteArrayInputStream(stringBuilder.toString().getBytes(StandardCharsets.UTF_8))));
////                        InputStream inputStream2 = new    ByteArrayInputStream(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
////                        org.w3c.dom.Document doc = builder.parse(inputStream2);
//
//                        Element element = doc.getDocumentElement();
//                        String id = element.getAttribute("id value");
//
//                        HttpHeaders headers = new HttpHeaders();
//                        headers.set("Authorization", "BEARER " + authToken);
//                        headers.set("Content-Type", "application/xml");
//
//                        String url = localhost + "/" + sandboxId + "/data/" +  beginsWith + "/" + element.getAttribute("id value");
//                        HttpEntity entity = new HttpEntity(stringBuilder.toString(), headers);
//                        try {
//                            restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
//                        } catch (HttpClientErrorException e) {
//                            logger.error("File not saved: " + fileName);
//                        }
//                    } catch (Exception e) {
//                        logger.error(e.getMessage() + "Unsupported file " + fileName);
//                    }
//                }
//            }

