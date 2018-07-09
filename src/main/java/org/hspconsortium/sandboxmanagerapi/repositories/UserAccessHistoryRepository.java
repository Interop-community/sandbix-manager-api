package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.UserAccessHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAccessHistoryRepository extends CrudRepository<UserAccessHistory, Integer> {
    List<UserAccessHistory> findBySandboxIdAndUserId(@Param("sandboxId") Integer sandboxId, @Param("userId") Integer userId);
}
