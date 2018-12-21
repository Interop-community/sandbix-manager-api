package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.controllers.UnauthorizedException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirTransactionRepository;
import org.hspconsortium.sandboxmanagerapi.repositories.StatisticsRepository;
import org.junit.Before;
import org.junit.Test;
import org.hspconsortium.sandboxmanagerapi.services.impl.AnalyticsServiceImpl;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class AnalyticsServiceTest {
    MockHttpServletRequest request = new MockHttpServletRequest();
    private FhirTransactionRepository fhirTransactionRepository = mock(FhirTransactionRepository.class);
    private StatisticsRepository statisticsRepository = mock(StatisticsRepository.class);
    private AnalyticsServiceImpl analyticsService = new AnalyticsServiceImpl(fhirTransactionRepository, statisticsRepository);
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
    private Sandbox sandbox2;
    private Sandbox sandbox3;
    private HashMap<String, Integer> sandboxApps;
    private List<Sandbox> sandboxes;
    private List<App> appList;
    private App app1;
    private App app2;
    private HashMap<String, String> transactionInfo;
    private List<String> schemaNames;
    private ResponseEntity<HashMap> responseEntity;
    private Iterable<SandboxActivityLog> sandboxActivityLogIterable;
    private Iterable<SandboxActivityLog> sandboxActivityLogIterable2;
    private SandboxActivityLog sandboxActivityLog;
    private SandboxActivityLog sandboxActivityLog2;
    private SandboxActivityLog sandboxActivityLog3;
    private SandboxActivityLog sandboxActivityLog4;
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

        sandbox = new Sandbox();
        sandbox.setSandboxId("1");
        sandbox.setCreatedBy(user);
        sandbox.setId(1);
        sandbox.setApiEndpointIndex("5");

        sandbox2 = new Sandbox();
        sandbox2.setSandboxId("2");
        sandbox2.setCreatedBy(user2);
        sandbox2.setId(2);
        sandbox2.setApiEndpointIndex("6");

        sandbox3 = new Sandbox();
        sandbox3.setSandboxId("2");
        sandbox3.setCreatedBy(user2);
        sandbox3.setId(2);
        sandbox3.setApiEndpointIndex("7");

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
        Timestamp timestamp = new Timestamp(d.getTime() - 40 * 24 * 3600 * 1000L);
        Timestamp timestamp2 = new Timestamp(d.getTime() - 30 * 24 * 3600 * 1000L);
        Timestamp timestamp3 = new Timestamp(d.getTime());
        Timestamp timestamp4 = new Timestamp(d.getTime() - 1 * 24 * 3600 * 1000L);

        sandboxActivityLog = new SandboxActivityLog();
        sandboxActivityLog.setId(1);
        sandboxActivityLog.setTimestamp(timestamp);
        sandboxActivityLog.setUser(user);
        sandboxActivityLog.setActivity(SandboxActivity.CREATED);
        sandboxActivityLog.setSandbox(sandbox);

        sandboxActivityLog2 = new SandboxActivityLog();
        sandboxActivityLog2.setId(2);
        sandboxActivityLog2.setTimestamp(timestamp2);
        sandboxActivityLog2.setUser(user);
        sandboxActivityLog2.setActivity(SandboxActivity.LOGGED_IN);
        sandboxActivityLog2.setSandbox(sandbox);

        sandboxActivityLog3 = new SandboxActivityLog();
        sandboxActivityLog3.setId(3);
        sandboxActivityLog3.setTimestamp(timestamp3);
        sandboxActivityLog3.setUser(user2);
        sandboxActivityLog3.setActivity(SandboxActivity.CREATED);
        sandboxActivityLog3.setSandbox(sandbox2);

        sandboxActivityLog4 = new SandboxActivityLog();
        sandboxActivityLog4.setId(4);
        sandboxActivityLog4.setTimestamp(timestamp4);
        sandboxActivityLog4.setUser(user2);
        sandboxActivityLog4.setActivity(SandboxActivity.LOGGED_IN);
        sandboxActivityLog4.setSandbox(sandbox3);

        sandboxActivityLogIterable = new ArrayList<>();
        ((ArrayList<SandboxActivityLog>) sandboxActivityLogIterable).add(sandboxActivityLog);
        ((ArrayList<SandboxActivityLog>) sandboxActivityLogIterable).add(sandboxActivityLog2);

        sandboxActivityLogIterable2 = new ArrayList<>();
        ((ArrayList<SandboxActivityLog>) sandboxActivityLogIterable2).add(sandboxActivityLog);
        ((ArrayList<SandboxActivityLog>) sandboxActivityLogIterable2).add(sandboxActivityLog2);
        ((ArrayList<SandboxActivityLog>) sandboxActivityLogIterable2).add(sandboxActivityLog3);
        ((ArrayList<SandboxActivityLog>) sandboxActivityLogIterable2).add(sandboxActivityLog4);

        fhirTransactionList = new ArrayList<>();
        FhirTransaction ft = new FhirTransaction();
        FhirTransaction ft2 = new FhirTransaction();
        ft.setTransactionTimestamp(timestamp3);
        ft.setSandboxId(1);
        ft2.setTransactionTimestamp(timestamp4);
        ft2.setSandboxId(2);
        fhirTransactionList.add(ft);
        fhirTransactionList.add(ft2);

    }

    @Test
    public void countSandboxesByUserTest(){
        when(userService.findBySbmUserId(user.getSbmUserId())).thenReturn(user);
        Integer expected = 1;
        Integer actual = analyticsService.countSandboxesByUser(user.getSbmUserId());
    }

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
        Integer actual = analyticsService.countTransactionsByPayer(user);
        Integer expected = new Integer(2);
        assertEquals(actual, expected);
    }

    @Test
    public void retrieveTotalMemoryByUserTest() {
        HashMap<String, Double> sandboxMemorySizes = new HashMap<>();
        sandboxMemorySizes.put("1", 1.5);
        sandboxMemorySizes.put("2", 3.5);
        responseEntity = new ResponseEntity<HashMap>(sandboxMemorySizes, HttpStatus.OK);
        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxes);
        when(sandboxService.getSystemSandboxApiURL()).thenReturn("");
        when(restTemplate.exchange(anyString(), any(), any(), eq(HashMap.class))).thenReturn(responseEntity);
        Double totalMemory = analyticsService.retrieveTotalMemoryByUser(user, "");
        Double n = new Double(5.0);
        assertEquals(totalMemory, n);
    }

    @Test
    public void retrieveMemoryInSchemasTest() {
        HashMap<String, Double> sandboxMemorySizes = new HashMap<>();
        sandboxMemorySizes.put("1", 1.5);
        sandboxMemorySizes.put("2", 3.5);
        responseEntity = new ResponseEntity<HashMap>(sandboxMemorySizes, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(HashMap.class))).thenReturn(responseEntity);
        Double totalMemory = analyticsService.retrieveMemoryInSchemas(schemaNames, "");
        Double n = new Double(5.0);
        assertEquals(totalMemory, n);

    }

    @Test
    public void getSandboxStatisticsTest() {
        when(sandboxService.fullCount()).thenReturn("1");
        when(sandboxService.schemaCount("1")).thenReturn("0");
        when(sandboxService.schemaCount("2")).thenReturn("0");
        when(sandboxService.schemaCount("5")).thenReturn("0");
        when(sandboxService.schemaCount("3")).thenReturn("0");
        when(sandboxService.schemaCount("4")).thenReturn("0");
        when(sandboxService.schemaCount("6")).thenReturn("1");
        when(sandboxService.schemaCount("7")).thenReturn("0");
        when(sandboxService.intervalCount(any())).thenReturn("1");
        when(userService.fullCount()).thenReturn("1");
        when(userService.intervalCount(any())).thenReturn("1");
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxActivityLogIterable);
        analyticsService.getSandboxStatistics("1");
        verify(sandboxService).fullCount();
        verify(sandboxService).schemaCount("1");
        verify(sandboxService).intervalCount(any());
        verify(userService).fullCount();
        verify(userService).intervalCount(any());
        verify(sandboxService).newDSTU2SandboxesInIntervalCount(any());
        verify(sandboxService).newSTU3SandboxesInIntervalCount(any());
        verify(sandboxService).newR4SandboxesInIntervalCount(any());
        verify(statisticsRepository).getFhirTransaction(any(), any());
    }

    @Test
    public void snapshotStatisticsTest() {
        analyticsService.snapshotStatistics();
        // TODO: finish test
    }

    @Test
    public void displayStatsForGivenNumberOfMonthsTest() {
        analyticsService.displayStatsForGivenNumberOfMonths("1");
        verify(statisticsRepository).get12MonthStatistics(any(), any());
    }

    @Test
    public void transactionStatsTest() {
        //Can't use everyline in the code, would need to return two sandboxes, which can't be mocked
        HashMap<String, Object> expected = new HashMap<>();
        HashMap<String, Double> a = new HashMap<>();
        a.put("1", 2.0);
        expected.put("top_values", a);
        expected.put("median", 2.0);
        expected.put("mean", 2.0);
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxActivityLogIterable);
        when(sandboxService.findBySandboxId(sandbox.getId().toString())).thenReturn(sandbox);
        when(fhirTransactionRepository.findBySandboxId(sandbox.getId())).thenReturn(fhirTransactionList);
        HashMap<String, Object> actual = analyticsService.transactionStats(45, 5);
        assertEquals(expected, actual);
    }

    @Test
    public void sandboxMemoryStatsTest() {
        when(sandboxActivityLogService.findAll()).thenReturn(sandboxActivityLogIterable);
        when(sandboxService.findBySandboxId(sandbox.getId().toString())).thenReturn(sandbox);
        HashMap<String, Double> sandboxMemorySizes = new HashMap<>();
        sandboxMemorySizes.put("1", 1.5);
        sandboxMemorySizes.put("2", 3.5);
        responseEntity = new ResponseEntity<HashMap>(sandboxMemorySizes, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(HashMap.class))).thenReturn(responseEntity);
        HashMap<String, Object> actual = analyticsService.sandboxMemoryStats(1, 1, "");
        HashMap<String, Object> expected = new HashMap<>();
        HashMap<String, Double> a = new HashMap<>();
        a.put("2", 3.5);
        expected.put("top_values", a);
        expected.put("median", 2.5);
        expected.put("mean", 2.5);
        assertEquals(expected, actual);
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
        assertEquals(expected.get("median"), actual.get("median"));
        assertEquals(expected.get("mean"), actual.get("mean"));
        assertEquals(expected.get("top_values"), actual.get("top_values"));
    }
}
