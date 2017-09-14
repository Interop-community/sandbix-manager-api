package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.AuthClient;
import org.springframework.data.repository.CrudRepository;

public interface AuthClientRepository extends CrudRepository<AuthClient, Integer> {
}
