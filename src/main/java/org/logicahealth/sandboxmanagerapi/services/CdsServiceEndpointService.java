package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.CdsServiceEndpoint;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.Visibility;

import java.util.List;

public interface CdsServiceEndpointService {

    CdsServiceEndpoint save(final CdsServiceEndpoint cds);

    void delete(final int id);

    void delete(final CdsServiceEndpoint cdsServiceEndpoint);

    List<CdsServiceEndpoint> create(final CdsServiceEndpoint cdsServiceEndpoint, final Sandbox sandbox);

    CdsServiceEndpoint update(final CdsServiceEndpoint cdsServiceEndpoint);

    CdsServiceEndpoint getById(final int id);

    List<CdsServiceEndpoint> findBySandboxId(final String sandboxId);

    List<CdsServiceEndpoint> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility);

    List<CdsServiceEndpoint> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy);

    CdsServiceEndpoint findByCdsServiceEndpointUrlAndSandboxId(final String url, final String sandboxId);

}
