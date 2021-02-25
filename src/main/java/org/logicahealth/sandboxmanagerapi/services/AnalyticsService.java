package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.FhirTransaction;
import org.logicahealth.sandboxmanagerapi.model.Statistics;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.model.UserStatistics;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface AnalyticsService {

    Integer countSandboxesByUser(final String userId);

    HashMap<String, Integer> countAppsPerSandboxByUser(User user);

    HashMap<String, Integer> countUsersPerSandboxByUser(User user);

    Integer countTransactionsByPayer(User payer);

    FhirTransaction handleFhirTransaction(User user, HashMap transactionInfo, String bearerToken);

    Double retrieveTotalMemoryByUser(User user, String request);

    Double retrieveMemoryInSchemas(List<String> schemaNames, String request);

    void saveMonthlySandboxStatistics(final String intervalDays);

    Statistics getSandboxStatisticsOverNumberOfDays(final String intervalDays);

    HashMap<String, Object> transactionStats(Integer interval, Integer n);

    HashMap<String, Object> sandboxMemoryStats(Integer interval, Integer n, String request);

    HashMap<String, Object> usersPerSandboxStats(Integer interval, Integer n);

    HashMap<String, Object> sandboxesPerUserStats(Integer interval, Integer n);

    void getSandboxAndUserStatsForLastTwoYears();

    List<Statistics> displayStatsForGivenNumberOfMonths(String numberOfMonths);

    UserStatistics getUserStats(User user, String request);

    Statistics getSandboxStatisticsForSpecificTimePeriod(Date beginDate, Date endDate);

}
