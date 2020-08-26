package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxCreationStatus;
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
    String fullCountForSpecificTimePeriod(@Param("endDate") Timestamp endDate);
    String schemaCount(@Param("apiEndpointIndex") String apiEndpointIndex);
    String schemaCountForSpecificTimePeriod(@Param("apiEndpointIndex") String apiEndpointIndex,
                                            @Param("endDate") Timestamp endDate);
    String intervalCount(@Param("intervalTime") Timestamp intervalTime);
    String newSandboxesInIntervalCount(@Param("intervalTime") Timestamp intervalTime,
                                       @Param("apiEndpointIndex") String apiEndpointIndex);
    String newSandboxesInIntervalCountForSpecificTimePeriod(@Param("apiEndpointIndex") String apiEndpointIndex,
                                                            @Param("beginDate") Timestamp beginDate,
                                                            @Param("endDate") Timestamp endDate);
    String intervalCountForSpecificTimePeriod(@Param("beginDate") Timestamp beginDate,
                                              @Param("endDate") Timestamp endDate);
    List<Sandbox> findByCreationStatusOrderByCreatedTimestampAsc(@Param("creationStatus") SandboxCreationStatus creationStatus);
    List<Sandbox> findByCreationStatus(@Param("creationStatus") SandboxCreationStatus creationStatus);
}
