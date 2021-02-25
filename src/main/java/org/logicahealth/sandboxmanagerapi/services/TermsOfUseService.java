package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.TermsOfUse;

public interface TermsOfUseService {

    TermsOfUse save(final TermsOfUse termsOfUse);

    TermsOfUse getById(final int id);

    TermsOfUse mostRecent();

}

