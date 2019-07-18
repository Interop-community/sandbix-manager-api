package org.hspconsortium.sandboxmanagerapi.services;

import org.apache.commons.io.FileUtils;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirProfileDetailRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.FhirProfileDetailServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.junit.Rule;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

    @Rule
    public TemporaryFolder tempFolder= new TemporaryFolder();

    @Before
    public void setup() throws Exception {
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
    public void saveZipFileTest() throws Exception {
        ZipFile zipFile = new ZipFile("src/test/resources/testUSCoreProfile.zip");
        when(sandboxService.findBySandboxId("sandbox")).thenReturn(sandbox);
        when(sandboxService.getApiSchemaURL("apiEndpointIndex")).thenReturn("localhost");
        when(repository.save(fhirProfileDetail)).thenReturn(fhirProfileDetail);
        fhirProfileDetailService.saveZipFile(fhirProfileDetail, zipFile, "a", "sandbox", "1");
        verify(repository).save(fhirProfileDetail);
    }

    @Test
    public void saveTGZfile() throws Exception {
        File file = new File("src/test/resources/testUSCoreProfile.tgz");
        InputStream fileInputStream = new FileInputStream(file);
        when(sandboxService.findBySandboxId("sandbox")).thenReturn(sandbox);
        when(sandboxService.getApiSchemaURL("apiEndpointIndex")).thenReturn("localhost");
        when(repository.save(fhirProfileDetail)).thenReturn(fhirProfileDetail);
        fhirProfileDetailService.saveTGZfile(fhirProfileDetail, fileInputStream, "a", "sandbox", "1");
        verify(sandboxService).findBySandboxId("sandbox");
    }
}
