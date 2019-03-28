package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.UserLaunchCdsServiceEndpoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLaunchCdsServiceEndpointRepository extends CrudRepository<UserLaunchCdsServiceEndpoint, Integer> {

    UserLaunchCdsServiceEndpoint findByUserIdAndLaunchScenarioCdsServiceEndpointId(@Param("sbmUserId") String sbmUserId,
                                                                 @Param("launchScenarioCdsServiceEndpointId") int launchScenarioCdsServiceEndpointId);

    List<UserLaunchCdsServiceEndpoint> findByUserId(@Param("sbmUserId") String sbmUserId);

    List<UserLaunchCdsServiceEndpoint> findByLaunchScenarioCdsServiceEndpointId(@Param("launchScenarioCdsServiceEndpointId") int launchScenarioCdsServiceEndpointId);
}
