package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

public interface SandboxRepository extends CrudRepository<Sandbox, Integer> {
    public Sandbox findBySandboxId(@Param("sandboxId") String sandboxId);
    public List<Sandbox> findByVisibility(@Param("visibility") Visibility visibility);
    public String fullCount();
    public String schemaCount(@Param("apiEndpointIndex") String apiEndpointIndex);
    public String intervalCount(@Param("intervalTime") Timestamp intervalTime);
}
