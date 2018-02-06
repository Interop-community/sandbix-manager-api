package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.ConfigType;
import org.hspconsortium.sandboxmanagerapi.model.Config;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigRepository extends CrudRepository<Config, Integer> {
    List<Config> findByConfigType(@Param("configType") ConfigType configType);
}
