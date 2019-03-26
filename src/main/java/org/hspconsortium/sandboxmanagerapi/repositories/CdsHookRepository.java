package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.CdsHook;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdsHookRepository extends CrudRepository<CdsHook, Integer> {

    CdsHook findByLogoUriAndHookId(@Param("logoUri") String logoUri,
                                                   @Param("hookId") String hookId);

    List<CdsHook> findByHookId(@Param("hookId") String hookId);

}
