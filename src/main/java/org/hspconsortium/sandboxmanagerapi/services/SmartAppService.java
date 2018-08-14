package org.hspconsortium.sandboxmanagerapi.services;

import lombok.NonNull;
import org.hspconsortium.sandboxmanagerapi.model.Image;
import org.hspconsortium.sandboxmanagerapi.model.SmartApp;
import org.json.JSONObject;

import java.util.List;

public interface SmartAppService {

    SmartApp save(final SmartApp smartApp);

    void delete(final String smartAppId, final String sandboxId);

    void delete(final SmartApp smartApp);

    SmartApp getById(final String smartAppId, final String sandboxId);

    List<SmartApp> findByOwnerId(final int ownerId);

    List<SmartApp> findBySandboxId(final String sandboxId);

    List<SmartApp> findPublic();

    SmartApp updateAppImage(final SmartApp smartApp, final Image image);

}
