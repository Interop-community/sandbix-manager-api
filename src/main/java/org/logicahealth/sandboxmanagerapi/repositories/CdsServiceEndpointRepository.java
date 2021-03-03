package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.CdsServiceEndpoint;
import org.logicahealth.sandboxmanagerapi.model.Visibility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CdsServiceEndpointRepository extends CrudRepository<CdsServiceEndpoint, Integer> {

    CdsServiceEndpoint findByCdsServiceEndpointUrlAndSandboxId(@Param("url") String url,
                                                      @Param("sandboxId") String sandboxId);

    List<CdsServiceEndpoint> findBySandboxId(@Param("sandboxId") String sandboxId);

    List<CdsServiceEndpoint> findBySandboxIdAndCreatedByOrVisibility(@Param("sandboxId") String sandboxId,
                                                      @Param("createdBy") String createdBy,
                                                      @Param("visibility") Visibility visibility);

    List<CdsServiceEndpoint> findBySandboxIdAndCreatedBy(@Param("sandboxId") String sandboxId,
                                                        @Param("createdBy") String createdBy);
}