package org.hspconsortium.sandboxmanagerapi.services;

public interface AnalyticsService {

    Integer countSandboxesByUser(final String userId);

    Double retrieveMemoryInSchema(String schemaName);
}
