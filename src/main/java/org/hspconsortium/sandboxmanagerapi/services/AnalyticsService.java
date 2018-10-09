package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.FhirTransaction;
import org.hspconsortium.sandboxmanagerapi.model.User;

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

    String getSandboxStatistics(final String intervalDays);

    String activeUserCount(Integer intervalDays);

    HashMap<String, Object> transactionStats(Integer interval, Integer n);

    HashMap<String, Object> sandboxMemoryStats(Integer interval, Integer n, String request);

    HashMap<String, Object> usersPerSandboxStats(Integer interval, Integer n);

    HashMap<String, Object> sandboxesPerUserStats(Integer interval, Integer n);

}
