package org.hspconsortium.sandboxmanagerapi.services.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hspconsortium.sandboxmanagerapi.controllers.UnauthorizedException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirTransactionRepository;
import org.hspconsortium.sandboxmanagerapi.repositories.StatisticsRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    private static Logger LOGGER = LoggerFactory.getLogger(AnalyticsServiceImpl.class.getName());

    @Value("${spring.datasource.base_url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUserName;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    private UserService userService;
    private SandboxService sandboxService;
    private FhirTransactionRepository fhirTransactionRepository;
    private StatisticsRepository statisticsRepository;

    private AppService appService;
    private RuleService ruleService;
    private SandboxActivityLogService sandboxActivityLogService;

    private RestTemplate simpleRestTemplate;

    private Set<Integer> DSTU2SanboxesInInterval = new HashSet<>();
    private Set<Integer> STU3SandboxesInInterval = new HashSet<>();
    private Set<Integer> R4SandboxesInInterval = new HashSet<>();
    private Set<Integer> activeSandboxInInterval = new HashSet<>();
    private Set<Integer> activeUserInInterval = new HashSet<>();
    private Set<Integer> fullSanboxCount = new HashSet<>();
    private Set<Integer> fullUserCount = new HashSet<>();
    private Set<Integer> fullDSTU2Count = new HashSet<>();
    private Set<Integer> fullSTU3Count = new HashSet<>();
    private Set<Integer> fullR4Count = new HashSet<>();
    private Integer countNewSandbox = 0;
    private Integer countNewUser = 0;
    private Integer sandboxId = 0;
    private Integer userId = 0;
    private String apiEndpointIndex = "";
    private Iterable<SandboxActivityLog> sandboxActivityLogs;
    private List<SandboxActivityLog> sandboxActivityLogListIntervalFilter;

    @Inject
    public AnalyticsServiceImpl(final FhirTransactionRepository fhirTransactionRepository,final StatisticsRepository statisticsRepository) {
        this.fhirTransactionRepository = fhirTransactionRepository;
        this.statisticsRepository = statisticsRepository;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Inject
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Inject
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Inject
    public void setSandboxActivityLogService(SandboxActivityLogService sandboxActivityLogService) {
        this.sandboxActivityLogService = sandboxActivityLogService;
    }

    @Inject
    public void setRestTemplate(RestTemplate simpleRestTemplate) {
        this.simpleRestTemplate = simpleRestTemplate;
    }

    public Integer countSandboxesByUser(String userId) {
        User user = userService.findBySbmUserId(userId);
        return 1;
    }

    public HashMap<String, Integer> countAppsPerSandboxByUser(User user) {
        HashMap<String, Integer> sandboxApps = new HashMap<>();
        List<Sandbox> userCreatedSandboxes = sandboxService.findByPayerId(user.getId());
        for (Sandbox sandbox: userCreatedSandboxes) {
            String sandboxId = sandbox.getSandboxId();
            sandboxApps.put(sandboxId, appService.findBySandboxId(sandboxId).size());
        }
        return sandboxApps;
    }

    public HashMap<String, Integer> countUsersPerSandboxByUser(User user) {
        HashMap<String, Integer> sandboxUsers = new HashMap<>();
        List<Sandbox> userCreatedSandboxes = sandboxService.findByPayerId(user.getId());
        for (Sandbox sandbox: userCreatedSandboxes) {
            List<UserRole> usersRoles = sandbox.getUserRoles();
            Map<String, UserRole> uniqueUsers = new LinkedHashMap<>();
            for (UserRole userRole : usersRoles) {
                uniqueUsers.put(userRole.getUser().getEmail(), userRole);
            }
            sandboxUsers.put(sandbox.getSandboxId(), uniqueUsers.size());
        }
        return sandboxUsers;
    }

    public FhirTransaction handleFhirTransaction(User user, HashMap transactionInfo, String bearerToken) {
        Sandbox sandbox = sandboxService.findBySandboxId(transactionInfo.get("tenant").toString());
        if (!ruleService.checkIfUserCanPerformTransaction(sandbox, transactionInfo.get("method").toString(), bearerToken)) {
            throw new UnauthorizedException("User has ran out of either transaction counts or storage. Cannot complete transaction.");
        }
        FhirTransaction fhirTransaction = new FhirTransaction();
        if (user != null) {
            fhirTransaction.setPerformedById(user.getId());
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        fhirTransaction.setTransactionTimestamp(timestamp);
        fhirTransaction.setSandboxId(sandbox.getId());
        fhirTransaction.setUrl(transactionInfo.get("url").toString());
        fhirTransaction.setFhirResource(transactionInfo.get("resource").toString());
        if (fhirTransaction.getFhirResource().equals("Practitioner")) {

        }
        fhirTransaction.setMethod(transactionInfo.get("method").toString());
        fhirTransaction.setDomain(transactionInfo.get("domain").toString());
        fhirTransaction.setIpAddress(transactionInfo.get("ip_address").toString());
        fhirTransaction.setResponseCode(Integer.parseInt(transactionInfo.get("response_code").toString()));
        fhirTransaction.setSecured(Boolean.parseBoolean(transactionInfo.get("secured").toString()));
        fhirTransaction.setPayerUserId(sandbox.getPayerUserId());
        return fhirTransactionRepository.save(fhirTransaction);
    }

    public Integer countTransactionsByPayer(User payer) {
        List<FhirTransaction> fhirTransactions = fhirTransactionRepository.findByPayerUserId(payer.getId());
        return fhirTransactions.size();
    }

    public Double retrieveTotalMemoryByUser(User user, String request) {
        List<Sandbox> userCreatedSandboxes = sandboxService.findByPayerId(user.getId());
        List<String> sandboxIds = new ArrayList<>();
        for (Sandbox sandbox: userCreatedSandboxes) {
            sandboxIds.add(sandbox.getSandboxId());
        }
        return retrieveMemoryInSchemas(sandboxIds, request);
    }

    public Double retrieveMemoryInSchemas(List<String> schemaNames, String request) {
        HashMap<String, Double> sandboxMemorySizes;
        Double count = 0.0;

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer " + request);
        HttpEntity<List<String>> httpEntity = new HttpEntity(schemaNames, requestHeaders);
        sandboxMemorySizes = simpleRestTemplate.exchange(sandboxService.getSystemSandboxApiURL() + "/memory/user", HttpMethod.PUT, httpEntity, HashMap.class).getBody();
        for (Double memory: new ArrayList<>(sandboxMemorySizes.values())) {
            count += memory;
        }
        return count;
    }

    private void sandboxActivityLogListIntervalFilter(Timestamp timestamp, Integer intervalDays) {
        sandboxActivityLogs= sandboxActivityLogService.findAll();
        for (SandboxActivityLog sandboxActivityLog: sandboxActivityLogs) {
            if (sandboxActivityLog.getSandbox() != null && sandboxActivityLog.getTimestamp().before(timestamp)) {
                fullSanboxCount.add(sandboxActivityLog.getSandbox().getId());
            }
        }
        List<SandboxActivityLog> sandboxActivityLogList = new ArrayList<>();
        sandboxActivityLogs.forEach(sandboxActivityLogList::add);
        sandboxActivityLogListIntervalFilter = sandboxActivityLogList.stream().filter(x -> new Date().getTime() - x.getTimestamp().getTime() < TimeUnit.DAYS.toMillis(intervalDays)).collect(Collectors.toList());
    }

    private String activeUserCount() {
        HashSet<Object> seen = new HashSet<>();
        List<SandboxActivityLog> activeInInterval = new ArrayList<>(sandboxActivityLogListIntervalFilter);
        activeInInterval.removeIf(e->!seen.add(e.getUser().getId()));
        return Integer.toString(seen.size());
    }

    private String activeSandboxCount() {
        List<SandboxActivityLog> activeInInterval = new ArrayList<>(sandboxActivityLogListIntervalFilter);
        HashSet<Object> seenSandboxes = new HashSet<>();
        activeInInterval.removeIf(e->e.getSandbox() != null && !seenSandboxes.add(e.getSandbox().getId()));
        return Integer.toString(seenSandboxes.size());
    }

    private String dstu2SandboxesInInterval() {
        Set<Integer> dstu2Sandbox = new HashSet<>();
        for (SandboxActivityLog sandboxActivityLog: sandboxActivityLogListIntervalFilter) {
            if (sandboxActivityLog.getSandbox() != null && sandboxActivityLog.getSandbox().getApiEndpointIndex().equals("5")) {
                dstu2Sandbox.add(sandboxActivityLog.getSandbox().getId());
            }
        }
        return Integer.toString(dstu2Sandbox.size());
    }

    private String stu3SandboxesInInterval() {
        Set<Integer> stu3Sandbox = new HashSet<>();
        for (SandboxActivityLog sandboxActivityLog: sandboxActivityLogListIntervalFilter) {
            if (sandboxActivityLog.getSandbox() != null && sandboxActivityLog.getSandbox().getApiEndpointIndex().equals("6")) {
                stu3Sandbox.add(sandboxActivityLog.getSandbox().getId());
            }
        }
        return Integer.toString(stu3Sandbox.size());
    }

    private String r4SandboxesInInterval() {
        Set<Integer> r4Sandbox = new HashSet<>();
        for (SandboxActivityLog sandboxActivityLog: sandboxActivityLogListIntervalFilter) {
            if (sandboxActivityLog.getSandbox() != null && sandboxActivityLog.getSandbox().getApiEndpointIndex().equals("7")) {
                r4Sandbox.add(sandboxActivityLog.getSandbox().getId());
            }
        }
        return Integer.toString(r4Sandbox.size());
    }

    @Override
    public Statistics getSandboxStatistics(final String intervalDays) {

        int intDays = Integer.parseInt(intervalDays);
        Date d = new Date();
        Date dateBefore = new Date(d.getTime() - intDays * 24 * 3600 * 1000L );
        Timestamp timestamp = new Timestamp(dateBefore.getTime());

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        sandboxActivityLogListIntervalFilter(timestamp, intDays);

        Statistics statistics = new Statistics();
        statistics.setFullSandboxCount(sandboxService.fullCount());
        int apiEndpoint1 = Integer.parseInt(sandboxService.schemaCount("1"));
        int apiEndpoint2 = Integer.parseInt(sandboxService.schemaCount("2"));
        int apiEndpoint5 = Integer.parseInt(sandboxService.schemaCount("5"));
        int twoTotal = apiEndpoint1 + apiEndpoint2 + apiEndpoint5;
        statistics.setFullDstu2Count(Integer.toString(twoTotal));
        int apiEndpoint3 = Integer.parseInt(sandboxService.schemaCount("3"));
        int apiEndpoint4 = Integer.parseInt(sandboxService.schemaCount("4"));
        int apiEndpoint6 = Integer.parseInt(sandboxService.schemaCount("6"));
        int fourTotal = apiEndpoint3 + apiEndpoint4 + apiEndpoint6;
        statistics.setFullStu3Count(Integer.toString(fourTotal));
        statistics.setNewSandboxesInInterval(sandboxService.intervalCount(timestamp));
        int apiEndpoint7 = Integer.parseInt(sandboxService.schemaCount("7"));
        statistics.setFullR4Count(Integer.toString(apiEndpoint7));
        statistics.setActiveSandboxesInInterval(activeSandboxCount());
        statistics.setDstu2SandboxesInInterval(dstu2SandboxesInInterval());
        statistics.setStu3SandboxesInInterval(stu3SandboxesInInterval());
        statistics.setR4SandboxesInInterval(r4SandboxesInInterval());
        statistics.setFullUserCount(userService.fullCount());
        statistics.setNewUsersInInterval(userService.intervalCount(timestamp));
        statistics.setActiveUserInInterval(activeUserCount());
        statistics.setCreatedTimestamp(timestamp);
        statistics.setFhirTransactions(Integer.toString(statisticsRepository.getFhirTransaction(timestamp, currentTimestamp)));

        return statistics;
    }

    private static String toJson(Statistics statistics) {
        Gson gson = new Gson();
        Type type = new TypeToken<Statistics>() {
        }.getType();
        return gson.toJson(statistics, type);
    }
    //(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]")
    @Scheduled(cron = "0 50 23 28-31 * ?")
    public void snapshotStatistics() {
        final Calendar c = Calendar.getInstance();
        if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {
            int intDays = LocalDate.now().lengthOfMonth();
            getSandboxStatistics(Integer.toString(intDays));
            //LOGGER.info("Creating stats");
        }
    }

    public Statistics getSandboxAndUserStatsForLastTwoYears() {
        fullSanboxCount.clear();
        fullUserCount.clear();
        fullDSTU2Count.clear();
        fullSTU3Count.clear();
        fullR4Count.clear();
        countNewSandbox = 0;
        countNewUser = 0;
        DSTU2SanboxesInInterval.clear();
        STU3SandboxesInInterval.clear();
        R4SandboxesInInterval.clear();
        activeSandboxInInterval.clear();
        activeUserInInterval.clear();

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        int targetMonth = currentTimestamp.getMonth();
        int targetYear = currentTimestamp.getYear() - 2;
        sandboxActivityLogs = sandboxActivityLogService.findAll();
        Statistics statisticsXX = new Statistics();
        for(SandboxActivityLog sandboxActivityLog: sandboxActivityLogs) {
            int monthActivityLog = sandboxActivityLog.getTimestamp().getMonth();
            int yearActivityLog = sandboxActivityLog.getTimestamp().getYear();

            if (sandboxActivityLog.getSandbox() == null) {
                continue;
            } else {
                sandboxId = sandboxActivityLog.getSandbox().getId();
            }
            if (sandboxActivityLog.getUser() == null) {
                continue;
            } else {
                userId = sandboxActivityLog.getUser().getId();
            }
            apiEndpointIndex = sandboxActivityLog.getSandbox().getApiEndpointIndex();
            if(yearActivityLog < targetYear || (yearActivityLog == targetYear && monthActivityLog < targetMonth)) {
                fullSanboxCount.add(sandboxId);
                fullUserCount.add(userId);
                if (apiEndpointIndex.equals("5")) {
                    fullDSTU2Count.add(sandboxId);
                }
                if (apiEndpointIndex.equals("6")) {
                    fullSTU3Count.add(sandboxId);
                }
                if (apiEndpointIndex.equals("7")) {
                    fullR4Count.add(sandboxId);
                }
            } else if (monthActivityLog == targetMonth && yearActivityLog == targetYear) {
                addToDifferentSandboxStats();
            } else if (fullSanboxCount.isEmpty()) {
                continue;
            }
            else {
                String timestampYear = Integer.toString(targetYear).substring(1);
                String timestampMonth = Integer.toString(targetMonth + 1);
                if (timestampMonth.length() == 1) {
                    timestampMonth = "0" + timestampMonth;
                }
                String strYear = "20" + timestampYear;
                int intYear = Integer.parseInt(strYear);
                int intMonth = Integer.parseInt(timestampMonth);

                YearMonth yearMonthObject = YearMonth.of(intYear, intMonth);
                String lengthOfMonth = Integer.toString(yearMonthObject.lengthOfMonth());

                String lastDayOfMonth = strYear + "-" + timestampMonth + "-" + lengthOfMonth + " 23:50:00"; // "2018-10-04 11:36:57"
                String firstDayOfMonth = strYear + "-" + timestampMonth + "-" + "01 23:50:00";
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date lastDayOfMonthDate = formatter.parse(lastDayOfMonth);
                    Date firstDayOfMonthDate = formatter.parse(firstDayOfMonth);
                    Timestamp monthlyTimestamp = new Timestamp(lastDayOfMonthDate.getTime());
                    Timestamp fromTimestamp = new Timestamp(firstDayOfMonthDate.getTime());

                    Statistics statistics = new Statistics();
                    statistics.setCreatedTimestamp(monthlyTimestamp);
                    statistics.setActiveSandboxesInInterval(Integer.toString(activeSandboxInInterval.size()));
                    statistics.setNewSandboxesInInterval(countNewSandbox.toString());
                    statistics.setFullSandboxCount(Integer.toString(fullSanboxCount.size()));
                    statistics.setDstu2SandboxesInInterval(Integer.toString(DSTU2SanboxesInInterval.size()));
                    statistics.setStu3SandboxesInInterval(Integer.toString(STU3SandboxesInInterval.size()));
                    statistics.setR4SandboxesInInterval(Integer.toString(R4SandboxesInInterval.size()));
                    statistics.setFullUserCount(Integer.toString(fullUserCount.size()));
                    statistics.setActiveUserInInterval(Integer.toString(activeUserInInterval.size()));
                    statistics.setNewUsersInInterval(Integer.toString(countNewUser));
                    statistics.setFullDstu2Count(Integer.toString(fullDSTU2Count.size()));
                    statistics.setFullStu3Count(Integer.toString(fullSTU3Count.size()));
                    statistics.setFullR4Count(Integer.toString(fullR4Count.size()));
                    statistics.setFhirTransactions(Integer.toString(statisticsRepository.getFhirTransaction(fromTimestamp, monthlyTimestamp)));
                    statisticsRepository.save(statistics);
                } catch (ParseException e) {
                        e.printStackTrace();
                }
                countNewSandbox = 0;
                countNewUser = 0;
                DSTU2SanboxesInInterval.clear();
                STU3SandboxesInInterval.clear();
                R4SandboxesInInterval.clear();
                activeSandboxInInterval.clear();
                activeUserInInterval.clear();
                addToDifferentSandboxStats();
                targetMonth = monthActivityLog;
                targetYear = yearActivityLog;
            }
        }
        return statisticsXX;
    }

    public List<Statistics> displayStatsForGivenNumberOfMonths(String numberOfMonths) {
        int intNumberOfMonths = Integer.parseInt(numberOfMonths);
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        int lengthOfMonth = LocalDate.now().lengthOfMonth();
        Date d = new Date();
        Date dateYearAgo = new Date(d.getTime() - intNumberOfMonths * lengthOfMonth * 24 * 3600 * 1000L );
        Timestamp yearAgoTimestamp = new Timestamp(dateYearAgo.getTime());
        return statisticsRepository.get12MonthStatistics(yearAgoTimestamp, currentTimestamp);
    }

    public HashMap<Integer, Statistics> getSandboxAndUserStatsForAYearOriginal() {
        HashMap<Integer, Statistics> monthStats = new HashMap<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        int targetMonth = timestamp.getMonth();
        int targetYear= timestamp.getYear() - 1;
        sandboxActivityLogs = sandboxActivityLogService.findAll();

        for(SandboxActivityLog sandboxActivityLog: sandboxActivityLogs) {
            int monthActivityLog = sandboxActivityLog.getTimestamp().getMonth();
            int yearActivityLog = sandboxActivityLog.getTimestamp().getYear();
            if (sandboxActivityLog.getSandbox() == null) {
                continue;
            } else {
                sandboxId = sandboxActivityLog.getSandbox().getId();
            }
            if (sandboxActivityLog.getUser() == null) {
                continue;
            } else {
                userId = sandboxActivityLog.getUser().getId();
            }
            apiEndpointIndex = sandboxActivityLog.getSandbox().getApiEndpointIndex();
            if(yearActivityLog < targetYear || (yearActivityLog == targetYear && monthActivityLog < targetMonth)) {
                fullSanboxCount.add(sandboxId);
                fullUserCount.add(userId);
                if (apiEndpointIndex.equals("5")) {
                    fullDSTU2Count.add(sandboxId);
                }
                if (apiEndpointIndex.equals("6")) {
                    fullSTU3Count.add(sandboxId);
                }
                if (apiEndpointIndex.equals("7")) {
                    fullR4Count.add(sandboxId);
                }
            } else if (monthActivityLog == targetMonth && yearActivityLog == targetYear) {
                addToDifferentSandboxStats();

            } else {
                Statistics statistics = new Statistics();
                statistics.setActiveSandboxesInInterval(Integer.toString(activeSandboxInInterval.size()));
                statistics.setNewSandboxesInInterval(countNewSandbox.toString());
                statistics.setFullSandboxCount(Integer.toString(fullSanboxCount.size()));
                statistics.setDstu2SandboxesInInterval(Integer.toString(DSTU2SanboxesInInterval.size()));
                statistics.setStu3SandboxesInInterval(Integer.toString(STU3SandboxesInInterval.size()));
                statistics.setR4SandboxesInInterval(Integer.toString(R4SandboxesInInterval.size()));
                statistics.setFullUserCount(Integer.toString(fullUserCount.size()));
                statistics.setActiveUserInInterval(Integer.toString(activeUserInInterval.size()));
                statistics.setNewUsersInInterval(Integer.toString(countNewUser));
                statistics.setFullDstu2Count(Integer.toString(fullDSTU2Count.size()));
                statistics.setFullStu3Count(Integer.toString(fullSTU3Count.size()));
                statistics.setFullR4Count(Integer.toString(fullR4Count.size()));
                if (!monthStats.containsKey(targetMonth + 1)) {
                    monthStats.put((targetMonth + 1), statistics);
                }
                targetMonth = monthActivityLog;
                targetYear = yearActivityLog;
                countNewSandbox = 0;
                countNewUser = 0;
                DSTU2SanboxesInInterval.clear();
                STU3SandboxesInInterval.clear();
                R4SandboxesInInterval.clear();
                activeSandboxInInterval.clear();
                activeUserInInterval.clear();
                addToDifferentSandboxStats();
            }
        }
        return monthStats;
    }

    public HashMap<String, Object> transactionStats(Integer interval, Integer n) {
        HashMap<String, Double> fhirTransactions = new HashMap<>();
        Set<String> sandboxIds = activeSandboxes(interval);

        for (String sandboxId: sandboxIds) {
            List<FhirTransaction> fhirTransactionList = fhirTransactionRepository.findBySandboxId(sandboxService.findBySandboxId(sandboxId).getId()).stream()
                    .filter(x -> new Date().getTime() - x.getTransactionTimestamp().getTime() < TimeUnit.DAYS.toMillis(30))
                    .collect(Collectors.toList());
            fhirTransactions.put(sandboxId, (double) fhirTransactionList.size());
        }

        return compileStats(fhirTransactions, n);
    }

    public HashMap<String, Object> sandboxMemoryStats(Integer interval, Integer n, String request) {
        Set<String> sandboxIds = activeSandboxes(interval);
        List<String> fullSandboxIds = new ArrayList<>();
        HashMap<String, Double> sandboxMemorySizes;

        for (String sandboxId: sandboxIds) {
            Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
            if (sandbox.getApiEndpointIndex().equals("5") || sandbox.getApiEndpointIndex().equals("6") || sandbox.getApiEndpointIndex().equals("7")) {
                fullSandboxIds.add("hspc_5_" + sandboxId);
            } else {
                fullSandboxIds.add("hspc_" + sandbox.getApiEndpointIndex() + "_" + sandboxId);
            }

        }
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer " + request);
        HttpEntity<String> httpEntity = new HttpEntity(fullSandboxIds, requestHeaders);
        sandboxMemorySizes = simpleRestTemplate.exchange(sandboxService.getSystemSandboxApiURL() + "/memory", HttpMethod.PUT, httpEntity, HashMap.class).getBody();
        return compileStats(sandboxMemorySizes, n);
    }

    public HashMap<String, Object> usersPerSandboxStats(Integer interval, Integer n) {
        Iterable<Sandbox> sandboxIterable = sandboxService.findAll();
        HashMap<String, Double> countsMap = new HashMap<>();
        for (Sandbox sandbox: sandboxIterable) {
            List<UserRole> userRoles = sandbox.getUserRoles();
            Set<String> set = new HashSet<>();
            Integer count = userRoles.stream().filter(p -> set.add(p.getUser().getSbmUserId())).collect(Collectors.toList()).size();
            countsMap.put(sandbox.getSandboxId(), (double) count);
        }
        return compileStats(countsMap, n);
    }

    public HashMap<String, Object> sandboxesPerUserStats(Integer interval, Integer n) {
        HashMap<String, Double> countsMap = new HashMap<>();
        Iterable<User> users = userService.findAll();
        for (User user: users) {
            if (user.getSandboxes().size() < 10) {
                if (user.getSandboxes().size() !=0) {
                    countsMap.put(user.getEmail(), (double) user.getSandboxes().size());
                }
            }
        }
        return compileStats(countsMap, n);
    }

    private HashMap<String, Object> compileStats(HashMap<String, Double> valuesMap, Integer n) {
        HashMap<String, Object> stats = new HashMap<>();

        Map<String, Double> topValues = valuesMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        stats.put("median", calculateMedian(new ArrayList<>(topValues.values())));
        stats.put("mean", calculateAverage(new ArrayList<>(topValues.values())));
        if (n != null) {
            topValues = valuesMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(n)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }
        stats.put("top_values", topValues);
        return stats;
    }

    private Set<String> activeSandboxes(Integer interval) {
        Iterable<SandboxActivityLog> sandboxActivityLogs = sandboxActivityLogService.findAll();
        Set<String> sandboxIds = new HashSet<>();
        for (SandboxActivityLog userAccessHistory : sandboxActivityLogs) {
            if (userAccessHistory.getTimestamp().getTime() > (new Date().getTime() - TimeUnit.DAYS.toMillis(interval))) {
                sandboxIds.add(userAccessHistory.getSandbox().getSandboxId());
            }
        }
        return sandboxIds;
    }

    private double calculateAverage(List <Double> values) {
        Double sum = 0.0;
        if(!values.isEmpty()) {
            for (Double mark : values) {
                sum += mark;
            }
            return sum / values.size();
        }
        return sum;
    }

    private double calculateMedian(List <Double> values) {
        if (values.size() % 2 == 0)
            return ((double) values.get(values.size()/2) + values.get(values.size()/2 - 1))/2;
        else
            return values.get(values.size()/2);
    }

    private void addToDifferentSandboxStats (){
        if (!fullSanboxCount.contains(sandboxId)) {
            countNewSandbox++;
        }
        if (!fullUserCount.contains(userId)) {
            countNewUser++;
        }
        activeSandboxInInterval.add(sandboxId);
        activeUserInInterval.add(userId);
        fullSanboxCount.add(sandboxId);
        fullUserCount.add(userId);
        if (apiEndpointIndex.equals("5")) {
            DSTU2SanboxesInInterval.add(sandboxId);
            fullDSTU2Count.add(sandboxId);
        }
        if (apiEndpointIndex.equals("6")) {
            STU3SandboxesInInterval.add(sandboxId);
            fullSTU3Count.add(sandboxId);
        }
        if (apiEndpointIndex.equals("7")) {
            R4SandboxesInInterval.add(sandboxId);
            fullR4Count.add(sandboxId);
        }
    }

}
