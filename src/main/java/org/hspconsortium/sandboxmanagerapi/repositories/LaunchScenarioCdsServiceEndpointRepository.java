package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.LaunchScenarioCdsServiceEndpoint;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaunchScenarioCdsServiceEndpointRepository extends CrudRepository<LaunchScenarioCdsServiceEndpoint, Integer> {

    List<LaunchScenarioCdsServiceEndpoint> findBySandboxId(@Param("sandboxId") String sandboxId);

    List<LaunchScenarioCdsServiceEndpoint> findByCdsServiceEndpointIdAndSandboxId(@Param("cdsId") int cdsId,
                                                                                  @Param("sandboxId") String sandboxId);

    List<LaunchScenarioCdsServiceEndpoint> findByUserPersonaIdAndSandboxId(@Param("userPersonaId") int userPersonaId,
                                                         @Param("sandboxId") String sandboxId);

    List<LaunchScenarioCdsServiceEndpoint> findBySandboxIdAndCreatedByOrVisibility(@Param("sandboxId") String sandboxId,
                                                                 @Param("createdBy") String createdBy,
                                                                 @Param("visibility") Visibility visibility);

    List<LaunchScenarioCdsServiceEndpoint> findBySandboxIdAndCreatedBy(@Param("sandboxId") String sandboxId,
                                                     @Param("createdBy") String createdBy);

}