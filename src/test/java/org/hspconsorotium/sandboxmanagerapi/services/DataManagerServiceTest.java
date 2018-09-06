package org.hspconsorotium.sandboxmanagerapi.services;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.impl.DataMangerServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import static org.mockito.Mockito.*;

public class DataManagerServiceTest {

    private DataMangerServiceImpl dataMangerService = spy(DataMangerServiceImpl.class);

    private SandboxService sandboxService = mock(SandboxService.class);

    private Sandbox sandbox = new Sandbox();
    private String bearerToken = "bearerToken";
    private String endpoint = "endpoint";
    private String patientId = "patientId";
    private String fhirIdPrefix = "fhirIdPrefix";

    @Before
    public void setup() {
    }

//    @Test
//    public void importPatientDataTest() throws UnsupportedEncodingException  {
//        doReturn("SUCCESS").when(dataMangerService).getEverythingForPatient(any(), anyString(), anyString(), anyString(), anyString(), anyString())
//        dataMangerService.importPatientData(sandbox, bearerToken, endpoint, patientId, fhirIdPrefix);
//        verify(sandboxService).addSandboxImport(any(), any());
//    }
}
