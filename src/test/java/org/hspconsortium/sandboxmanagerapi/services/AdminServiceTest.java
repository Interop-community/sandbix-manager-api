package org.hspconsortium.sandboxmanagerapi.services;

import org.apache.http.HttpEntity;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxActivityLog;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.impl.AdminServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class AdminServiceTest {

    private AdminServiceImpl adminService = new AdminServiceImpl();
    private UserService userService = mock(UserService.class);
    private SandboxService sandboxService = mock(SandboxService.class);
    private SandboxActivityLogService sandboxActivityLogService = mock(SandboxActivityLogService.class);
    private HttpEntity httpEntity = mock(HttpEntity.class);
    private RestTemplate restTemplate = mock(RestTemplate.class);
    private ResponseEntity<Collection> responseEntity;

    private Iterable<Sandbox> sandboxesIterable;
    private Iterable<SandboxActivityLog> sandboxAccessHistories;
    private Sandbox sandbox;
    private Sandbox sandbox2;
    private SandboxActivityLog sandboxAccessHistory;
    private SandboxActivityLog sandboxAccessHistory2;
    private User user;

    @Before
    public void setup() {
        adminService.setUserService(userService);
        adminService.setSandboxService(sandboxService);
        adminService.setSandboxActivityLogService(sandboxActivityLogService);
        adminService.setRestTemplate(restTemplate);

        sandbox = new Sandbox();
        sandbox.setSandboxId("SND1");

        sandbox2 = new Sandbox();
        sandbox2.setSandboxId("SND2");

        sandboxesIterable = new ArrayList<>();
        ((ArrayList<Sandbox>) sandboxesIterable).add(sandbox);
        ((ArrayList<Sandbox>) sandboxesIterable).add(sandbox2);

        user = new User();
        user.setSbmUserId("ab");

        sandboxAccessHistory = new SandboxActivityLog();
        sandboxAccessHistory.setId(1);
        sandboxAccessHistory.setSandbox(sandbox);

        sandboxAccessHistory2 = new SandboxActivityLog();
        sandboxAccessHistory2.setId(2);
        sandboxAccessHistories = new ArrayList<>();

        String[] dontDeleteInSyncList = new String[]{"SND1"};
        ReflectionTestUtils.setField(adminService, "dontDeleteInSync", dontDeleteInSyncList);
    }

    @Test
    public void syncSandboxManagerandReferenceApiTest() {
        Collection<LinkedHashMap> sandboxesInRAPI = new ArrayList<>();
        ResponseEntity responseEntity2 = new ResponseEntity<Collection>(sandboxesInRAPI, HttpStatus.OK);
        when(sandboxService.findAll()).thenReturn(sandboxesIterable);
        when(sandboxService.getSystemSandboxApiURL()).thenReturn("");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Collection.class))).thenReturn(responseEntity2);
        when(sandboxService.findBySandboxId("SND2")).thenReturn(sandbox2);
        doNothing().when(sandboxService).delete(sandbox2, "", null, true);
        HashMap<String, Object> actual = new HashMap<>();
        List<String> missingInSandboxManagerIds = new ArrayList<>();
        List<String> missingInReferenceApi = new ArrayList<>();
        missingInReferenceApi.add("SND1");
        missingInReferenceApi.add("SND2");
        actual.put("missing_in_sandbox_manager", missingInSandboxManagerIds);
        actual.put("missing_in_reference_api", missingInReferenceApi);
        HashMap<String, Object> expected = adminService.syncSandboxManagerandReferenceApi(true, "");
        assertEquals(expected, actual);
    }

    @Test(expected = RuntimeException.class)
    public void syncSandboxManagerandReferenceApiExceptionTest() {
        when(sandboxService.findAll()).thenReturn(sandboxesIterable);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(null);
        adminService.syncSandboxManagerandReferenceApi(true, "");
    }

    @Test
    public void syncSandboxManagerandReferenceApiMissingInSandboxManagerTest() {
        Collection<LinkedHashMap> sandboxesInRAPI = new ArrayList<>();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("teamId", "SND3");
        linkedHashMap.put("schemaVersion", "6");

        ((ArrayList<LinkedHashMap>) sandboxesInRAPI).add(linkedHashMap);
        ResponseEntity responseEntity2 = new ResponseEntity<Collection>(sandboxesInRAPI, HttpStatus.OK);
        when(sandboxService.findAll()).thenReturn(sandboxesIterable);
        when(sandboxService.getSystemSandboxApiURL()).thenReturn("");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Collection.class))).thenReturn(responseEntity2);
        when(sandboxService.findBySandboxId("SND2")).thenReturn(sandbox2);
        doNothing().when(sandboxService).delete(sandbox2, "", null, true);
        HashMap<String, Object> actual = new HashMap<>();
        List<String> missingInSandboxManagerIds = new ArrayList<>();
        missingInSandboxManagerIds.add("SND3");
        List<String> missingInReferenceApi = new ArrayList<>();
        missingInReferenceApi.add("SND1");
        missingInReferenceApi.add("SND2");
        actual.put("missing_in_sandbox_manager", missingInSandboxManagerIds);
        actual.put("missing_in_reference_api", missingInReferenceApi);
        HashMap<String, Object> expected = adminService.syncSandboxManagerandReferenceApi(true, "");
        assertEquals(expected, actual);
    }

    @Test
    public void syncSandboxManagerandReferenceApiSandboxJsonNotNullTest() {
        Collection<LinkedHashMap> sandboxesInRAPI = new ArrayList<>();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("teamId", "SND1");
        linkedHashMap.put("schemaVersion", "6");

        ((ArrayList<LinkedHashMap>) sandboxesInRAPI).add(linkedHashMap);
        ResponseEntity responseEntity2 = new ResponseEntity<Collection>(sandboxesInRAPI, HttpStatus.OK);
        when(sandboxService.findAll()).thenReturn(sandboxesIterable);
        when(sandboxService.getSystemSandboxApiURL()).thenReturn("");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Collection.class))).thenReturn(responseEntity2);
        when(sandboxService.findBySandboxId("SND2")).thenReturn(sandbox2);
        doNothing().when(sandboxService).delete(sandbox2, "", null, true);
        HashMap<String, Object> actual = new HashMap<>();
        List<String> missingInSandboxManagerIds = new ArrayList<>();
        List<String> missingInReferenceApi = new ArrayList<>();
        missingInReferenceApi.add("SND2");
        actual.put("missing_in_sandbox_manager", missingInSandboxManagerIds);
        actual.put("missing_in_reference_api", missingInReferenceApi);
        HashMap<String, Object> expected = adminService.syncSandboxManagerandReferenceApi(true, "");
        assertEquals(expected, actual);
    }

    @Test
    public void syncSandboxManagerandReferenceApiSandboxJsonNullTest() {
        Collection<LinkedHashMap> sandboxesInRAPI = new ArrayList<>();
        LinkedHashMap linkedHashMap = null;

        ((ArrayList<LinkedHashMap>) sandboxesInRAPI).add(linkedHashMap);
        ResponseEntity responseEntity2 = new ResponseEntity<Collection>(sandboxesInRAPI, HttpStatus.OK);
        when(sandboxService.findAll()).thenReturn(sandboxesIterable);
        when(sandboxService.getSystemSandboxApiURL()).thenReturn("");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Collection.class))).thenReturn(responseEntity2);
        when(sandboxService.findBySandboxId("SND2")).thenReturn(sandbox2);
        doNothing().when(sandboxService).delete(sandbox2, "", null, true);
        HashMap<String, Object> actual = new HashMap<>();
        List<String> missingInSandboxManagerIds = new ArrayList<>();
        List<String> missingInReferenceApi = new ArrayList<>();
        missingInReferenceApi.add("SND1");
        missingInReferenceApi.add("SND2");
        actual.put("missing_in_sandbox_manager", missingInSandboxManagerIds);
        actual.put("missing_in_reference_api", missingInReferenceApi);
        HashMap<String, Object> expected = adminService.syncSandboxManagerandReferenceApi(true, "");
        assertEquals(expected, actual);
    }

    @Test
    public void syncSandboxManagerandReferenceApiContainsSandboxIdTest() {
        Collection<LinkedHashMap> sandboxesInRAPI = new ArrayList<>();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("teamId", "SND1");
        linkedHashMap.put("schemaVersion", "6");

        ((ArrayList<LinkedHashMap>) sandboxesInRAPI).add(linkedHashMap);
        ResponseEntity responseEntity2 = new ResponseEntity<Collection>(sandboxesInRAPI, HttpStatus.OK);
        when(sandboxService.findAll()).thenReturn(sandboxesIterable);
        when(sandboxService.getSystemSandboxApiURL()).thenReturn("");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Collection.class))).thenReturn(responseEntity2);
        when(sandboxService.findBySandboxId("SND1")).thenReturn(sandbox);
        doNothing().when(sandboxService).delete(sandbox2, "", null, true);
        HashMap<String, Object> actual = new HashMap<>();
        List<String> missingInSandboxManagerIds = new ArrayList<>();
        List<String> missingInReferenceApi = new ArrayList<>();
        missingInReferenceApi.add("SND2");
        actual.put("missing_in_sandbox_manager", missingInSandboxManagerIds);
        actual.put("missing_in_reference_api", missingInReferenceApi);
        HashMap<String, Object> expected = adminService.syncSandboxManagerandReferenceApi(true, "");
        assertEquals(expected, actual);
    }

    @Test
    public void syncSandboxManagerandReferenceApiDeleteInSyncTest() {
        Collection<LinkedHashMap> sandboxesInRAPI = new ArrayList<>();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("teamId", "SND1");
        linkedHashMap.put("schemaVersion", "6");

        ((ArrayList<LinkedHashMap>) sandboxesInRAPI).add(linkedHashMap);
        ResponseEntity responseEntity2 = new ResponseEntity<Collection>(sandboxesInRAPI, HttpStatus.OK);
        when(sandboxService.findAll()).thenReturn(sandboxesIterable);
        when(sandboxService.getSystemSandboxApiURL()).thenReturn("");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Collection.class))).thenReturn(responseEntity2);
        when(sandboxService.findBySandboxId("SND1")).thenReturn(sandbox);
        doNothing().when(sandboxService).delete(sandbox, "", null, true);
        HashMap<String, Object> actual = new HashMap<>();
        List<String> missingInSandboxManagerIds = new ArrayList<>();
        List<String> missingInReferenceApi = new ArrayList<>();
        missingInReferenceApi.add("SND2");
        actual.put("missing_in_sandbox_manager", missingInSandboxManagerIds);
        actual.put("missing_in_reference_api", missingInReferenceApi);
        HashMap<String, Object> expected = adminService.syncSandboxManagerandReferenceApi(true, "");
        assertEquals(expected, actual);
    }

    @Test
    public void syncSandboxManagerandReferenceApiFixedFalseTest() {
        Collection<LinkedHashMap> sandboxesInRAPI = new ArrayList<>();
        LinkedHashMap linkedHashMap1 = new LinkedHashMap();
        linkedHashMap1.put("teamId", "SND1");
        linkedHashMap1.put("schemaVersion", "6");

        ((ArrayList<LinkedHashMap>) sandboxesInRAPI).add(linkedHashMap1);
        ResponseEntity responseEntity2 = new ResponseEntity<Collection>(sandboxesInRAPI, HttpStatus.OK);
        when(sandboxService.findAll()).thenReturn(sandboxesIterable);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Collection.class))).thenReturn(responseEntity2);
        when(sandboxService.findBySandboxId("SND2")).thenReturn(sandbox2);
        doNothing().when(sandboxService).delete(sandbox2, "", null, true);
        HashMap<String, Object> actual = new HashMap<>();
        List<String> missingInSandboxManagerIds = new ArrayList<>();
        List<String> missingInReferenceApi = new ArrayList<>();
        missingInReferenceApi.add("SND2");
        actual.put("missing_in_sandbox_manager", missingInSandboxManagerIds);
        actual.put("missing_in_reference_api", missingInReferenceApi);
        HashMap<String, Object> expected = adminService.syncSandboxManagerandReferenceApi(false, "");
        assertEquals(expected, actual);
    }

    @Test
    public void deleteUnusedSandboxesTest() {
        Date d = new Date();
        Timestamp timestamp = new Timestamp(d.getTime() - 400 * 24 * 3600 * 1000L);
        sandboxAccessHistory.setTimestamp(timestamp);
        ((ArrayList<SandboxActivityLog>) sandboxAccessHistories).add(sandboxAccessHistory);
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxAccessHistories);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        doNothing().when(sandboxService).delete(sandbox, "Token", user, false);
        Set<String> expected = new HashSet<>();
        expected.add(sandbox.getSandboxId());
        Set<String> actual = adminService.deleteUnusedSandboxes(user, "Token");
        assertEquals(expected, actual);
    }

    @Test
    public void deleteUnusedSandboxesMoreThanYearAndSandboxNullTest() {
        Date d = new Date();
        Timestamp timestamp = new Timestamp(d.getTime() - 400 * 24 * 3600 * 1000L);
        sandboxAccessHistory2.setTimestamp(timestamp);
        ((ArrayList<SandboxActivityLog>) sandboxAccessHistories).add(sandboxAccessHistory2);
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxAccessHistories);
        when(sandboxService.findBySandboxId("SND1")).thenReturn(sandbox);
        doNothing().when(sandboxService).delete(sandbox, "Token", user, false);
        Set<String> expected = new HashSet<>();
        Set<String> actual = adminService.deleteUnusedSandboxes(user, "Token");
        assertEquals(expected, actual);
    }

    @Test
    public void deleteUnusedSandboxesLessThanYearTest() {
        Date d = new Date();
        Timestamp timestamp = new Timestamp(d.getTime() - 200 * 24 * 3600 * 1000L);
        sandboxAccessHistory.setTimestamp(timestamp);
        ((ArrayList<SandboxActivityLog>) sandboxAccessHistories).add(sandboxAccessHistory);
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxAccessHistories);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        doNothing().when(sandboxService).delete(sandbox, "Token", user, false);
        Set<String> expected = new HashSet<>();
        Set<String> actual = adminService.deleteUnusedSandboxes(user, "Token");
        assertEquals(expected, actual);
    }

    @Test
    public void deleteUnusedSandboxesLessThanYearAndSandboxNullTest() {
        Date d = new Date();
        Timestamp timestamp = new Timestamp(d.getTime() - 200 * 24 * 3600 * 1000L);
        sandboxAccessHistory.setTimestamp(timestamp);
        ((ArrayList<SandboxActivityLog>) sandboxAccessHistories).add(sandboxAccessHistory);
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxAccessHistories);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(null);
        doNothing().when(sandboxService).delete(sandbox, "Token", user, false);
        Set<String> expected = new HashSet<>();
        Set<String> actual = adminService.deleteUnusedSandboxes(user, "Token");
        assertEquals(expected, actual);
    }

    @Test
    public void deleteUnusedSandboxesMoreThanOneHistoryTest() {
        Date d = new Date();
        Timestamp timestamp = new Timestamp(d.getTime() - 400 * 24 * 3600 * 1000L);
        Timestamp timestamp2 = new Timestamp(d.getTime() - 300 * 24 * 3600 * 1000L);
        sandboxAccessHistory.setTimestamp(timestamp);
        sandboxAccessHistory2.setTimestamp(timestamp2);
        ((ArrayList<SandboxActivityLog>) sandboxAccessHistories).add(sandboxAccessHistory);
        ((ArrayList<SandboxActivityLog>) sandboxAccessHistories).add(sandboxAccessHistory2);
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxAccessHistories);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        doNothing().when(sandboxService).delete(sandbox, "Token", user, false);
        Set<String> expected = new HashSet<>();
        expected.add("SND1");
        Set<String> actual = adminService.deleteUnusedSandboxes(user, "Token");
        assertEquals(expected, actual);
    }

    @Test
    public void deleteUnusedSandboxesAccessHistoryNotNullTest() {
        Date d = new Date();
        Timestamp timestamp = new Timestamp(d.getTime() - 400 * 24 * 3600 * 1000L);
        sandboxAccessHistory.setTimestamp(timestamp);
        ((ArrayList<SandboxActivityLog>) sandboxAccessHistories).add(sandboxAccessHistory);
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxAccessHistories);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        doNothing().when(sandboxService).delete(sandbox, "Token", user, false);
        Set<String> expected = new HashSet<>();
        expected.add(sandbox.getSandboxId());
        Set<String> actual = adminService.deleteUnusedSandboxes(user, "Token");
        assertEquals(expected, actual);
    }
}
