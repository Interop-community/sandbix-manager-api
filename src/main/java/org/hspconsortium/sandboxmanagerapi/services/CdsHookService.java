package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.CdsHook;
import org.hspconsortium.sandboxmanagerapi.model.CdsServiceEndpoint;
import org.hspconsortium.sandboxmanagerapi.model.Image;

import java.util.List;

public interface CdsHookService {

    CdsHook save(final CdsHook cdsHook);

    void delete(final int id);

    void delete(CdsHook cdsHook);

    CdsHook getById(final int id);

    CdsHook updateCdsHookImage(final CdsHook cdsHook, final Image image);

    CdsHook deleteCdsHookImage(final CdsHook cdsHook);

    CdsHook findByHookIdAndCdsServiceEndpointId(final String hookId, final int cdsServiceEndpointId);

    List<CdsHook> findByCdsServiceEndpointId(final int cdsServiceEndpointId);

}
