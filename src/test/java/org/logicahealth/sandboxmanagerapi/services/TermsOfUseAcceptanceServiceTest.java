package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.logicahealth.sandboxmanagerapi.repositories.TermsOfUseAcceptanceRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.TermsOfUseAcceptanceServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TermsOfUseAcceptanceServiceTest {

    private TermsOfUseAcceptanceRepository repository = mock(TermsOfUseAcceptanceRepository.class);

    private TermsOfUseAcceptanceServiceImpl termsOfUseAcceptanceService = new TermsOfUseAcceptanceServiceImpl(repository);

    private TermsOfUseAcceptance termsOfUseAcceptance;

    @Before
    public void setup() {
        termsOfUseAcceptance = new TermsOfUseAcceptance();
        termsOfUseAcceptance.setId(1);
    }

    @Test
    public void saveTest() {
        when(repository.save(termsOfUseAcceptance)).thenReturn(termsOfUseAcceptance);
        TermsOfUseAcceptance returnedTermsOfUseAcceptance = termsOfUseAcceptanceService.save(termsOfUseAcceptance);
        assertEquals(termsOfUseAcceptance, returnedTermsOfUseAcceptance);
    }

    @Test
    public void getByIdTest() {
        when(repository.findById(termsOfUseAcceptance.getId())).thenReturn(of(termsOfUseAcceptance));
        TermsOfUseAcceptance returnedTermsOfUseAcceptance = termsOfUseAcceptanceService.getById(termsOfUseAcceptance.getId());
        assertEquals(termsOfUseAcceptance, returnedTermsOfUseAcceptance);
    }
}
