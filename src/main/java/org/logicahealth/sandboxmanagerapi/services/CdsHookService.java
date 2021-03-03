package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.CdsHook;
import org.logicahealth.sandboxmanagerapi.model.Image;

import java.util.List;

public interface CdsHookService {

    CdsHook create(final CdsHook cdsHook);

    CdsHook save(final CdsHook cdsHook);

    CdsHook update(final CdsHook cdsHook);

    void delete(final int id);

    void delete(CdsHook cdsHook);

    CdsHook getById(final int id);

    CdsHook updateCdsHookImage(final CdsHook cdsHook, final Image image);

    CdsHook deleteCdsHookImage(final CdsHook cdsHook);

    CdsHook findByHookIdAndCdsServiceEndpointId(final String hookId, final int cdsServiceEndpointId);

    List<CdsHook> findByCdsServiceEndpointId(final int cdsServiceEndpointId);

}
