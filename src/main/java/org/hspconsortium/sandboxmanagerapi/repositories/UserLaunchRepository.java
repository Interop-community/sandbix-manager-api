package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.UserLaunch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserLaunchRepository extends CrudRepository<UserLaunch, Integer> {
    public UserLaunch findByUserIdAndLaunchScenarioId(@Param("sbmUserId") String sbmUserId,
                                                      @Param("launchScenarioId") int launchScenarioId);

    public List<UserLaunch> findByUserId(@Param("sbmUserId") String sbmUserId);

    public List<UserLaunch> findByLaunchScenarioId(@Param("launchScenarioId") int launchScenarioId);
}
