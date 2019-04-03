package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.CdsHook;
import org.hspconsortium.sandboxmanagerapi.model.CdsServiceEndpoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdsHookRepository extends CrudRepository<CdsHook, Integer> {

    CdsHook findByHookIdAndCdsServiceEndpointId(@Param("hookId") String hookId,
                                                @Param("cdsServiceEndpointId") int cdsServiceEndpointId);
}
