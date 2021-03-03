package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.ContextParams;

public interface ContextParamsService {

    ContextParams save(final ContextParams contextParams);

    void delete(final int id);

    void delete(ContextParams contextParams);

}
