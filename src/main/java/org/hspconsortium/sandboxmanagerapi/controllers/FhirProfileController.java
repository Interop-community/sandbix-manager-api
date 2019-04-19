package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.IOUtil;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirProfileDetailRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.json.simple.JSONObject;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
    private FhirProfileDetailRepository repository;

    @Inject
    public FhirProfileController(final FhirProfileService fhirProfileService, final SandboxService sandboxService,
                                 final UserService userService, final AuthorizationService authorizationService,
                                 final FhirProfileDetailService fhirProfileDetailService, final FhirProfileDetailRepository repository) {
        this.fhirProfileService = fhirProfileService;
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.authorizationService = authorizationService;
        this.fhirProfileDetailService = fhirProfileDetailService;
        this.repository = repository;
    }

    @PostMapping(value = "/uploadProfile", params = {"sandboxId", "apiEndpoint", "profileName", "profileId"})
    public JSONObject uploadProfile (@RequestParam("file") MultipartFile file, HttpServletRequest request,
                                     @RequestParam(value = "sandboxId") String sandboxId,
                                     @RequestParam(value = "apiEndpoint") String apiEndpoint,
                                     @RequestParam(value = "profileName") String profileName,
                                     @RequestParam(value = "profileId") String profileId) throws IOException {

        FhirProfileDetail existingFhirProfileDetail = repository.findByProfileIdAndSandboxId(profileId, sandboxId);
        if (existingFhirProfileDetail != null) {
            throw new IllegalArgumentException("");
        }

        String id = UUID.randomUUID().toString();
        String fileName = file.getOriginalFilename();
        String fileExtension = FilenameUtils.getExtension(fileName);
        JSONObject statusReturned = new JSONObject();
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        Visibility visibility = authorizationService.getDefaultVisibility(user, sandbox);
        //TODO: add user authentication
//        if(!sandboxService.verifyUser(request, sandboxId)) {
//            throw new UnauthorizedUserException("User not authorized");
//        }
        if (!fileName.isEmpty() & !fileExtension.equals("tgz")) {
            File zip = File.createTempFile(id, "temp");
            FileOutputStream outputStream = new FileOutputStream(zip);
            IOUtil.copy(file.getInputStream(), outputStream);
            outputStream.close();
            try {
                ZipFile zipFile = new ZipFile(zip);
                fhirProfileDetailService.saveZipFile(zipFile, request, sandboxId, apiEndpoint, id, profileName, profileId, user, visibility);
            } catch (ZipException e) {
                e.printStackTrace();
            }
            finally {
                zip.delete();
            }
        } else if (!fileName.isEmpty() & fileExtension.equals("tgz")) {
            fhirProfileDetailService.saveTGZfile(file, request, sandboxId, apiEndpoint, id, profileName, profileId, user, visibility);
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

    @GetMapping(value = "/getSDs", params = {"profileId"})
    public List<JSONObject> getStructureDefinitions () {
        List<JSONObject> structureDefinitions = new ArrayList<>();


        return structureDefinitions;
    }

    @PostMapping
    public void saveProfile(HttpServletRequest request, @RequestBody List<FhirProfile> fhirProfiles) {
        for (FhirProfile fhirProfile : fhirProfiles) {
//            Sandbox sandbox = sandboxService.findBySandboxId(fhirProfile.getSandbox().getSandboxId());
//            if (sandbox == null) {
//                throw new ResourceNotFoundException("Sandbox does not exist");
//            }
            User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
            Timestamp timestamp = new Timestamp(new Date().getTime());
            FhirProfileDetail fhirProfileDetail = new FhirProfileDetail();
            fhirProfileDetail.setFhirProfiles(fhirProfiles);

//            fhirProfileDetail.setProfileId(fhirProfile.getProfileId());
//            fhirProfileDetail.setProfileName(fhirProfile.getProfileName());
//            fhirProfileDetail.setSandbox(fhirProfile.getSandbox());

            fhirProfileDetail.setCreatedBy(user);
            fhirProfileDetail.setCreatedTimestamp(timestamp);
            fhirProfileDetail.setLastUpdated(timestamp);
//            fhirProfileDetail.setVisibility(authorizationService.getDefaultVisibility(user, sandbox));
            FhirProfileDetail fhirProfileDetailSaved = fhirProfileDetailService.save(fhirProfileDetail);

//            fhirProfile.setSandbox(sandbox);
            fhirProfile.setFhirProfileId(fhirProfileDetailSaved.getId());
            FhirProfile existingFhirProfile = fhirProfileService.findByFullUrlAndFhirProfileId(fhirProfile.getFullUrl(), fhirProfile.getFhirProfileId());
            if (existingFhirProfile != null) {
                fhirProfileService.save(fhirProfile);
            }
        }
    }

    @GetMapping(params = {"sandboxId"}, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<FhirProfileDetail> getFhirProfiles(@RequestParam(value = "sandboxId") String sandboxId) {
        return fhirProfileDetailService.getFhirProfileDetails(sandboxId);
    }

    @GetMapping(value = "/{fhirProfileId}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public FhirProfileDetail getFhirProfile(@RequestParam(value = "fhirProfileId") Integer fhirProfileId) {
        return fhirProfileDetailService.getFhirProfileDetail(fhirProfileId);
    }

    @DeleteMapping(value = "/{fhirProfileId}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public void deleteProfile(@RequestParam(value = "fhirProfileId") Integer fhirProfileId) {
        fhirProfileDetailService.delete(fhirProfileId);
    }

    //TODO: Add update: OR may be not needed
}
