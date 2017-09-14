package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.UserRole;

public interface UserRoleService {

    void delete(final int id);

    void delete(final UserRole userRole);

    UserRole save(final UserRole userRole);

}
