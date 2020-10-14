package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.repositories.ContextParamsRepository;
import org.hspconsortium.sandboxmanagerapi.model.ContextParams;
import org.hspconsortium.sandboxmanagerapi.services.impl.ContextParamsServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContextParamsServiceTest {

    private ContextParamsRepository contextParamsRepository = mock(ContextParamsRepository.class);

    private ContextParamsServiceImpl contextParamsService = new ContextParamsServiceImpl(contextParamsRepository);

    private ContextParams contextParams = new ContextParams();

    @Before
    public void setup() {
        contextParams.setId(1);
    }

    @Test
    public void saveTest() {
        when(contextParamsRepository.save(contextParams)).thenReturn(contextParams);
        ContextParams returnedContextParams = contextParamsService.save(contextParams);
        assertEquals(contextParams, returnedContextParams);
    }

    @Test
    public void deleteTest() {
        contextParamsService.delete(contextParams.getId());
        verify(contextParamsRepository).deleteById(contextParams.getId());
    }
}
