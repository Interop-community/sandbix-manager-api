package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.UserRole;

public interface UserRoleService {

    void delete(final int id);

    void delete(final UserRole userRole);

    UserRole save(final UserRole userRole);

}
