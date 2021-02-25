package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.TermsOfUseAcceptance;

public interface TermsOfUseAcceptanceService {

    TermsOfUseAcceptance save(final TermsOfUseAcceptance termsOfUseAcceptance);

    TermsOfUseAcceptance getById(final int id);

}

