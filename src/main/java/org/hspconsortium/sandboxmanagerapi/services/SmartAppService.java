package org.hspconsortium.sandboxmanagerapi.services;

import lombok.NonNull;
import org.hspconsortium.sandboxmanagerapi.model.SmartApp;

import java.util.List;

public interface SmartAppService {

    SmartApp save(final SmartApp smartApp, final String performedBy);

    void delete(final String id, final String performedBy);

    void delete(final SmartApp smartApp, final String performedBy);

    SmartApp getById(final String id, final String performedBy);

    List<SmartApp> findByOwnerId(@NonNull final int ownerId, @NonNull final String performedBy);

    List<SmartApp> findPublic();

    SmartApp findById(final String id);

}
