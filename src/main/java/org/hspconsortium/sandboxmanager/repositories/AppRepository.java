package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.App;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.Visibility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppRepository extends CrudRepository<App, Integer> {
    public App findByLaunchUriAndClientIdAndSandboxId(@Param("launchUri") String launchUri,
                                                      @Param("clientId") String clientId,
                                                      @Param("sandboxId") String sandboxId);

    public List<App> findBySandboxId(@Param("sandboxId") String sandboxId);

    public List<App> findBySandboxIdAndCreatedByOrVisibility(@Param("sandboxId") String sandboxId,
                                                             @Param("createdBy") String createdBy,
                                                             @Param("visibility") Visibility visibility);

    public List<App> findBySandboxIdAndCreatedBy(@Param("sandboxId") String sandboxId,
                                                             @Param("createdBy") String createdBy);
}
