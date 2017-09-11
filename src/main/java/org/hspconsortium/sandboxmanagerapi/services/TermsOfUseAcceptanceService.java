package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.TermsOfUseAcceptance;

public interface TermsOfUseAcceptanceService {

    TermsOfUseAcceptance save(final TermsOfUseAcceptance termsOfUseAcceptance);

    TermsOfUseAcceptance getById(final int id);

}

