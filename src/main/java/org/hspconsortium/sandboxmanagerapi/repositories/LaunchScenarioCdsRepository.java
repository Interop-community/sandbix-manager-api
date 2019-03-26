package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.LaunchScenarioCds;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaunchScenarioCdsRepository extends CrudRepository<LaunchScenarioCds, Integer> {

    List<LaunchScenarioCds> findBySandboxId(@Param("sandboxId") String sandboxId);

    List<LaunchScenarioCds> findByCdsIdAndSandboxId(@Param("cdsId") int cdsId,
                                                 @Param("sandboxId") String sandboxId);

    List<LaunchScenarioCds> findByUserPersonaIdAndSandboxId(@Param("userPersonaId") int userPersonaId,
                                                         @Param("sandboxId") String sandboxId);

    List<LaunchScenarioCds> findBySandboxIdAndCreatedByOrVisibility(@Param("sandboxId") String sandboxId,
                                                                 @Param("createdBy") String createdBy,
                                                                 @Param("visibility") Visibility visibility);

    List<LaunchScenarioCds> findBySandboxIdAndCreatedBy(@Param("sandboxId") String sandboxId,
                                                     @Param("createdBy") String createdBy);

}