package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;

import java.util.List;

public interface AnalyticsService {

    Integer countSandboxesByUser(final String userId);

    List<Sandbox> sandboxesCreatedByUser(User user);

    Double retrieveMemoryInSchema(String schemaName);
}
