package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.UserLaunchCdsServiceEndpoint;

import java.util.List;

public interface UserLaunchCdsServiceEndpointService {

    UserLaunchCdsServiceEndpoint save(final UserLaunchCdsServiceEndpoint userLaunchCdsServiceEndpoint);

    void delete(final int id);

    void delete(final UserLaunchCdsServiceEndpoint userLaunchCdsServiceEndpoint);

    UserLaunchCdsServiceEndpoint create(final UserLaunchCdsServiceEndpoint userLaunchCdsServiceEndpoint);

    UserLaunchCdsServiceEndpoint update(final UserLaunchCdsServiceEndpoint userLaunchCdsServiceEndpoint);

    UserLaunchCdsServiceEndpoint getById(final int id);

    UserLaunchCdsServiceEndpoint findByUserIdAndLaunchScenarioCdsServiceEndpointId(final String sbmUserId, final int launchScenarioCdsServiceEndpointId);

    List<UserLaunchCdsServiceEndpoint> findByUserId(final String sbmUserId);

    List<UserLaunchCdsServiceEndpoint> findByLaunchScenarioCdsServiceEndpointId(final int launchScenarioCdsServiceEndpointId);
}
