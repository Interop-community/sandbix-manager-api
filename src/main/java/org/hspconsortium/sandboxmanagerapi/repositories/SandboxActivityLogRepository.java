package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.SandboxActivity;
import org.hspconsortium.sandboxmanagerapi.model.SandboxActivityLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface SandboxActivityLogRepository extends CrudRepository<SandboxActivityLog, Integer> {
    public List<SandboxActivityLog> findByUserSbmUserId(@Param("sbmUserId") String sbmUserId);
    public List<SandboxActivityLog> findByUserId(@Param("userId") int UserId);
    public List<SandboxActivityLog> findBySandboxId(@Param("sandboxId") String sandboxId);
    public List<SandboxActivityLog> findBySandboxActivity(@Param("sandboxActivity") SandboxActivity sandboxActivity);
    public String intervalActive(@Param("intervalTime") Timestamp intervalTime);
}
