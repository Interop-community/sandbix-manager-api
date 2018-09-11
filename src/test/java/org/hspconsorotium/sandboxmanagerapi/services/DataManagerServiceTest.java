package org.hspconsorotium.sandboxmanagerapi.services;

import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.impl.DataMangerServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class DataManagerServiceTest {

    private SandboxService sandboxService = mock(SandboxService.class);
    private CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    private CloseableHttpResponse response = spy(CloseableHttpResponse.class);

    private DataMangerServiceImpl dataMangerService = new DataMangerServiceImpl();

    private Sandbox sandbox = new Sandbox();
    private String bearerToken = "bearerToken";
    private String endpoint = "endpoint";
    private String patientId = "patientId";
    private String fhirIdPrefix = "fhirIdPrefix";
    private StatusLine statusLine;

    @Before
    public void setup() throws IOException {
        statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!");
        when(httpClient.execute(any())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
    }

    @Test
    public void importPatientDataTest() throws UnsupportedEncodingException  {

        String returnedString = dataMangerService.importPatientData(sandbox, bearerToken, endpoint, patientId, fhirIdPrefix);
        assertEquals("SUCCESS", returnedString);
        verify(sandboxService).addSandboxImport(any(), any());
    }
}
