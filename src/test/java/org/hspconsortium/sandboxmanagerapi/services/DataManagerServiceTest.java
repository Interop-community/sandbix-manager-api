package org.hspconsortium.sandboxmanagerapi.services;

import org.apache.http.HttpEntity;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class DataManagerServiceTest {

    private SandboxService sandboxService = mock(SandboxService.class);
    private CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    private CloseableHttpResponse response = spy(CloseableHttpResponse.class);
    private HttpEntity httpEntity = mock(HttpEntity.class);
    private RestTemplate restTemplate = mock(RestTemplate.class);
    private ResponseEntity<String> responseEntity;

    private DataMangerServiceImpl dataMangerService = new DataMangerServiceImpl();

    private Sandbox sandbox = new Sandbox();
    private String bearerToken = "bearerToken";
    private String endpoint = "endpoint";
    private String patientId = "patientId";
    private String fhirIdPrefix = "fhirIdPrefix";
    private StatusLine statusLine;

    @Before
    public void setup() throws IOException {
        dataMangerService.setHttpClient(httpClient);
        dataMangerService.setSandboxService(sandboxService);
        dataMangerService.setRestTemplate(restTemplate);
//        statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!");
//        response.setEntity(httpEntity);
//        when(httpClient.execute(any())).thenReturn(response);
//        when(response.getStatusLine()).thenReturn(statusLine);

    }

    // No need to test importPatientData, it's not currently in use
//    @Test
//    public void importPatientDataTest() throws UnsupportedEncodingException  {
//        String returnedString = dataMangerService.importPatientData(sandbox, bearerToken, endpoint, patientId, fhirIdPrefix);
//        assertEquals("SUCCESS", returnedString);
//        verify(sandboxService).addSandboxImport(any(), any());
//    }

    @Test
    public void resetTest() throws UnsupportedEncodingException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(responseEntity);
        when(sandboxService.getSandboxApiURL(sandbox) + "/sandbox/reset").thenReturn("url");
        String success = dataMangerService.reset(sandbox, bearerToken);
        assertEquals("SUCCESS", success);
        verify(sandboxService).reset(sandbox, bearerToken);
    }

    @Test(expected = UnknownError.class)
    public void resetTestErrorInApiCall() throws UnsupportedEncodingException {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenThrow(HttpClientErrorException.class);
        when(sandboxService.getSandboxApiURL(sandbox) + "/sandbox/reset").thenReturn("url");
        dataMangerService.reset(sandbox, bearerToken);
    }


}
