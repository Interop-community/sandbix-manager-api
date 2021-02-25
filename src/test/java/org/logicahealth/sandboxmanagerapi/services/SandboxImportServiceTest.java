package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.SandboxImport;
import org.logicahealth.sandboxmanagerapi.repositories.SandboxImportRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.SandboxImportServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SandboxImportServiceTest {

    private SandboxImportRepository repository = mock(SandboxImportRepository.class);

    private SandboxImportServiceImpl sandboxImportService = new SandboxImportServiceImpl(repository);

    private SandboxImport sandboxImport;

    @Before
    public void setup() {
        sandboxImport = new SandboxImport();
        sandboxImport.setId(1);
    }

    @Test
    public void saveTest() {
        when(repository.save(sandboxImport)).thenReturn(sandboxImport);
        SandboxImport returnedSandboxImport = sandboxImportService.save(sandboxImport);
        assertEquals(sandboxImport, returnedSandboxImport);
    }

    @Test
    public void deleteTest() {
        sandboxImportService.delete(sandboxImport.getId());
        verify(repository).deleteById(sandboxImport.getId());
    }
}
