package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.LaunchScenario;
import org.logicahealth.sandboxmanagerapi.model.Visibility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaunchScenarioRepository extends CrudRepository<LaunchScenario, Integer> {

    List<LaunchScenario> findBySandboxId(@Param("sandboxId") String sandboxId);

    List<LaunchScenario> findByAppIdAndSandboxId(@Param("appId") int appId,
                                                        @Param("sandboxId") String sandboxId);

    List<LaunchScenario> findByUserPersonaIdAndSandboxId(@Param("userPersonaId") int userPersonaId,
                                                                @Param("sandboxId") String sandboxId);

    List<LaunchScenario> findBySandboxIdAndCreatedByOrVisibility(@Param("sandboxId") String sandboxId,
                                                             @Param("createdBy") String createdBy,
                                                             @Param("visibility") Visibility visibility);

    List<LaunchScenario> findBySandboxIdAndCreatedBy(@Param("sandboxId") String sandboxId,
                                                                        @Param("createdBy") String createdBy);

    List<LaunchScenario> findByCdsHookIdAndSandboxId(@Param("cdsHookId") int cdsHookId,
                                                                @Param("sandboxId") String sandboxId);

}
