package org.hspconsortium.sandboxmanagerapi.controllers;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = FhirProfileController.class, secure = false)
@ContextConfiguration(classes = FhirProfileController.class)
public class FhirProfileControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FhirProfileService fhirProfileService;

    @MockBean
    private FhirProfileDetailService fhirProfileDetailService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthorizationService authorizationService;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    private FhirProfile fhirProfile;
    private FhirProfileDetail fhirProfileDetail;
    private Sandbox sandbox;
    private User user;
    private ProfileTask profileTask;
    private HashMap<String, ProfileTask> idProfileTask;
    private List<FhirProfile> fhirProfiles;
    private List<FhirProfileDetail> fhirProfileDetails;

    @Before
    public void setup() {
        Timestamp timestamp = new Timestamp(new Date().getTime());
        sandbox = new Sandbox();
        sandbox.setApiEndpointIndex("9");
        sandbox.setSandboxId("Sandbox");
        sandbox.setId(1);

        user = new User();
        user.setSbmUserId("me");

        fhirProfile = new FhirProfile();
        fhirProfile.setFhirProfileId(1);
        fhirProfile.setProfileType("Patient");
        fhirProfile.setRelativeUrl("StructureDefinition/us-core-allergyintolerance");
        fhirProfile.setFhirProfileId(1);
        fhirProfile.setFullUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-allergyintolerance");

        fhirProfiles = new ArrayList<>();
        fhirProfiles.add(fhirProfile);

        fhirProfileDetail = new FhirProfileDetail();
        fhirProfileDetail.setProfileName("USCore");
        fhirProfileDetail.setProfileId("USCore");
        fhirProfileDetail.setSandbox(sandbox);
        fhirProfileDetail.setCreatedBy(user);
        fhirProfileDetail.setCreatedTimestamp(timestamp);
        fhirProfileDetail.setLastUpdated(timestamp);

        profileTask = new ProfileTask();
        profileTask.setStatus(true);

        idProfileTask = new HashMap<>();
        fhirProfileDetails = new ArrayList<>();
        fhirProfileDetails.add(fhirProfileDetail);

    }

    @Test
    public void uploadProfileTest() throws Exception {
        when(fhirProfileDetailService.findByProfileIdAndSandboxId("uscore", sandbox.getSandboxId())).thenReturn(fhirProfileDetail);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        when(authorizationService.getSystemUserId(any())).thenReturn(user.getSbmUserId());
        when(authorizationService.checkSandboxUserNotReadOnlyAuthorization(null, sandbox)).thenReturn(user.getSbmUserId());
        doNothing().when(fhirProfileDetailService).saveZipFile(any(), any(), any(), any(), any());
        doNothing().when(fhirProfileDetailService).saveTGZfile(any(), any(), any(), any(), any());

        MockMultipartFile file = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());

        mvc
                .perform(
                        fileUpload("/profile/uploadProfile?file=file.zip&sandboxId=" + sandbox.getSandboxId() +
                                "&profileName=" + fhirProfileDetail.getProfileName() + "&profileId=" + fhirProfileDetail.getProfileId())
                                .file(file));

//                .perform(
//                        MockMvcRequestBuilders.fileUpload("/profile/uploadProfile?file=file.zip&sandboxId=" + sandbox.getSandboxId() +
//                                "&profileName=" + fhirProfileDetail.getProfileName() + "&profileId=" + fhirProfileDetail.getProfileId())
//                                .file(file))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//                .andExpect(status().isOk());;
    }

//                        post("/profile/uploadProfile?sandboxId=" + sandbox.getSandboxId() +
//                                "&profileName=" + fhirProfileDetail.getProfileName() + "&profileId=" + fhirProfileDetail.getProfileId())
//                                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                                .content(json))

    @Test
    public void fetchStatusTest() throws Exception {
        when(fhirProfileDetailService.getTaskRunning("abcd")).thenReturn(profileTask);
        when(fhirProfileDetailService.getIdProfileTask()).thenReturn(idProfileTask);
        mvc
                .perform(get("/profile/profileUploadStatus?id=abcd"))
                .andExpect(status().isOk());

    }

    @Test
    public void getStructureDefinitionsTest() throws Exception {
        when(fhirProfileService.getAllResourcesForGivenProfileId(1)).thenReturn(fhirProfiles);
        mvc
                .perform(get("/profile/getProfileSDs?fhirProfileId=1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getFhirProfilesTest() throws Exception {
        when(fhirProfileDetailService.getAllProfilesForAGivenSandbox("sandbox")).thenReturn(fhirProfileDetails);
        mvc
                .perform(get("/profile?sandboxId=sandbox"))
                .andExpect(status().isOk());
    }

    @Test
    public void getFhirProfileTest() throws Exception {
        when(fhirProfileDetailService.getFhirProfileDetail(1)).thenReturn(fhirProfileDetail);
        mvc
                .perform(get("/profile?fhirProfileId=1"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteProfileTest() throws Exception {

        when(sandboxService.findBySandboxId("sandbox")).thenReturn(sandbox);
        when(userService.findBySbmUserId("user")).thenReturn(user);
        when(authorizationService.checkSandboxUserNotReadOnlyAuthorization(null, sandbox)).thenReturn(user.getSbmUserId());
        doNothing().when(fhirProfileDetailService).delete(null, 1, sandbox.getSandboxId());
        mvc
                .perform(get("/profile?fhirProfileId=1&sandboxId=sandbox"))
                .andExpect(status().isOk());
    }


    @SuppressWarnings("unchecked")
    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
