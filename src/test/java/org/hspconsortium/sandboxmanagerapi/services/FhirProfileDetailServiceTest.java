package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirProfileDetailRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.FhirProfileDetailServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class FhirProfileDetailServiceTest {

    private FhirProfileDetailRepository repository = mock(FhirProfileDetailRepository.class);
    private FhirProfileService fhirProfileService = mock(FhirProfileService.class);
    private SandboxService sandboxService = mock(SandboxService.class);
    private RestTemplate restTemplate = mock(RestTemplate.class);
    private HttpServletRequest request = mock(HttpServletRequest.class);

    private FhirProfileDetailServiceImpl fhirProfileDetailService = new FhirProfileDetailServiceImpl(repository);
    private HashMap<String, ProfileTask> idProfileTask = new HashMap<>();
    private FhirProfile fhirProfile;
    private List<FhirProfile> fhirProfiles;
    private FhirProfileDetail fhirProfileDetail;
    private List<FhirProfileDetail> fhirProfileDetails;
    private Sandbox sandbox;
    private User user;

    @Before
    public void setup() {
        String[] profileResources = new String[]{"StructureDefinition", "CodeSystem", "ValueSet", "SearchParameter"};
        ReflectionTestUtils.setField(fhirProfileDetailService, "profileResources", profileResources);
        fhirProfileDetailService.setFhirProfileService(fhirProfileService);
        fhirProfileDetailService.setSandboxService(sandboxService);

        sandbox = new Sandbox();
        sandbox.setSandboxId("sandbox");
        sandbox.setApiEndpointIndex("9");
        user = new User();

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
        fhirProfileDetail.setFhirProfiles(fhirProfiles);
    }

    @Test
    public void saveTest() throws Exception {
        when(repository.save(fhirProfileDetail)).thenReturn(fhirProfileDetail);
        assertEquals(fhirProfileDetail, fhirProfileDetailService.save(fhirProfileDetail));
    }

    @Test
    public void findByProfileIdAndSandboxIdTest() throws Exception {
        when(repository.findByProfileIdAndSandboxId("uscore", "sandbox")).thenReturn(fhirProfileDetail);
        assertEquals(fhirProfileDetail, fhirProfileDetailService.findByProfileIdAndSandboxId("uscore", "sandbox"));
    }

    @Test
    public void getFhirProfileDetailTest() throws Exception {
        when(repository.findByFhirProfileId(1)).thenReturn(fhirProfileDetail);
        assertEquals(fhirProfileDetail, fhirProfileDetailService.getFhirProfileDetail(1));
    }

    @Test
    public void getAllProfilesForAGivenSandbox() throws Exception {
        when(repository.findBySandboxId("sandbox")).thenReturn(fhirProfileDetails);
        assertEquals(fhirProfileDetails, fhirProfileDetailService.getAllProfilesForAGivenSandbox("sandbox"));
    }

    @Test
    public void deleteTest() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("authToken");
        when(fhirProfileService.getAllResourcesForGivenProfileId(1)).thenReturn(fhirProfiles);
        when(sandboxService.findBySandboxId("sandbox")).thenReturn(sandbox);
        when(sandboxService.getApiSchemaURL("apiEndpointIndex")).thenReturn("localhost");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Collection.class))).thenReturn(null);
        fhirProfileDetailService.delete(request, 1, "sandbox");
        verify(fhirProfileService).delete(1);
    }

    @Test
    public void deleteWithFhirProfileIdTest() {
        fhirProfileDetailService.delete(1);
        verify(fhirProfileService).delete(1);
    }

    @Test
    public void saveZipFileTest() {
        InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
        when(sandboxService.findBySandboxId("sandbox")).thenReturn(sandbox);
        when(sandboxService.getApiSchemaURL("apiEndpointIndex")).thenReturn("localhost");
        fhirProfileDetailService.saveZipFile(fhirProfileDetail, inputStream, anyString(), anyString(), anyString());
    }


}
