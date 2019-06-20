package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.CustomHook;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomHookRepository extends CrudRepository<CustomHook, Integer> {

    List<CustomHook> findBySandboxId(@Param("sandboxId") String sandboxId);
}
