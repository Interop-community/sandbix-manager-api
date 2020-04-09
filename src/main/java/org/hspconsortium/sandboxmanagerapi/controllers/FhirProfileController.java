package org.hspconsortium.sandboxmanagerapi.controllers;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.IOUtil;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.json.simple.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping({"/profile"})
public class FhirProfileController {
    private FhirProfileService fhirProfileService;
    private SandboxService sandboxService;
    private UserService userService;
    private AuthorizationService authorizationService;
    private FhirProfileDetailService fhirProfileDetailService;

    @Inject
    public FhirProfileController(final FhirProfileService fhirProfileService, final SandboxService sandboxService,
                                 final UserService userService, final AuthorizationService authorizationService,
                                 @Lazy final FhirProfileDetailService fhirProfileDetailService) {
        this.fhirProfileService = fhirProfileService;
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.authorizationService = authorizationService;
        this.fhirProfileDetailService = fhirProfileDetailService;
    }

    // NEED ALL OF THESE
    @PostMapping(value = "/uploadProfile", params = {"sandboxId", "profileName", "profileId"})
    public JSONObject uploadProfile (@RequestParam("file") MultipartFile file, HttpServletRequest request,
                                     @RequestParam(value = "sandboxId") String sandboxId,
                                     @RequestParam(value = "profileName") String profileName,
                                     @RequestParam(value = "profileId") String profileId) throws IOException {

        FhirProfileDetail existingFhirProfileDetail = fhirProfileDetailService.findByProfileIdAndSandboxId(profileId, sandboxId);
        if (existingFhirProfileDetail != null) {
            throw new IllegalArgumentException(profileName + " has already been uploaded");
        }
        String authToken = request.getHeader("Authorization");
        String id = UUID.randomUUID().toString();
        String fileName = file.getOriginalFilename();
        String fileExtension = FilenameUtils.getExtension(fileName);
        JSONObject statusReturned = new JSONObject();
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        Visibility visibility = authorizationService.getDefaultVisibility(user, sandbox);

        if(!authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox).equals(user.getSbmUserId())) {
            throw new UnauthorizedUserException("User not authorized");
        }
        FhirProfileDetail fhirProfileDetail = new FhirProfileDetail();
        Timestamp timestamp = new Timestamp(new Date().getTime());
        fhirProfileDetail.setLastUpdated(timestamp);
        fhirProfileDetail.setCreatedTimestamp(timestamp);
        fhirProfileDetail.setSandbox(sandbox);
        fhirProfileDetail.setCreatedBy(user);
        fhirProfileDetail.setProfileName(profileName);
        fhirProfileDetail.setProfileId(profileId);
        fhirProfileDetail.setVisibility(visibility);

        if (!fileName.isEmpty() & !fileExtension.equals("tgz")) {
            File zip = File.createTempFile(id, "temp");
            FileOutputStream outputStream = new FileOutputStream(zip);
            IOUtil.copy(file.getInputStream(), outputStream);
            outputStream.close();
            try {
                ZipFile zipFile = new ZipFile(zip);
                fhirProfileDetailService.saveZipFile(fhirProfileDetail, zipFile, authToken, sandboxId, id);
            } catch (ZipException e) {
                e.printStackTrace();
            }
            finally {
                zip.delete();
            }
        } else if (!fileName.isEmpty() & fileExtension.equals("tgz")) {
            InputStream fileInputStream = file.getInputStream();
            fhirProfileDetailService.saveTGZfile(fhirProfileDetail, fileInputStream, authToken, sandboxId, id);
        } else {
            statusReturned.put("status", false);
            statusReturned.put("id", id);
            statusReturned.put("responseEntity", HttpStatus.BAD_REQUEST);
            return statusReturned;
        }
        statusReturned.put("status", true);
        statusReturned.put("id", id);
        statusReturned.put("responseEntity", HttpStatus.OK);
        return statusReturned;
    }

    // NEED THIS
    @RequestMapping(value = "/profileUploadStatus", params = {"id"})
    @ResponseBody
    public ProfileTask fetchStatus(@RequestParam(value = "id") String id) {
        ProfileTask profileTask = fhirProfileDetailService.getTaskRunning(id);
        if (profileTask != null) {
            ProfileTask profileTaskCopy = profileTask;
            if (!profileTask.getStatus()){
                fhirProfileDetailService.getIdProfileTask().remove(id);
            }
            return profileTaskCopy;
        } else {
            ProfileTask profileTaskNull = new ProfileTask();
            return profileTaskNull;
        }
    }

    @GetMapping(value = "/getProfileSDs", params = {"fhirProfileId"})
    @ResponseBody
    public List<FhirProfile> getStructureDefinitions (@RequestParam(value = "fhirProfileId") Integer fhirProfileId) {
        return fhirProfileService.getAllSDsForGivenProfileId(fhirProfileId);
    }

    @GetMapping(value = "/getProfileResources", params = {"fhirProfileId"})
    @ResponseBody
    public List<FhirProfile> getAllResourcesForGivenProfileId (@RequestParam(value = "fhirProfileId") Integer fhirProfileId) {
        return fhirProfileService.getAllResourcesForGivenProfileId(fhirProfileId);
    }

    @GetMapping(params = {"sandboxId"}, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<FhirProfileDetail> getFhirProfiles(@RequestParam(value = "sandboxId") String sandboxId) {
        return fhirProfileDetailService.getAllProfilesForAGivenSandbox(sandboxId);
    }

    @GetMapping(params = {"sandboxId", "type"}, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public HashMap<String, List<FhirProfile>> getFhirProfilesWithASpecificType(@RequestParam(value = "sandboxId") String sandboxId, @RequestParam(value = "type") String type) {
        HashMap<String, List<FhirProfile>> profileNameAndFhirProfile = new HashMap<>();
        List<Integer> fhirProfileIds = fhirProfileDetailService.getAllFhirProfileIdsAssociatedWithASandbox(sandboxId);
        for (Integer fhirProfileId: fhirProfileIds) {
            List<FhirProfile> fhirProfile = fhirProfileDetailService.getFhirProfileWithASpecificTypeForAGivenSandbox(fhirProfileId, type);
            if (fhirProfile != null) {
                String profileName = fhirProfileDetailService.getFhirProfileDetail(fhirProfileId).getProfileName();
                profileNameAndFhirProfile.put(profileName, fhirProfile);
            }
        }
        return profileNameAndFhirProfile;
    }

    @GetMapping(params = {"fhirProfileId"}, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public FhirProfileDetail getFhirProfile(@RequestParam(value = "fhirProfileId") Integer fhirProfileId) {
        return fhirProfileDetailService.getFhirProfileDetail(fhirProfileId);
    }

    @GetMapping(value = "/getAllProfileTypes", params = {"sandboxId"})
    @ResponseBody
    public Set<String> getAllProfileTypes (@RequestParam(value = "sandboxId") String sandboxId) {
        List<FhirProfileDetail> fhirProfiles = fhirProfileDetailService.getAllProfilesForAGivenSandbox(sandboxId);
        Set<String> types = new HashSet<>();
        for (FhirProfileDetail fhirProfile: fhirProfiles) {
            List<String> typesFound = fhirProfileService.getAllProfileTypesForAGivenProfileId(fhirProfile.getId());
            types.addAll(typesFound);
        }
        return types;
    }

    @DeleteMapping(params = {"fhirProfileId", "sandboxId"}, produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public void deleteProfile(HttpServletRequest request,@RequestParam(value = "fhirProfileId") Integer fhirProfileId, @RequestParam(value = "sandboxId") String sandboxId) {
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if(!authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox).equals(user.getSbmUserId())) {
            throw new UnauthorizedUserException("User not authorized");
        }
        fhirProfileDetailService.delete(request, fhirProfileId, sandboxId);
    }
}
