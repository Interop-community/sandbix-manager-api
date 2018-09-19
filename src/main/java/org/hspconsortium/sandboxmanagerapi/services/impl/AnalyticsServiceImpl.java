package org.hspconsortium.sandboxmanagerapi.services.impl;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hspconsortium.sandboxmanagerapi.controllers.UnauthorizedException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.FhirTransactionRepository;
import org.hspconsortium.sandboxmanagerapi.repositories.UserAccessHistoryRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Value("${spring.datasource.base_url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUserName;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    private UserService userService;
    private SandboxService sandboxService;
    private FhirTransactionRepository fhirTransactionRepository;
    private UserAccessHistoryRepository userAccessHistoryRepository;
    private AppService appService;
    private RuleService ruleService;
    private SandboxActivityLogService sandboxActivityLogService;

    @Inject
    AnalyticsServiceImpl(final FhirTransactionRepository fhirTransactionRepository, final UserAccessHistoryRepository userAccessHistoryRepository) {
        this.fhirTransactionRepository = fhirTransactionRepository;
        this.userAccessHistoryRepository = userAccessHistoryRepository;
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

    public FhirTransaction handleFhirTransaction(User user, HashMap transactionInfo) {
        Sandbox sandbox = sandboxService.findBySandboxId(transactionInfo.get("tenant").toString());
        if (!ruleService.checkIfUserCanPerformTransaction(sandbox, transactionInfo.get("method").toString())) {
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
        Integer count = 0;
        List<FhirTransaction> fhirTransactions = fhirTransactionRepository.findByPayerUserId(payer.getId());
        return fhirTransactions.size();
    }

    public Double retrieveTotalMemoryByUser(User user) {
        Double memoryUseInMB = 0.0;
        List<Sandbox> userCreatedSandboxes = sandboxService.findByPayerId(user.getId());
        for (Sandbox sandbox: userCreatedSandboxes) {
            memoryUseInMB += retrieveMemoryInSchema(sandbox.getSandboxId());
        }
        return memoryUseInMB;
    }

    public Double retrieveMemoryInSchema(String schemaName) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(databaseUrl, databaseUserName, databasePassword);
            Statement stmt = conn.createStatement() ;
            String query = "SELECT table_name AS 'Table', ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'\n" +
                    "FROM information_schema.TABLES WHERE table_schema REGEXP 'hspc_[0-9]_" + schemaName + "';" ;
            ResultSet rs = stmt.executeQuery(query);
            Double count = 0.0;
            while (rs.next()) {
                count += Double.parseDouble(rs.getString(2));
            }
            conn.close();
            return count;

        } catch (Exception e) {
            throw new RuntimeException("Error getting memory information for " + schemaName, e);
        }
    }

    public String activeUserCount(Integer intervalDays) {
        Iterable<SandboxActivityLog> sandboxActivityLogs = sandboxActivityLogService.findAll();
        List<SandboxActivityLog> sandboxActivityLogList = new ArrayList<>();
        sandboxActivityLogs.forEach(sandboxActivityLogList::add);
        List<SandboxActivityLog> sandboxActivityLogListInterfalFilter = sandboxActivityLogList.stream().filter(x -> new Date().getTime() - x.getTimestamp().getTime() < TimeUnit.DAYS.toMillis(intervalDays)).collect(Collectors.toList());
        HashSet<Object> seen=new HashSet<>();
        sandboxActivityLogListInterfalFilter.removeIf(e->!seen.add(e.getUser().getId()));
        return Integer.toString(seen.size());
    }

    private class Statistics {
        private String fullSandboxCount;
        private String schema1Sandboxes;
        private String schema2Sandboxes;
        private String schema3Sandboxes;
        private String schema4Sandboxes;
        private String schema5Sandboxes;
        private String sandboxesInInterval;

        private String fullUserCount;
        private String newUsersInInterval;
        private String activeUserInInterval;


        public String getFullSandboxCount() {
            return fullSandboxCount;
        }

        void setFullSandboxCount(String fullSandboxCount) {
            this.fullSandboxCount = fullSandboxCount;
        }

        public String getSchema1Sandboxes() {
            return schema1Sandboxes;
        }

        void setSchema1Sandboxes(String schema1Sandboxes) {
            this.schema1Sandboxes = schema1Sandboxes;
        }

        public String getSchema2Sandboxes() {
            return schema2Sandboxes;
        }

        void setSchema2Sandboxes(String schema2Sandboxes) {
            this.schema2Sandboxes = schema2Sandboxes;
        }

        public String getSchema3Sandboxes() {
            return schema3Sandboxes;
        }

        void setSchema3Sandboxes(String schema3Sandboxes) {
            this.schema3Sandboxes = schema3Sandboxes;
        }

        public String getSchema4Sandboxes() {
            return schema4Sandboxes;
        }

        void setSchema4Sandboxes(String schema4Sandboxes) {
            this.schema4Sandboxes = schema4Sandboxes;
        }

        public String getSandboxesInInterval() {
            return sandboxesInInterval;
        }

        void setSandboxesInInterval(String sandboxesInInterval) {
            this.sandboxesInInterval = sandboxesInInterval;
        }

        public String getFullUserCount() {
            return fullUserCount;
        }

        void setFullUserCount(String fullUserCount) {
            this.fullUserCount = fullUserCount;
        }

        public String getNewUsersInInterval() {
            return newUsersInInterval;
        }

        void setNewUsersInInterval(String newUsersInInterval) {
            this.newUsersInInterval = newUsersInInterval;
        }

        public String getActiveUserInInterval() {
            return activeUserInInterval;
        }

        void setActiveUserInInterval(String activeUserInInterval) {
            this.activeUserInInterval = activeUserInInterval;
        }

        public String getSchema5Sandboxes() {
            return schema5Sandboxes;
        }

        public void setSchema5Sandboxes(String schema5Sandboxes) {
            this.schema5Sandboxes = schema5Sandboxes;
        }
    }

    @Override
    public String getSandboxStatistics(final String intervalDays) {

        int intDays = Integer.parseInt(intervalDays);
        Date d = new Date();
        Date dateBefore = new Date(d.getTime() - intDays * 24 * 3600 * 1000L );
        Timestamp timestamp = new Timestamp(dateBefore.getTime());

        Statistics statistics = new Statistics();
        statistics.setFullSandboxCount(sandboxService.fullCount());
        statistics.setSchema1Sandboxes(sandboxService.schemaCount("1"));
        int apiEndpoint2 = Integer.parseInt(sandboxService.schemaCount("2"));
        int apiEndpoint5 = Integer.parseInt(sandboxService.schemaCount("5"));
        int twoTotal = apiEndpoint2 + apiEndpoint5;
        statistics.setSchema2Sandboxes(Integer.toString(twoTotal));
        statistics.setSchema3Sandboxes(sandboxService.schemaCount("3"));
        int apiEndpoint4 = Integer.parseInt(sandboxService.schemaCount("4"));
        int apiEndpoint6 = Integer.parseInt(sandboxService.schemaCount("6"));
        int fourTotal = apiEndpoint4 + apiEndpoint6;
        statistics.setSchema4Sandboxes(Integer.toString(fourTotal));
        statistics.setSandboxesInInterval(sandboxService.intervalCount(timestamp));
        int apiEndpoint7 = Integer.parseInt(sandboxService.schemaCount("7"));
        statistics.setSchema5Sandboxes(Integer.toString(apiEndpoint7));
        statistics.setFullUserCount(userService.fullCount());
        statistics.setNewUsersInInterval(userService.intervalCount(timestamp));
        statistics.setActiveUserInInterval(activeUserCount(intDays));

        return toJson(statistics);
    }

    private static String toJson(Statistics statistics) {
        Gson gson = new Gson();
        Type type = new TypeToken<Statistics>() {
        }.getType();
        return gson.toJson(statistics, type);
    }

}
