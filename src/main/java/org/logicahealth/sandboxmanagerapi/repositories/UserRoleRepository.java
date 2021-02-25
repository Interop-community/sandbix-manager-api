package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, Integer> {

}
