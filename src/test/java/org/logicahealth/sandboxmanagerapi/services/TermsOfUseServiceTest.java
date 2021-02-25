package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.TermsOfUse;
import org.logicahealth.sandboxmanagerapi.repositories.TermsOfUseRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.TermsOfUseServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TermsOfUseServiceTest {

    private TermsOfUseRepository repository = mock(TermsOfUseRepository.class);

    private TermsOfUseServiceImpl termsOfUseService = new TermsOfUseServiceImpl(repository);

    private TermsOfUse termsOfUse;

    @Before
    public void setup() {
        termsOfUse = new TermsOfUse();
        termsOfUse.setId(1);
    }

    @Test
    public void saveTest() {
        when(repository.save(termsOfUse)).thenReturn(termsOfUse);
        TermsOfUse returnedTermsOfUse = termsOfUseService.save(termsOfUse);
        assertEquals(termsOfUse, returnedTermsOfUse);
    }

    @Test
    public void getByIdTest() {
        when(repository.findById(termsOfUse.getId())).thenReturn(of(termsOfUse));
        TermsOfUse returnedTermsOfUse = termsOfUseService.getById(termsOfUse.getId());
        assertEquals(termsOfUse, returnedTermsOfUse);
    }

    @Test
    public void mostRecentTest() {
        List<TermsOfUse> termsOfUses = new ArrayList<>();
        termsOfUses.add(termsOfUse);
        when(repository.orderByCreatedTimestamp()).thenReturn(termsOfUses);
        TermsOfUse returnedTermsOfUse = termsOfUseService.mostRecent();
        assertEquals(termsOfUse, returnedTermsOfUse);
    }

    @Test
    public void mostRecentTestNoneFound() {
        when(repository.orderByCreatedTimestamp()).thenReturn(new ArrayList<>());
        TermsOfUse returnedTermsOfUse = termsOfUseService.mostRecent();
        assertNull(returnedTermsOfUse);
    }

    @Test
    public void mostRecentTestNullList() {
        when(repository.orderByCreatedTimestamp()).thenReturn(null);
        TermsOfUse returnedTermsOfUse = termsOfUseService.mostRecent();
        assertNull(returnedTermsOfUse);
    }
}
