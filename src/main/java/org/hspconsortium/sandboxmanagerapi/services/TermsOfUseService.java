package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.TermsOfUse;

public interface TermsOfUseService {

    TermsOfUse save(final TermsOfUse termsOfUse);

    TermsOfUse getById(final int id);

    TermsOfUse mostRecent();

}

