package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.SandboxActivity;
import org.hspconsortium.sandboxmanagerapi.model.SandboxActivityLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface SandboxActivityLogRepository extends CrudRepository<SandboxActivityLog, Integer> {
    List<SandboxActivityLog> findByUserSbmUserId(@Param("sbmUserId") String sbmUserId);
    List<SandboxActivityLog> findByUserId(@Param("userId") int UserId);
    List<SandboxActivityLog> findBySandboxId(@Param("sandboxId") String sandboxId);
    List<SandboxActivityLog> findBySandboxActivity(@Param("sandboxActivity") SandboxActivity sandboxActivity);
    String intervalActive(@Param("intervalTime") Timestamp intervalTime);
}
