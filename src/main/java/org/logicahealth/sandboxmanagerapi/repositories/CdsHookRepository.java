package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.CdsHook;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdsHookRepository extends CrudRepository<CdsHook, Integer> {

    CdsHook findByHookIdAndCdsServiceEndpointId(@Param("hookId") String hookId,
                                                @Param("cdsServiceEndpointId") int cdsServiceEndpointId);

    List<CdsHook> findByCdsServiceEndpointId(@Param("cdsServiceEndpointId") int cdsServiceEndpointId);

}
