package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.UserLaunch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLaunchRepository extends CrudRepository<UserLaunch, Integer> {
    UserLaunch findByUserIdAndLaunchScenarioId(@Param("sbmUserId") String sbmUserId,
                                                      @Param("launchScenarioId") int launchScenarioId);

    List<UserLaunch> findByUserId(@Param("sbmUserId") String sbmUserId);

    List<UserLaunch> findByLaunchScenarioId(@Param("launchScenarioId") int launchScenarioId);

    List<UserLaunch> findByLaunchScenarioCdsServiceEndpointId(@Param("launchScenarioCdsServiceEndpointId") int launchScenarioCdsServiceEndpointId);
}
