package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.controllers.UnauthorizedException;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.repositories.FhirTransactionRepository;
import org.logicahealth.sandboxmanagerapi.repositories.StatisticsRepository;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    private static Logger LOGGER = LoggerFactory.getLogger(AnalyticsServiceImpl.class.getName());

    @Autowired
    private ApiEndpointIndex apiEndpointIndexObj;

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

    private Set<String> totalSandboxCount = new HashSet<>();
    private Set<String> totalDSTU2Count = new HashSet<>();
    private Set<String> totalSTU3Count = new HashSet<>();
    private Set<String> totalR4Count = new HashSet<>();
    private Set<String> totalUserCount = new HashSet<>();
    private Set<String> activeSandboxesInInterval = new HashSet<>();
    private Set<String> activeDSTU2SanboxesInInterval = new HashSet<>();
    private Set<String> activeSTU3SandboxesInInterval = new HashSet<>();
    private Set<String> activeR4SandboxesInInterval = new HashSet<>();
    private Set<String> activeUserInInterval = new HashSet<>();
    private Integer countNewSandbox = 0;
    private Integer countNewUser = 0;
    private Set<String> newDSTU2SandboxesInInterval = new HashSet<>();
    private Set<String> newSTU3SandboxesInInterval = new HashSet<>();
    private Set<String> newR4SandboxesInInterval = new HashSet<>();
    private String sandboxId = "";
    private String userId = "";
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
        LOGGER.info("countSandboxesByUser");

        User user = userService.findBySbmUserId(userId);

        LOGGER.debug("countSandboxesByUser: "
        +"Parameters: userId = "+userId+"; Return value = "+1);
        return 1;
    }

    public HashMap<String, Integer> countAppsPerSandboxByUser(User user) {
        LOGGER.info("countAppsPerSandboxByUser");

        HashMap<String, Integer> sandboxApps = new HashMap<>();
        List<Sandbox> userCreatedSandboxes = sandboxService.findByPayerId(user.getId());
        for (Sandbox sandbox: userCreatedSandboxes) {
            String sandboxId = sandbox.getSandboxId();
            sandboxApps.put(sandboxId, appService.findBySandboxId(sandboxId).size());
        }

        LOGGER.debug("countAppsPerSandboxByUser: "
        + "Parameters: user = "+user+"; Return value = "+sandboxApps);

        return sandboxApps;
    }

    public HashMap<String, Integer> countUsersPerSandboxByUser(User user) {
        LOGGER.info("countUsersPerSandboxByUser");

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

        LOGGER.debug("countUsersPerSandboxByUser: "
        +"Parameters: user = "+user+";Return value = "+sandboxUsers);

        return sandboxUsers;
    }

    public FhirTransaction handleFhirTransaction(User user, HashMap transactionInfo, String bearerToken) {
        LOGGER.info("handleFhirTransaction");

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
        
        FhirTransaction returnValue = fhirTransactionRepository.save(fhirTransaction);

        LOGGER.debug("handleFhirTransaction: "
        +"Parameters: user = "+user+", transactionInfo = "+transactionInfo
        +", bearerToken = "+bearerToken+"; Return Value = "+returnValue);

        return returnValue;
    }

    public Integer countTransactionsByPayer(User payer) {
        
        LOGGER.info("countTransactionsByPayer");

        List<FhirTransaction> fhirTransactions = fhirTransactionRepository.findByPayerUserId(payer.getId());

        LOGGER.debug("countTransactionsByPayer "+
        "Parameters: payer = "+payer+"; Return Value = "+fhirTransactions.size());
        return fhirTransactions.size();
    }

    public Double retrieveTotalMemoryByUser(User user, String request) {
        LOGGER.info("retrieveTotalMemoryByUser");

        List<Sandbox> userCreatedSandboxes = sandboxService.findByPayerId(user.getId());
        List<String> sandboxIds = new ArrayList<>();
        for (Sandbox sandbox: userCreatedSandboxes) {
            sandboxIds.add(sandbox.getSandboxId());
        }

        Double returnValue = retrieveMemoryInSchemas(sandboxIds, request);

        LOGGER.debug("retrieveTotalMemoryByUser: "
        +"Parameters: user = "+user+", request = "+request+"; Return Value = "+returnValue);

        return returnValue;
    }

    public Double retrieveMemoryInSchemas(List<String> schemaNames, String request) {
        LOGGER.info("retrieveMemoryInSchemas");

        HashMap<String, Double> sandboxMemorySizes;
        Double count = 0.0;

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer " + request);
        HttpEntity<List<String>> httpEntity = new HttpEntity(schemaNames, requestHeaders);
        sandboxMemorySizes = simpleRestTemplate.exchange(sandboxService.getSystemSandboxApiURL() + "/memory/user", HttpMethod.PUT, httpEntity, HashMap.class).getBody();
        for (Double memory: sandboxMemorySizes.values()) {
            count += memory;
        }

        LOGGER.debug("retrieveMemoryInSchemas: "
        +"Parameters: schemaNames = "+schemaNames+", request = "+request+"; Return Value = "+count);

        return count;
    }

    private void sandboxActivityLogListIntervalFilter(Timestamp timestamp, Integer intervalDays) {
        LOGGER.info("sandboxActivityLogListIntervalFilter");

        sandboxActivityLogs = sandboxActivityLogService.findAll();
        for (SandboxActivityLog sandboxActivityLog: sandboxActivityLogs) {
            if (sandboxActivityLog.getSandbox() != null && sandboxActivityLog.getTimestamp().before(timestamp)) {
                totalSandboxCount.add(sandboxActivityLog.getSandbox().getSandboxId());
            }
        }
        List<SandboxActivityLog> sandboxActivityLogList = new ArrayList<>();
        sandboxActivityLogs.forEach(sandboxActivityLogList::add);
        sandboxActivityLogListIntervalFilter = sandboxActivityLogList.stream().filter(x -> new Date().getTime() - x.getTimestamp().getTime() < TimeUnit.DAYS.toMillis(intervalDays)).collect(Collectors.toList());

        LOGGER.debug("sandboxActivityLogListIntervalFilter: "
        +"Parameters: timestamp = "+timestamp+", intervalDays = "+intervalDays+"; No return value");
    }

    private void sandboxActivityLogListIntervalFilterForSpecificTimePeriod(Timestamp beginDateTimestamp, Timestamp endDateTimestamp) {
        LOGGER.info("sandboxActivityLogListIntervalFilterForSpecificTimePeriod");

        sandboxActivityLogListIntervalFilter = sandboxActivityLogService.findAllForSpecificTimePeriod(beginDateTimestamp, endDateTimestamp);

        LOGGER.debug("sandboxActivityLogListIntervalFilterForSpecificTimePeriod: "
        +"Parameters: beginDateTimestamp = "+beginDateTimestamp+", endDateTimestamp = "+endDateTimestamp);
    }

    private String activeUserCount() {
        
        LOGGER.info("activeUserCount");

        HashSet<Object> seen = new HashSet<>();
        List<SandboxActivityLog> activeInInterval = new ArrayList<>(sandboxActivityLogListIntervalFilter);
        activeInInterval.removeIf(e->!seen.add(e.getUser().getSbmUserId()));

        LOGGER.debug("activeUserCount: "
        +"No parameters; Return Value = "+Integer.toString(seen.size()));

        return Integer.toString(seen.size());
    }

    private String activeSandboxCount() {
        
        LOGGER.info("activeSandboxCount");

        List<SandboxActivityLog> activeInInterval = new ArrayList<>(sandboxActivityLogListIntervalFilter);
        HashSet<Object> seenSandboxes = new HashSet<>();
        activeInInterval.removeIf(e->e.getSandbox() != null && !seenSandboxes.add(e.getSandbox().getSandboxId()));

        LOGGER.debug("activeSandboxCount: "
        +"No parameters; Return Value = "+Integer.toString(seenSandboxes.size()));

        return Integer.toString(seenSandboxes.size());
    }

    private String activeDstu2SandboxesInInterval() {
                
        LOGGER.info("activeDstu2SandboxesInInterval");
        
        Set<String> dstu2Sandbox = new HashSet<>();
        for (SandboxActivityLog sandboxActivityLog: sandboxActivityLogListIntervalFilter) {
            if (sandboxActivityLog.getSandbox() != null &&
                    (sandboxActivityLog.getSandbox().getApiEndpointIndex().equals(apiEndpointIndexObj.getCurrent().getDstu2())
                            || sandboxActivityLog.getSandbox().getApiEndpointIndex().equals(apiEndpointIndexObj.getPrev().getDstu2()))) {
                dstu2Sandbox.add(sandboxActivityLog.getSandbox().getSandboxId());
            }
        }

        LOGGER.debug("activeDstu2SandboxesInInterval: "
        +"No parameters; Return Value = "+Integer.toString(dstu2Sandbox.size()));

        return Integer.toString(dstu2Sandbox.size());
    }

    private String activeStu3SandboxesInInterval() {
        
        LOGGER.info("activeStu3SandboxesInInterval");

        Set<String> stu3Sandbox = new HashSet<>();
        for (SandboxActivityLog sandboxActivityLog: sandboxActivityLogListIntervalFilter) {
            if (sandboxActivityLog.getSandbox() != null &&
                    (sandboxActivityLog.getSandbox().getApiEndpointIndex().equals(apiEndpointIndexObj.getCurrent().getStu3())
                    || sandboxActivityLog.getSandbox().getApiEndpointIndex().equals(apiEndpointIndexObj.getPrev().getStu3()))) {
                stu3Sandbox.add(sandboxActivityLog.getSandbox().getSandboxId());
            }
        }

        LOGGER.debug("activeStu3SandboxesInInterval: "
        +"No parameters; Return Value = "+Integer.toString(stu3Sandbox.size()));

        return Integer.toString(stu3Sandbox.size());
    }

    private String activeR4SandboxesInInterval() {
        
        LOGGER.info("activeR4SandboxesInInterval");

        Set<String> r4Sandbox = new HashSet<>();
        for (SandboxActivityLog sandboxActivityLog: sandboxActivityLogListIntervalFilter) {
            if (sandboxActivityLog.getSandbox() != null &&
                    (sandboxActivityLog.getSandbox().getApiEndpointIndex().equals(apiEndpointIndexObj.getCurrent().getR4())
                    || sandboxActivityLog.getSandbox().getApiEndpointIndex().equals(apiEndpointIndexObj.getPrev().getR4()))) {
                r4Sandbox.add(sandboxActivityLog.getSandbox().getSandboxId());
            }
        }

        LOGGER.debug("activeR4SandboxesInInterval: "
        +"No parameters; Return Value = "+Integer.toString(r4Sandbox.size()));

        return Integer.toString(r4Sandbox.size());
    }

    private Statistics getSandboxStatistics(String intervalDays) {
        
        LOGGER.info("getSandboxStatistics");

        int intDays = Integer.parseInt(intervalDays);
        Date d = new Date();
        Date firstDayOfTheMonth = new Date(d.getTime() - (intDays - 1) * 24 * 3600 * 1000L );
        Timestamp firstDayOfTheMonthTimestamp = new Timestamp(firstDayOfTheMonth.getTime());

        Timestamp lastDayOfTheMonthTimestamp = new Timestamp(System.currentTimeMillis());
        sandboxActivityLogListIntervalFilter(firstDayOfTheMonthTimestamp, intDays);

        Integer totalDstu2SandboxesCount = getInteger(sandboxService.schemaCount(apiEndpointIndexObj.getCurrent().getDstu2()))
                                            + getInteger(sandboxService.schemaCount(apiEndpointIndexObj.getPrev().getDstu2()));

        Integer totalStu3SandboxesCount = getInteger(sandboxService.schemaCount(apiEndpointIndexObj.getCurrent().getStu3()))
                                            + getInteger(sandboxService.schemaCount(apiEndpointIndexObj.getPrev().getStu3()));

        Integer totalR4SandboxesCount = getInteger(sandboxService.schemaCount(apiEndpointIndexObj.getCurrent().getR4()))
                                            + getInteger(sandboxService.schemaCount(apiEndpointIndexObj.getPrev().getR4()));

        Integer newDstu2SandboxesInInterval = getInteger(sandboxService.newSandboxesInIntervalCount(firstDayOfTheMonthTimestamp, apiEndpointIndexObj.getCurrent().getDstu2()))
                                            + getInteger(sandboxService.newSandboxesInIntervalCount(firstDayOfTheMonthTimestamp, apiEndpointIndexObj.getPrev().getDstu2()));

        Integer newStu3SandboxesInInterval = getInteger(sandboxService.newSandboxesInIntervalCount(firstDayOfTheMonthTimestamp, apiEndpointIndexObj.getCurrent().getStu3()))
                                            + getInteger(sandboxService.newSandboxesInIntervalCount(firstDayOfTheMonthTimestamp, apiEndpointIndexObj.getPrev().getStu3()));

        Integer newR4SandboxesInInterval = getInteger(sandboxService.newSandboxesInIntervalCount(firstDayOfTheMonthTimestamp, apiEndpointIndexObj.getCurrent().getR4()))
                                            + getInteger(sandboxService.newSandboxesInIntervalCount(firstDayOfTheMonthTimestamp, apiEndpointIndexObj.getPrev().getR4()));

        Statistics statistics = new Statistics();
        statistics.setCreatedTimestamp(lastDayOfTheMonthTimestamp);
        statistics.setTotalSandboxesCount(sandboxService.fullCount());
        statistics.setTotalDstu2SandboxesCount(totalDstu2SandboxesCount.toString());
        statistics.setTotalStu3SandboxesCount(totalStu3SandboxesCount.toString());
        statistics.setTotalR4SandboxesCount(totalR4SandboxesCount.toString());
        statistics.setTotalUsersCount(userService.fullCount());

        statistics.setActiveSandboxesInInterval(activeSandboxCount());
        statistics.setActiveDstu2SandboxesInInterval(activeDstu2SandboxesInInterval());
        statistics.setActiveStu3SandboxesInInterval(activeStu3SandboxesInInterval());
        statistics.setActiveR4SandboxesInInterval(activeR4SandboxesInInterval());
        statistics.setActiveUsersInInterval(activeUserCount());

        statistics.setNewSandboxesInInterval(sandboxService.intervalCount(firstDayOfTheMonthTimestamp));
        statistics.setNewDstu2SandboxesInInterval(newDstu2SandboxesInInterval.toString());
        statistics.setNewStu3SandboxesInInterval(newStu3SandboxesInInterval.toString());
        statistics.setNewR4SandboxesInInterval(newR4SandboxesInInterval.toString());
        statistics.setNewUsersInInterval(userService.intervalCount(firstDayOfTheMonthTimestamp));

        statistics.setFhirTransactions(Integer.toString(statisticsRepository.getFhirTransaction(firstDayOfTheMonthTimestamp, lastDayOfTheMonthTimestamp)));

        LOGGER.debug("getSandboxStatistics: "
        +"Parameters: intervalDays = "+intervalDays+"; Return Value = "+statistics);

        return statistics;
    }

    @Override
    public void saveMonthlySandboxStatistics(final String intervalDays) {

        LOGGER.info("saveMonthlySandboxStatistics");

        Statistics statistics = getSandboxStatistics(intervalDays);
        statisticsRepository.save(statistics);

        LOGGER.debug("saveMonthlySandboxStatistics: "
        +"Parameters: intervalDays = "+intervalDays+"; No return value");

    }

    @Override
    public Statistics getSandboxStatisticsOverNumberOfDays(final String intervalDays) {
        
        LOGGER.info("getSandboxStatisticsOverNumberOfDays");

        LOGGER.debug("getSandboxStatisticsOverNumberOfDays: "
        +"Parameters: intervalDays = "+intervalDays+"; Return Value = "+getSandboxStatistics(intervalDays));

        return getSandboxStatistics(intervalDays);
    }

    @Scheduled(cron = "0 50 23 28-31 * ?")
    public void snapshotStatistics() {
        
        LOGGER.info("snapshotStatistics");

        final Calendar c = Calendar.getInstance();
        if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {
            int intDays = LocalDate.now().lengthOfMonth();
            saveMonthlySandboxStatistics(Integer.toString(intDays));
        }

        LOGGER.debug("snapshotStatistics: No parameters; No return value");

   }

    public void getSandboxAndUserStatsForLastTwoYears() {
        
        LOGGER.info("getSandboxAndUserStatsForLastTwoYears");

        totalSandboxCount.clear();
        totalUserCount.clear();
        totalDSTU2Count.clear();
        totalSTU3Count.clear();
        totalR4Count.clear();
        countNewSandbox = 0;
        countNewUser = 0;
        activeDSTU2SanboxesInInterval.clear();
        activeSTU3SandboxesInInterval.clear();
        activeR4SandboxesInInterval.clear();
        activeSandboxesInInterval.clear();
        activeUserInInterval.clear();
        newDSTU2SandboxesInInterval.clear();
        newSTU3SandboxesInInterval.clear();
        newR4SandboxesInInterval.clear();

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        int targetMonth = currentTimestamp.getMonth();
        int targetYear = currentTimestamp.getYear() - 2;
        sandboxActivityLogs = sandboxActivityLogService.findAll();
        for(SandboxActivityLog sandboxActivityLog: sandboxActivityLogs) {
            int monthActivityLog = sandboxActivityLog.getTimestamp().getMonth();
            int yearActivityLog = sandboxActivityLog.getTimestamp().getYear();

            if (sandboxActivityLog.getUser() != null) {
                userId = sandboxActivityLog.getUser().getSbmUserId();
            }

            if (sandboxActivityLog.getSandbox() != null) {
                sandboxId = sandboxActivityLog.getSandbox().getSandboxId();
                apiEndpointIndex = sandboxActivityLog.getSandbox().getApiEndpointIndex();
            } else {
                sandboxId = "-1";
                apiEndpointIndex = "-1";
            }
            if(yearActivityLog < targetYear || (yearActivityLog == targetYear && monthActivityLog < targetMonth)) {
                totalSandboxCount.add(sandboxId);
                    totalUserCount.add(userId);
                if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent().getDstu2()) || apiEndpointIndex.equals(apiEndpointIndexObj.getPrev().getDstu2())) {
                    totalDSTU2Count.add(sandboxId);
                }
                if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent().getStu3()) || apiEndpointIndex.equals(apiEndpointIndexObj.getPrev().getStu3())) {
                    totalSTU3Count.add(sandboxId);
                }
                if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent().getR4()) || apiEndpointIndex.equals(apiEndpointIndexObj.getPrev().getR4())) {
                    totalR4Count.add(sandboxId);
                }
            } else if (monthActivityLog == targetMonth && yearActivityLog == targetYear) {
                addToDifferentSandboxStats();
            } else {
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

                String lastDayOfMonth = strYear + "-" + timestampMonth + "-" + lengthOfMonth + " 23:50:00";
                String firstDayOfMonth = strYear + "-" + timestampMonth + "-" + "01 23:50:00";
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date lastDayOfMonthDate = formatter.parse(lastDayOfMonth);
                    Date firstDayOfMonthDate = formatter.parse(firstDayOfMonth);
                    Timestamp monthlyTimestamp = new Timestamp(lastDayOfMonthDate.getTime());
                    Timestamp fromTimestamp = new Timestamp(firstDayOfMonthDate.getTime());

                    Statistics statistics = new Statistics();
                    statistics.setCreatedTimestamp(monthlyTimestamp);
                    statistics.setTotalSandboxesCount(Integer.toString(totalSandboxCount.size()));
                    statistics.setTotalDstu2SandboxesCount(Integer.toString(totalDSTU2Count.size()));
                    statistics.setTotalStu3SandboxesCount(Integer.toString(totalSTU3Count.size()));
                    statistics.setTotalR4SandboxesCount(Integer.toString(totalR4Count.size()));
                    statistics.setTotalUsersCount(Integer.toString(totalUserCount.size()));
                    statistics.setActiveSandboxesInInterval(Integer.toString(activeSandboxesInInterval.size()));
                    statistics.setActiveDstu2SandboxesInInterval(Integer.toString(activeDSTU2SanboxesInInterval.size()));
                    statistics.setActiveStu3SandboxesInInterval(Integer.toString(activeSTU3SandboxesInInterval.size()));
                    statistics.setActiveR4SandboxesInInterval(Integer.toString(activeR4SandboxesInInterval.size()));
                    statistics.setActiveUsersInInterval(Integer.toString(activeUserInInterval.size()));
                    statistics.setNewSandboxesInInterval(countNewSandbox.toString());
                    statistics.setNewDstu2SandboxesInInterval(Integer.toString(newDSTU2SandboxesInInterval.size()));
                    statistics.setNewStu3SandboxesInInterval(Integer.toString(newSTU3SandboxesInInterval.size()));
                    statistics.setNewR4SandboxesInInterval(Integer.toString(newR4SandboxesInInterval.size()));
                    statistics.setNewUsersInInterval(Integer.toString(countNewUser));
                    statistics.setFhirTransactions(Integer.toString(statisticsRepository.getFhirTransaction(fromTimestamp, monthlyTimestamp)));
                    statisticsRepository.save(statistics);
                } catch (ParseException e) {
                        e.printStackTrace();
                }
                countNewSandbox = 0;
                countNewUser = 0;
                activeDSTU2SanboxesInInterval.clear();
                activeSTU3SandboxesInInterval.clear();
                activeR4SandboxesInInterval.clear();
                activeSandboxesInInterval.clear();
                activeUserInInterval.clear();
                newDSTU2SandboxesInInterval.clear();
                newSTU3SandboxesInInterval.clear();
                newR4SandboxesInInterval.clear();
                addToDifferentSandboxStats();
                targetMonth = monthActivityLog;
                targetYear = yearActivityLog;
            }
        }

        LOGGER.debug("getSandboxAndUserStatsForLastTwoYears: No parameters; No return value");

    }

    public List<Statistics> displayStatsForGivenNumberOfMonths(String numberOfMonths) {
        
        LOGGER.info("displayStatsForGivenNumberOfMonths");

        int intNumberOfMonths = Integer.parseInt(numberOfMonths);
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        int lengthOfMonth = LocalDate.now().lengthOfMonth();
        Date d = new Date();
        Date dateYearAgo = new Date(d.getTime() - intNumberOfMonths * lengthOfMonth * 24 * 3600 * 1000L );
        Timestamp yearAgoTimestamp = new Timestamp(dateYearAgo.getTime());

        LOGGER.debug("displayStatsForGivenNumberOfMonths: "
        +"Parameters: numberOfMonths = "+numberOfMonths
        +"; Return value = "+statisticsRepository.get12MonthStatistics(yearAgoTimestamp, currentTimestamp));

        return statisticsRepository.get12MonthStatistics(yearAgoTimestamp, currentTimestamp);
    }

    public HashMap<String, Object> transactionStats(Integer interval, Integer n) {
        
        LOGGER.info("transactionStats");

        HashMap<String, Double> fhirTransactions = new HashMap<>();
        Set<String> sandboxIds = activeSandboxes(interval);

        for (String sandboxId: sandboxIds) {
            List<FhirTransaction> fhirTransactionList = fhirTransactionRepository.findBySandboxId(sandboxService.findBySandboxId(sandboxId).getId()).stream()
                    .filter(x -> new Date().getTime() - x.getTransactionTimestamp().getTime() < TimeUnit.DAYS.toMillis(30))
                    .collect(Collectors.toList());
            fhirTransactions.put(sandboxId, (double) fhirTransactionList.size());
        }

        LOGGER.debug("transactionStats: "
        +"Parameters: interval = "+interval+", n = "+n+"; Return Value = "+compileStats(fhirTransactions, n));

        return compileStats(fhirTransactions, n);
    }

    public HashMap<String, Object> sandboxMemoryStats(Integer interval, Integer n, String request) {
        
        LOGGER.info("sandboxMemoryStats");

        Set<String> sandboxIds = activeSandboxes(interval);
        List<String> fullSandboxIds = new ArrayList<>();
        HashMap<String, Double> sandboxMemorySizes;

        for (String sandboxId: sandboxIds) {
            Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
            if (sandbox.getApiEndpointIndex().equals(apiEndpointIndexObj.getCurrent().getDstu2())
                    || sandbox.getApiEndpointIndex().equals(apiEndpointIndexObj.getCurrent().getStu3())
                    || sandbox.getApiEndpointIndex().equals(apiEndpointIndexObj.getCurrent().getR4())) {
                fullSandboxIds.add("hspc_" + apiEndpointIndexObj.getCurrent().getDstu2() + "_" + sandboxId);
            } else if (sandbox.getApiEndpointIndex().equals(apiEndpointIndexObj.getPrev().getDstu2())
                    || sandbox.getApiEndpointIndex().equals(apiEndpointIndexObj.getPrev().getStu3())
                    || sandbox.getApiEndpointIndex().equals(apiEndpointIndexObj.getPrev().getR4())) {
                fullSandboxIds.add("hspc_" + apiEndpointIndexObj.getPrev().getDstu2() + "_" + sandboxId);
            } else {
                fullSandboxIds.add("hspc_" + sandbox.getApiEndpointIndex() + "_" + sandboxId);
            }

        }
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer " + request);
        HttpEntity<String> httpEntity = new HttpEntity(fullSandboxIds, requestHeaders);
        sandboxMemorySizes = simpleRestTemplate.exchange(sandboxService.getSystemSandboxApiURL() + "/memory", HttpMethod.PUT, httpEntity, HashMap.class).getBody();

        LOGGER.debug("sandboxMemoryStats: "
        +"Parameters: interval = "+interval+", n = "+n+", request = "+request
        +"; Return Value = "+compileStats(sandboxMemorySizes, n));

        return compileStats(sandboxMemorySizes, n);
    }

    public HashMap<String, Object> usersPerSandboxStats(Integer interval, Integer n) {
        
        LOGGER.info("usersPerSandboxStats");

        Iterable<Sandbox> sandboxIterable = sandboxService.findAll();
        HashMap<String, Double> countsMap = new HashMap<>();
        for (Sandbox sandbox: sandboxIterable) {
            List<UserRole> userRoles = sandbox.getUserRoles();
            Set<String> set = new HashSet<>();
            Integer count = userRoles.stream().filter(p -> set.add(p.getUser().getSbmUserId())).collect(Collectors.toList()).size();
            countsMap.put(sandbox.getSandboxId(), (double) count);
        }

        LOGGER.debug("usersPerSandboxStats: "
        +"Parameters: interval = "+interval+", n = "+n+"; Return Value = "+compileStats(countsMap, n));

        return compileStats(countsMap, n);
    }

    public HashMap<String, Object> sandboxesPerUserStats(Integer interval, Integer n) {
        
        LOGGER.info("sandboxesPerUserStats");

        HashMap<String, Double> countsMap = new HashMap<>();
        Iterable<User> users = userService.findAll();
        for (User user: users) {
            if (user.getSandboxes().size() < 10) {
                if (user.getSandboxes().size() !=0) {
                    countsMap.put(user.getEmail(), (double) user.getSandboxes().size());
                }
            }
        }

        LOGGER.debug("sandboxesPerUserStats: "
        +"Parameters: interval = "+interval+", n = "+n+"; Return Value = "+compileStats(countsMap, n));

        return compileStats(countsMap, n);
    }

    public UserStatistics getUserStats(User user, String request) {
        
        LOGGER.info("getUserStats");

        UserStatistics usrStats = new UserStatistics(ruleService.findRulesByUser(user));

        usrStats.setMemoryCount(retrieveTotalMemoryByUser(user, request));
        usrStats.setTransactionsCount(countTransactionsByPayer(user));
        usrStats.setApplicationsCount(countAppsPerSandboxByUser(user));
        usrStats.setSandboxesCount(countSandboxesByUser(user.getSbmUserId()));
        usrStats.setUsersCount(countUsersPerSandboxByUser(user));

        LOGGER.debug("getUserStats: "
        +"Parameters: user = "+user+", request = "+request+"; Return Value = "+usrStats);

        return usrStats;
    }

    private HashMap<String, Object> compileStats(HashMap<String, Double> valuesMap, Integer n) {
        
        LOGGER.info("compileStats");

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

        LOGGER.debug("compileStats: "
        +"Parameters: valuesMap = "+valuesMap+", n = "+n+"; Return Value = "+stats);

        return stats;
    }

    private Set<String> activeSandboxes(Integer interval) {
        
        LOGGER.info("activeSandboxes");

        Iterable<SandboxActivityLog> sandboxActivityLogs = sandboxActivityLogService.findAll();
        Set<String> sandboxIds = new HashSet<>();
        for (SandboxActivityLog userAccessHistory : sandboxActivityLogs) {
            if (userAccessHistory.getTimestamp().getTime() > (new Date().getTime() - TimeUnit.DAYS.toMillis(interval))) {
                sandboxIds.add(userAccessHistory.getSandbox().getSandboxId());
            }
        }

        LOGGER.debug("activeSandboxes: "
        +"Parameters: interval = "+interval+"; Return Value = "+sandboxIds);

        return sandboxIds;
    }

    private double calculateAverage(List <Double> values) {
        
        LOGGER.info("calculateAverage");

        Double sum = 0.0;
        if(!values.isEmpty()) {
            for (Double mark : values) {
                sum += mark;
            }
            return sum / values.size();
        }

        LOGGER.debug("calculateAverage: "
        +"Parameters: values = "+values+"; Return Value = "+sum);

        return sum;
    }

    private double calculateMedian(List <Double> values) {

        LOGGER.info("calculateMedian");

        if (values.size() % 2 == 0){

            LOGGER.debug("calculateMedian: "
            +"Parameters: values = "+values
            +"; Return Value = "+((double) values.get(values.size()/2) + values.get(values.size()/2 - 1))/2);

            return ((double) values.get(values.size()/2) + values.get(values.size()/2 - 1))/2;
        }
        else{

            LOGGER.debug("calculateMedian: "
            +"Parameters: values = "+values
            +"; Return Value = "+values.get(values.size()/2));

            return values.get(values.size()/2);
        }
    }

    private void addToDifferentSandboxStats (){
        
        LOGGER.info("addToDifferentSandboxStats");

        if (!sandboxId.equals("-1") && !totalSandboxCount.contains(sandboxId)) {
            countNewSandbox++;
            if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent().getDstu2()) || apiEndpointIndex.equals(apiEndpointIndexObj.getPrev().getDstu2())) {
                newDSTU2SandboxesInInterval.add(sandboxId);
            }
            if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent().getStu3()) || apiEndpointIndex.equals(apiEndpointIndexObj.getPrev().getStu3())) {
                newSTU3SandboxesInInterval.add(sandboxId);
            }
            if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent().getR4()) || apiEndpointIndex.equals(apiEndpointIndexObj.getPrev().getR4())) {
                newR4SandboxesInInterval.add(sandboxId);
            }
        }
        if (!totalUserCount.contains(userId)) {
            countNewUser++;
        }
        if (!sandboxId.equals("-1")) {
            activeSandboxesInInterval.add(sandboxId);
            totalSandboxCount.add(sandboxId);
        }
        activeUserInInterval.add(userId);
        totalUserCount.add(userId);
        if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent().getDstu2()) || apiEndpointIndex.equals(apiEndpointIndexObj.getPrev().getDstu2())) {
            activeDSTU2SanboxesInInterval.add(sandboxId);
            totalDSTU2Count.add(sandboxId);
        }
        if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent().getStu3()) || apiEndpointIndex.equals(apiEndpointIndexObj.getPrev().getStu3())) {
            activeSTU3SandboxesInInterval.add(sandboxId);
            totalSTU3Count.add(sandboxId);
        }
        if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent().getR4()) || apiEndpointIndex.equals(apiEndpointIndexObj.getPrev().getR4())) {
            activeR4SandboxesInInterval.add(sandboxId);
            totalR4Count.add(sandboxId);
        }

        LOGGER.debug("addToDifferentSandboxStats: "
        +"No parameters; No return value");

    }

    private int getInteger(String num) {
        
        LOGGER.info("getInteger");

        if (num !=null) {
            
            LOGGER.debug("getInteger: "
            +"Parameters: num = "+num+"; Return Value = "+Integer.parseInt(num));

            return Integer.parseInt(num);
        } else {

            LOGGER.debug("getInteger: "
            +"Parameters: num = "+num+"; Return Value = "+0);

            return 0;
        }
    }
    @Override
    public Statistics getSandboxStatisticsForSpecificTimePeriod(Date beginDate, Date endDate) {
        
        LOGGER.info("getSandboxStatisticsForSpecificTimePeriod");

        Timestamp beginDateTimestamp = new Timestamp(beginDate.getTime());
        Timestamp endDateTimestamp = new Timestamp(endDate.getTime());
        sandboxActivityLogListIntervalFilterForSpecificTimePeriod(beginDateTimestamp, endDateTimestamp);

        Integer totalDstu2SandboxesCount = getInteger(sandboxService.schemaCountForSpecificTimePeriod(apiEndpointIndexObj.getCurrent().getDstu2(), endDateTimestamp))
                + getInteger(sandboxService.schemaCountForSpecificTimePeriod(apiEndpointIndexObj.getPrev().getDstu2(), endDateTimestamp));

        Integer totalStu3SandboxesCount = getInteger(sandboxService.schemaCountForSpecificTimePeriod(apiEndpointIndexObj.getCurrent().getStu3(), endDateTimestamp))
                + getInteger(sandboxService.schemaCountForSpecificTimePeriod(apiEndpointIndexObj.getPrev().getStu3(), endDateTimestamp));

        Integer totalR4SandboxesCount = getInteger(sandboxService.schemaCountForSpecificTimePeriod(apiEndpointIndexObj.getCurrent().getR4(), endDateTimestamp))
                + getInteger(sandboxService.schemaCountForSpecificTimePeriod(apiEndpointIndexObj.getPrev().getR4(), endDateTimestamp));

        Integer newDstu2SandboxesInInterval = getInteger(sandboxService.newSandboxesInIntervalCountForSpecificTimePeriod(apiEndpointIndexObj.getCurrent().getDstu2(), beginDateTimestamp, endDateTimestamp))
                + getInteger(sandboxService.newSandboxesInIntervalCountForSpecificTimePeriod(apiEndpointIndexObj.getPrev().getDstu2(), beginDateTimestamp, endDateTimestamp));

        Integer newStu3SandboxesInInterval = getInteger(sandboxService.newSandboxesInIntervalCountForSpecificTimePeriod(apiEndpointIndexObj.getCurrent().getStu3(), beginDateTimestamp, endDateTimestamp))
                + getInteger(sandboxService.newSandboxesInIntervalCountForSpecificTimePeriod(apiEndpointIndexObj.getPrev().getStu3(), beginDateTimestamp, endDateTimestamp));

        Integer newR4SandboxesInInterval = getInteger(sandboxService.newSandboxesInIntervalCountForSpecificTimePeriod(apiEndpointIndexObj.getCurrent().getR4(), beginDateTimestamp, endDateTimestamp))
                + getInteger(sandboxService.newSandboxesInIntervalCountForSpecificTimePeriod(apiEndpointIndexObj.getPrev().getR4(), beginDateTimestamp, endDateTimestamp));

        Statistics statistics = new Statistics();
        statistics.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        statistics.setTotalSandboxesCount(sandboxService.fullCountForSpecificTimePeriod(endDateTimestamp));
        statistics.setTotalDstu2SandboxesCount(totalDstu2SandboxesCount.toString());
        statistics.setTotalStu3SandboxesCount(totalStu3SandboxesCount.toString());
        statistics.setTotalR4SandboxesCount(totalR4SandboxesCount.toString());
        statistics.setTotalUsersCount(userService.fullCountForSpecificPeriod(endDateTimestamp));

        statistics.setActiveSandboxesInInterval(activeSandboxCount());
        statistics.setActiveDstu2SandboxesInInterval(activeDstu2SandboxesInInterval());
        statistics.setActiveStu3SandboxesInInterval(activeStu3SandboxesInInterval());
        statistics.setActiveR4SandboxesInInterval(activeR4SandboxesInInterval());
        statistics.setActiveUsersInInterval(activeUserCount());

        statistics.setNewSandboxesInInterval(sandboxService.intervalCountForSpecificTimePeriod(beginDateTimestamp, endDateTimestamp));
        statistics.setNewDstu2SandboxesInInterval(newDstu2SandboxesInInterval.toString());
        statistics.setNewStu3SandboxesInInterval(newStu3SandboxesInInterval.toString());
        statistics.setNewR4SandboxesInInterval(newR4SandboxesInInterval.toString());
        statistics.setNewUsersInInterval(userService.intervalCountForSpecificTimePeriod(beginDateTimestamp, endDateTimestamp));

        statistics.setFhirTransactions(Integer.toString(statisticsRepository.getFhirTransaction(beginDateTimestamp, endDateTimestamp)));

        LOGGER.debug("getSandboxStatisticsForSpecificTimePeriod: "
        +"Parameters: beginDate = "+beginDate+", endDate = "+endDate+"; Return Value = "+statistics);

        return statistics;
    }
}
