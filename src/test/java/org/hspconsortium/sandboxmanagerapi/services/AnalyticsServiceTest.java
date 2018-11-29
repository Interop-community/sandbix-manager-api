package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.controllers.UnauthorizedException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirTransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.hspconsortium.sandboxmanagerapi.services.impl.AnalyticsServiceImpl;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class AnalyticsServiceTest {
    MockHttpServletRequest request = new MockHttpServletRequest();
    private FhirTransactionRepository fhirTransactionRepository = mock(FhirTransactionRepository.class);
    private AnalyticsServiceImpl analyticsService = new AnalyticsServiceImpl(fhirTransactionRepository);
    private UserService userService = mock(UserService.class);
    private SandboxService sandboxService = mock(SandboxService.class);
    private AppService appService = mock(AppService.class);
    private RuleService ruleService = mock(RuleService.class);
    private SandboxActivityLogService sandboxActivityLogService = mock(SandboxActivityLogService.class);
    private RestTemplate restTemplate = mock(RestTemplate.class);

    private User user;
    private User user2;
    private UserRole userRole1;
    private UserRole userRole2;
    private UserRole userRole3;
    private List<UserRole> userRoles;
    private Sandbox sandbox;
//    private Sandbox sandbox2;
    private HashMap<String, Integer> sandboxApps;
    private List<Sandbox> sandboxes;
    private List<App> appList;
    private App app1;
    private App app2;
    private HashMap<String, String> transactionInfo;
    private List<String> schemaNames;
    private ResponseEntity<HashMap> responseEntity;
    private Iterable<SandboxActivityLog> sandboxActivityLogs;
    private List<SandboxActivityLog> sandboxActivityLogList;
    private SandboxActivityLog sandboxActivityLog;
    private SandboxActivityLog sandboxActivityLog2;
    private List<FhirTransaction> fhirTransactionList;


    @Before
    public void setup() {
        analyticsService.setUserService(userService);
        analyticsService.setSandboxService(sandboxService);
        analyticsService.setAppService(appService);
        analyticsService.setRuleService(ruleService);
        analyticsService.setSandboxActivityLogService(sandboxActivityLogService);
        analyticsService.setRestTemplate(restTemplate);

        user = new User();
        user.setSbmUserId("userID");
        user.setId(1);
        user.setEmail("Kay@interopion.com");

        user2 = new User();
        user2.setSbmUserId("userID-2");
        user2.setId(2);
        user2.setEmail("Bray@interopion.com");

        app1 = new App();
        app2 = new App();
        appList = new ArrayList<>();
        appList.add(app1);
        appList.add(app2);

//        DataSet dataSetApp;
//        dataSetApp.

        sandbox = new Sandbox();
        sandbox.setSandboxId("1");
        sandbox.setCreatedBy(user);
        sandbox.setId(1);

//        sandbox2 = new Sandbox();
//        sandbox2.setSandboxId("2");
//        sandbox2.setCreatedBy(user);
//        sandbox2.setId(2);
//        sandbox.setApps(appList);

        userRole1 = new UserRole();
        userRole1.setUser(user);
        userRole2 = new UserRole();
        userRole2.setUser(user);
        userRole3 = new UserRole();
        userRole3.setUser(user);

        userRoles = new ArrayList<>();

        userRole1.setRole(Role.ADMIN);
        userRoles.add(userRole1);
        sandbox.setUserRoles(userRoles);
//        sandbox2.setUserRoles(userRoles);

        userRole2.setRole(Role.MANAGE_USERS);
        userRoles.add(userRole2);
        sandbox.setUserRoles(userRoles);
//        sandbox2.setUserRoles(userRoles);

        userRole3.setRole(Role.USER);
        userRoles.add(userRole3);
        sandbox.setUserRoles(userRoles);
//        sandbox2.setUserRoles(userRoles);

//        sandbox.setUserRoles(userRoles);
//        sandbox2.setUserRoles(userRoles);

        sandboxes = new ArrayList<>();
        sandboxes.add(sandbox);
//        sandboxes.add(sandbox2);
        user.setSandboxes(sandboxes);

        sandboxApps = new HashMap<>();
        sandboxApps.put("A",1);
        sandboxApps.put("B",2);
        sandboxApps.put("C",3);

        transactionInfo = new HashMap<>();
        transactionInfo.put("tenant", sandbox.getSandboxId());
        transactionInfo.put("secured", "true");
        transactionInfo.put("userId", user.getSbmUserId());
        transactionInfo.put("method", "A");
        transactionInfo.put("url", "http://abc.com");
        transactionInfo.put("resource", "Practitioner");
        transactionInfo.put("domain", "abcd");
        transactionInfo.put("ip_address", "111.111.111");
        transactionInfo.put("response_code", "1");

        schemaNames = new ArrayList<>();
        schemaNames.add("1");
        schemaNames.add("2");

        Date d = new Date();
        Timestamp timestamp = new Timestamp(d.getTime());

        sandboxActivityLog = new SandboxActivityLog();
        sandboxActivityLog.setId(1);
        sandboxActivityLog.setTimestamp(timestamp);
        sandboxActivityLog.setUser(user);
        sandboxActivityLog.setActivity(SandboxActivity.CREATED);
        sandboxActivityLog.setSandbox(sandbox);

        sandboxActivityLog2 = new SandboxActivityLog();
        sandboxActivityLog2.setId(2);
        sandboxActivityLog2.setTimestamp(timestamp);
        sandboxActivityLog2.setUser(user2);
        sandboxActivityLog2.setActivity(SandboxActivity.LOGGED_IN);
        sandboxActivityLog2.setSandbox(sandbox);

        sandboxActivityLogs = new ArrayList<>();
        ((ArrayList<SandboxActivityLog>) sandboxActivityLogs).add(sandboxActivityLog);
        ((ArrayList<SandboxActivityLog>) sandboxActivityLogs).add(sandboxActivityLog2);

        sandboxActivityLogList = new ArrayList<>();
        sandboxActivityLogList.add(sandboxActivityLog);
        sandboxActivityLogList.add(sandboxActivityLog2);

        fhirTransactionList = new ArrayList<>();
        FhirTransaction ft = new FhirTransaction();
        ft.setTransactionTimestamp(timestamp);
        fhirTransactionList.add(ft);

    }

//    @Test
//    public void countSandboxesByUserTest(){
//        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
//        when(analyticsService.countSandboxesByUser(user.getSbmUserId())).thenReturn(1);
//    }

    @Test
    public void countAppsPerSandboxByUserSizeTest(){
        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxes);
        for(Sandbox sandbox: sandboxes) {
            when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(appList);
        }
        assertEquals(   1, analyticsService.countAppsPerSandboxByUser(user).size());
    }

    @Test
    public void countAppsPerSandboxByUserKeyValueTest(){
        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxes);
        for(Sandbox sandbox: sandboxes) {
            when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(appList);
        }
        Integer numApps = analyticsService.countAppsPerSandboxByUser(user).get("1");
        Integer n = 2;
        assertEquals(n, numApps);
    }

    @Test
    public void countUsersPerSandboxByUserTest() {
        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxes);
        final Map<String, Integer> actual = analyticsService.countUsersPerSandboxByUser(user);
        final Map<String, Integer> expected = new HashMap<String, Integer>() {
            {
                put("1", 1);
            }
        };
        assertEquals(expected, actual);
    }

    @Test
    public void handleFhirTransactionTest() {
        when(sandboxService.findBySandboxId("1")).thenReturn(sandbox);
        when(ruleService.checkIfUserCanPerformTransaction(sandbox,transactionInfo.get("method").toString(), "")).thenReturn(true);
        analyticsService.handleFhirTransaction(user, transactionInfo, "");
    }

    @Test(expected = UnauthorizedException.class)
    public void handleFhirTransactionRuleServiceFalseTest() {
        when(sandboxService.findBySandboxId("1")).thenReturn(sandbox);
        when(ruleService.checkIfUserCanPerformTransaction(sandbox,transactionInfo.get("method").toString(), "")).thenReturn(false);
        analyticsService.handleFhirTransaction(user, transactionInfo, "");
    }

    @Test
    public void handleFhirTransactionRuleServiceTrueTest() {
        when(sandboxService.findBySandboxId("1")).thenReturn(sandbox);
        when(ruleService.checkIfUserCanPerformTransaction(sandbox,transactionInfo.get("method").toString(), "")).thenReturn(true);
        analyticsService.handleFhirTransaction(user, transactionInfo, "");
    }

    @Test
    public void handleFhirTransactionUserNullTest() {
        when(sandboxService.findBySandboxId("1")).thenReturn(sandbox);
        when(ruleService.checkIfUserCanPerformTransaction(sandbox,transactionInfo.get("method").toString(), "")).thenReturn(true);
        analyticsService.handleFhirTransaction(null, transactionInfo, "");
    }

    @Test
    public void countTransactionsByPayerTest() {
        when(fhirTransactionRepository.findByPayerUserId(1)).thenReturn(fhirTransactionList);
        Integer n = analyticsService.countTransactionsByPayer(user);
        Integer n2 = new Integer(1);
        assertEquals(n, n2);
    }

//    @Test
//    public void retrieveTotalMemoryByUserTest() {
//
//        HashMap<String, Double> sandboxMemorySizes = new HashMap<>();
//        sandboxMemorySizes.put("1", 1.5);
//        sandboxMemorySizes.put("2", 3.5);
//        responseEntity = new ResponseEntity<HashMap>(sandboxMemorySizes, HttpStatus.OK);
//        HttpHeaders requestHeaders = new HttpHeaders();
//        requestHeaders.set("Authorization", "Bearer " + request);
//        HttpEntity<List<String>> httpEntity = new HttpEntity(schemaNames, requestHeaders);
//        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxes);
//        when(sandboxService.getSystemSandboxApiURL()).thenReturn("");
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(HashMap.class))).thenReturn(responseEntity);
//        Double totalMemory = analyticsService.retrieveTotalMemoryByUser(user, "");
//        Double n = new Double(5.0);
//        assertEquals(totalMemory, n);
//
//    }

    @Test
    public void retrieveMemoryInSchemasTest() {
        //TODO: same problem as above
    }

    @Test
    public void activeUserCountTest() {
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxActivityLogs);
        String n = analyticsService.activeUserCount(1);
        assertEquals("2", n);
    }

    @Test
    public void getSandboxStatisticsTest() {
        Date d = new Date();
        int intDays = Integer.parseInt("5");
        Date dateBefore = new Date(d.getTime() - intDays * 24 * 3600 * 1000L );
        Timestamp timestamp = new Timestamp(dateBefore.getTime());

        when(sandboxService.fullCount()).thenReturn("1");
        when(sandboxService.schemaCount("1")).thenReturn("1");
        when(sandboxService.schemaCount("2")).thenReturn("2");
        when(sandboxService.schemaCount("5")).thenReturn("5");
        when(sandboxService.schemaCount("3")).thenReturn("3");
        when(sandboxService.schemaCount("4")).thenReturn("4");
        when(sandboxService.schemaCount("6")).thenReturn("6");
        when(sandboxService.schemaCount("7")).thenReturn("7");
        when(sandboxService.intervalCount(timestamp)).thenReturn("1");
        when(userService.fullCount()).thenReturn("1");
        when(userService.intervalCount(timestamp)).thenReturn("1");
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxActivityLogs);
        String actual = analyticsService.getSandboxStatistics("5");
        String expected = "";
       assertEquals(expected, actual);
    }

    @Test
    public void transactionStatsTest() {
        HashMap<String, Object> expected = new HashMap<>();
        HashMap<String, Double> a = new HashMap<>();
        a.put("1", 1.0);
        expected.put("top_values", a);
        expected.put("median", 1.0);
        expected.put("mean", 1.0);
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxActivityLogs);
        when(sandboxService.findBySandboxId(sandbox.getId().toString())).thenReturn(sandbox);
        when(fhirTransactionRepository.findBySandboxId(sandbox.getId())).thenReturn(fhirTransactionList);
        HashMap<String, Object> actual = analyticsService.transactionStats(1, 2);
        assertEquals(expected, actual);
    }

    @Test
    public void sandboxMemoryStatsTest() {
        when(sandboxService.findBySandboxId(sandbox.getId().toString())).thenReturn(sandbox);
        //TODO: RestTemplate issue, same as above
    }

    @Test
    public void usersPerSandboxStatsTest() {
        Iterable<Sandbox> sndIterable = new ArrayList<>();
        ((ArrayList<Sandbox>) sndIterable).add(sandbox);
        HashMap<String, Object> expected = new HashMap<>();
        HashMap<String, Double> a = new HashMap<>();
        a.put("1", 1.0);
        expected.put("top_values", a);
        expected.put("median", 1.0);
        expected.put("mean", 1.0);
        when(sandboxService.findAll()).thenReturn(sndIterable);
        HashMap<String, Object> actual = analyticsService.usersPerSandboxStats(1,1);
        assertEquals(expected, actual);
    }

    @Test
    public void sandboxesPerUserStatsTest(){
        Iterable<User> userIterable = new ArrayList<>();
        ((ArrayList<User>) userIterable).add(user);
        HashMap<String, Object> expected = new HashMap<>();
        HashMap<String, Double> a = new HashMap<>();
        a.put("Kay@interopion.com", 1.0);
        expected.put("top_values", a);
        expected.put("median", 1.0);
        expected.put("mean", 1.0);
        when(userService.findAll()).thenReturn(userIterable);
        HashMap<String, Object> actual = analyticsService.sandboxesPerUserStats(1,1);
        assertEquals(expected, actual);
    }

}
