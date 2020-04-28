package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.ConcurrentSandboxNames;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcurrentSandboxNamesRepository extends CrudRepository<String, Integer> {

    List<String> findAllSandboxName();

}
