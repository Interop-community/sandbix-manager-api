package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.LaunchScenarioCdsServiceEndpoint;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;
import org.json.JSONObject;

import java.util.List;

public interface LaunchScenarioCdsServiceEndpointService {

    LaunchScenarioCdsServiceEndpoint save(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint);

    void delete(final int id);

    void delete(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint);

    LaunchScenarioCdsServiceEndpoint create(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint);

    LaunchScenarioCdsServiceEndpoint update(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint);

    LaunchScenarioCdsServiceEndpoint updateContext(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint, final JSONObject context);

    Iterable<LaunchScenarioCdsServiceEndpoint> findAll();

    LaunchScenarioCdsServiceEndpoint getById(final int id);

    List<LaunchScenarioCdsServiceEndpoint> findBySandboxId(final String sandboxId);

    List<LaunchScenarioCdsServiceEndpoint> findByCdsIdAndSandboxId(final int cdsId, final String sandboxId);

    List<LaunchScenarioCdsServiceEndpoint> findByUserPersonaIdAndSandboxId(final int userPersonaId, final String sandboxId);

    List<LaunchScenarioCdsServiceEndpoint> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility);

    List<LaunchScenarioCdsServiceEndpoint> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy);

    List<LaunchScenarioCdsServiceEndpoint> updateLastLaunchForCurrentUser(final List<LaunchScenarioCdsServiceEndpoint> launchScenarioCdsServiceEndpoint, final User user);
}
