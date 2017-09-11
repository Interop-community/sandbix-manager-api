package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LaunchScenarioRepository extends CrudRepository<LaunchScenario, Integer> {
    public List<LaunchScenario> findBySandboxId(@Param("sandboxId") String sandboxId);

    public List<LaunchScenario> findByAppIdAndSandboxId(@Param("appId") int appId,
                                                        @Param("sandboxId") String sandboxId);

    public List<LaunchScenario> findByUserPersonaIdAndSandboxId(@Param("userPersonaId") int userPersonaId,
                                                                @Param("sandboxId") String sandboxId);

    public List<LaunchScenario> findBySandboxIdAndCreatedByOrVisibility(@Param("sandboxId") String sandboxId,
                                                             @Param("createdBy") String createdBy,
                                                             @Param("visibility") Visibility visibility);

    public List<LaunchScenario> findBySandboxIdAndCreatedBy(@Param("sandboxId") String sandboxId,
                                                                        @Param("createdBy") String createdBy);

}
