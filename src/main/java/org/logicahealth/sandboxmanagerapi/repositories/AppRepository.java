package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.App;
import org.logicahealth.sandboxmanagerapi.model.Visibility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppRepository extends CrudRepository<App, Integer> {
    App findByLaunchUriAndClientIdAndSandboxId(@Param("launchUri") String launchUri,
                                                      @Param("clientId") String clientId,
                                                      @Param("sandboxId") String sandboxId);

    List<App> findBySandboxId(@Param("sandboxId") String sandboxId);

    List<App> findBySandboxIdIncludingCustomApps(@Param("sandboxId") String sandboxId);

    List<App> findBySandboxIdAndCreatedByOrVisibility(@Param("sandboxId") String sandboxId,
                                                             @Param("createdBy") String createdBy,
                                                             @Param("visibility") Visibility visibility);

    List<App> findBySandboxIdAndCreatedBy(@Param("sandboxId") String sandboxId,
                                                             @Param("createdBy") String createdBy);
}
