package org.hspconsortium.sandboxmanagerapi.repositories;

import lombok.NonNull;
import org.hspconsortium.sandboxmanagerapi.model.SmartApp;
import org.hspconsortium.sandboxmanagerapi.model.Visibility2;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmartAppRepository extends CrudRepository<SmartApp, String> {
    List<SmartApp> findByOwnerId(@Param("ownerId") @NonNull int ownerId);

    List<SmartApp> findByVisibility(@Param("ownerId") @NonNull Visibility2 visibility);
}
