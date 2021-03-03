package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.services.impl.OAuthClientServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OAuthClientServiceTest {

    private OAuth2RestOperations restTemplate = mock(OAuth2RestOperations.class);
    private ResponseEntity<String> responseEntity;

    private OAuthClientServiceImpl oAuthClientService = new OAuthClientServiceImpl();

    @Before
    public void setup() {
        oAuthClientService.setRestTemplate(restTemplate);
        ReflectionTestUtils.setField(oAuthClientService, "oauthClientEndpointURL", "oauthClientEndpointURL");
        responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Test
    public void postOAuthClientTest() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(responseEntity);
        oAuthClientService.postOAuthClient("clientJSON");
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
    }

    @Test
    public void putOAuthClientWithClientIdTest() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(String.class))).thenReturn(responseEntity);
        oAuthClientService.putOAuthClientWithClientId("clientId", "clientJSON");
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.PUT), any(), eq(String.class));
    }

    @Test
    public void getOAuthClientWithClientIdTest() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity);
        oAuthClientService.getOAuthClientWithClientId("clientId");
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class));
    }

    @Test
    public void deleteOAuthClientWithClientIdTest() {
        oAuthClientService.deleteOAuthClientWithClientId("clientId");
        verify(restTemplate).delete("oauthClientEndpointURL?clientId=clientId");
    }
}
