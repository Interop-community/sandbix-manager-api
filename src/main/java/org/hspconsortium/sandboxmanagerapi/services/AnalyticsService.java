package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.FhirTransaction;
import org.hspconsortium.sandboxmanagerapi.model.User;

import java.util.HashMap;

public interface AnalyticsService {

    Integer countSandboxesByUser(final String userId);

    HashMap<String, Integer> countAppsPerSandboxByUser(User user);

    HashMap<String, Integer> countUsersPerSandboxByUser(User user);

    Integer countTransactionsByPayer(User payer);

    FhirTransaction handleFhirTransaction(User user, HashMap transactionInfo);

    Double retrieveTotalMemoryByUser(User user);

    Double retrieveMemoryInSchema(String schemaName);

    String getSandboxStatistics(final String intervalDays);

    String activeUserCount(Integer intervalDays);

}
