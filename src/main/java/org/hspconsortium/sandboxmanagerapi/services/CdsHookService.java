package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.CdsHook;
import org.hspconsortium.sandboxmanagerapi.model.Image;

public interface CdsHookService {

    CdsHook save(final CdsHook cdsHook);

    void delete(final int id);

    void delete(CdsHook cdsHook);

    CdsHook getById(final int id);

    CdsHook updateCDSImage(final CdsHook cdsHook, final Image image);

    CdsHook deleteCDSImage(final CdsHook cdsHook);

}
