package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.UserLaunch;

import java.util.List;

public interface UserLaunchService {

    UserLaunch save(final UserLaunch userLaunch);

    void delete(final int id);

    void delete(final UserLaunch userLaunch);

    UserLaunch create(final UserLaunch userLaunch);

    UserLaunch update(final UserLaunch userLaunch);

    UserLaunch getById(final int id);

    UserLaunch findByUserIdAndLaunchScenarioId(final String sbmUserId, final int launchScenarioId);

    List<UserLaunch> findByUserId(final String sbmUserId);

    List<UserLaunch> findByLaunchScenarioId(final int launchScenarioId);

    List<UserLaunch> findByLaunchScenarioCdsServiceEndpointId(final int launchScenarioCdsServiceEndpointId);
}
