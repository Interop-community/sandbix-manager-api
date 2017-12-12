package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.UserLaunch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserLaunchRepository extends CrudRepository<UserLaunch, Integer> {
    UserLaunch findByUserIdAndLaunchScenarioId(@Param("sbmUserId") String sbmUserId,
                                                      @Param("launchScenarioId") int launchScenarioId);

    List<UserLaunch> findByUserId(@Param("sbmUserId") String sbmUserId);

    List<UserLaunch> findByLaunchScenarioId(@Param("launchScenarioId") int launchScenarioId);
}
