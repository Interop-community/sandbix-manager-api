package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.UserSandbox;
import org.hspconsortium.sandboxmanagerapi.model.UserSandboxId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSandboxRepository extends CrudRepository<UserSandbox, UserSandboxId> {
    void deleteAllBySandboxId(Integer sandboxId);
}
