package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.UserAccessHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAccessHistoryRepository extends CrudRepository<UserAccessHistory, Integer> {
    List<UserAccessHistory> findBySandboxId(@Param("sandboxId") String sandboxId);
    List<UserAccessHistory> findBySbmUserId(@Param("sbmUserId") String sbmUserId);
    List<UserAccessHistory> findBySbmUserIdAndSandboxId(@Param("sbmUserId") String sbmUserId, @Param("sandboxId") String sandboxId);
}
