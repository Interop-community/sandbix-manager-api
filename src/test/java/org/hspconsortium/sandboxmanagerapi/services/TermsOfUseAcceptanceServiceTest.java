package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.hspconsortium.sandboxmanagerapi.repositories.TermsOfUseAcceptanceRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.TermsOfUseAcceptanceServiceImpl;
import org.junit.Before;
import org.junit.Test;

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
        when(repository.findOne(termsOfUseAcceptance.getId())).thenReturn(termsOfUseAcceptance);
        TermsOfUseAcceptance returnedTermsOfUseAcceptance = termsOfUseAcceptanceService.getById(termsOfUseAcceptance.getId());
        assertEquals(termsOfUseAcceptance, returnedTermsOfUseAcceptance);
    }
}
