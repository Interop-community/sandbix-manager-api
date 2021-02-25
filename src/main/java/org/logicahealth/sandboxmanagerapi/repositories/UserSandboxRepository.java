package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.UserSandbox;
import org.logicahealth.sandboxmanagerapi.model.UserSandboxId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSandboxRepository extends CrudRepository<UserSandbox, UserSandboxId> {
    void deleteAllBySandboxId(Integer sandboxId);
}
