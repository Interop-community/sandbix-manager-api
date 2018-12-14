package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.Visibility;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface SandboxRepository extends CrudRepository<Sandbox, Integer> {
    Sandbox findBySandboxId(@Param("sandboxId") String sandboxId);
    List<Sandbox> findByVisibility(@Param("visibility") Visibility visibility);
    List<Sandbox> findByPayerUserId(@Param("payerId") Integer payerId);
    String fullCount();
    String schemaCount(@Param("apiEndpointIndex") String apiEndpointIndex);
    String intervalCount(@Param("intervalTime") Timestamp intervalTime);
    String newDSTU2SandboxesInIntervalCount(@Param("intervalTime") Timestamp intervalTime);
    String newSTU3SandboxesInIntervalCount(@Param("intervalTime") Timestamp intervalTime);
    String newR4SandboxesInIntervalCount(@Param("intervalTime") Timestamp intervalTime);
}
