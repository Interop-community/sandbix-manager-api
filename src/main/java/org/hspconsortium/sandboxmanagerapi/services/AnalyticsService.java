package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.FhirTransaction;
import org.hspconsortium.sandboxmanagerapi.model.Statistics;
import org.hspconsortium.sandboxmanagerapi.model.User;

import java.sql.Timestamp;
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

    Statistics getSandboxStatistics(final String intervalDays);

    HashMap<String, Object> transactionStats(Integer interval, Integer n);

    HashMap<String, Object> sandboxMemoryStats(Integer interval, Integer n, String request);

    HashMap<String, Object> usersPerSandboxStats(Integer interval, Integer n);

    HashMap<String, Object> sandboxesPerUserStats(Integer interval, Integer n);

    Statistics getSandboxAndUserStatsForLastTwoYears();

    List<Statistics> displayStatsForGivenNumberOfMonths(String numberOfMonths);

}
