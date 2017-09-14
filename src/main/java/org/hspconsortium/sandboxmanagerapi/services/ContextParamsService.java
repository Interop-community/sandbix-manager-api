package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.ContextParams;

public interface ContextParamsService {

    ContextParams save(final ContextParams contextParams);

    void delete(final int id);

    void delete(ContextParams contextParams);

}
