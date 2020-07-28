package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.UserRole;
import org.hspconsortium.sandboxmanagerapi.repositories.UserRoleRepository;
import org.hspconsortium.sandboxmanagerapi.services.UserRoleService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository repository;

    @Inject
    public UserRoleServiceImpl(final UserRoleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void delete(final int id){
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(final UserRole userRole){
        delete(userRole.getId());
    }

    @Override
    @Transactional
    public UserRole save(final UserRole userRole) {
        return repository.save(userRole);
    }

}
