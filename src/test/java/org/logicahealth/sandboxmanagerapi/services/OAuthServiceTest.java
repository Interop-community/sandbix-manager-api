package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.services.impl.OAuthServiceImpl;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuthServiceTest {

    private RestTemplate restTemplate = mock(RestTemplate.class);
    private HttpServletRequest request = mock(HttpServletRequest.class);
    private ResponseEntity<String> responseEntity;
    private String jsonString;

    private OAuthServiceImpl oAuthService = new OAuthServiceImpl();

    @Before
    public void setup() {
        oAuthService.setRestTemplate(restTemplate);
        ReflectionTestUtils.setField(oAuthService, "oauthUserInfoEndpointURL", "oauthUserInfoEndpointURL");
        jsonString = "{\"sub\": \"userId\", \"name\": \"userName\", \"preferred_username\": \"user@email.com\"}";
        responseEntity = new ResponseEntity<>(jsonString, HttpStatus.ACCEPTED);
        when(request.getHeader("Authorization")).thenReturn("Bearer auth-token");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity);
    }

    @Test
    public void getBearerTokenTest() {
        String returnedString = oAuthService.getBearerToken(request);
        assertEquals("auth-token", returnedString);
    }

    @Test
    public void getBearerTokenTestReturnsNull() {
        when(request.getHeader("Authorization")).thenReturn(null);
        String returnedString = oAuthService.getBearerToken(request);
        assertNull(returnedString);
    }

    @Test
    public void getOAuthUserIdTest() {
        String ouAuthUserId = oAuthService.getOAuthUserId(request);
        assertEquals("userId", ouAuthUserId);
    }

    @Test(expected = JSONException.class)
    public void getOAuthUserIdTestSubNotFound() {
        responseEntity = new ResponseEntity<>("{}", HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity);
        String ouAuthUserId = oAuthService.getOAuthUserId(request);
        assertNull(ouAuthUserId);
    }

    @Test(expected = RuntimeException.class)
    public void getOAuthUserIdTestJSONObjectNotParsed() {
        responseEntity = new ResponseEntity<>("not-object", HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity);
        oAuthService.getOAuthUserId(request);
    }

    @Test(expected = UnknownError.class)
    public void getOAuthUserIdTestErrorInApiCall() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenThrow(HttpClientErrorException.class);
        oAuthService.getOAuthUserId(request);
    }

    @Test
    public void getOAuthUserNameTest() {
        String ouAuthUserName = oAuthService.getOAuthUserName(request);
        assertEquals("userName", ouAuthUserName);
    }

    @Test(expected = JSONException.class)
    public void getOAuthUserNameTestNameNotFound() {
        responseEntity = new ResponseEntity<>("{}", HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity);
        String ouAuthUserId = oAuthService.getOAuthUserName(request);
        assertNull(ouAuthUserId);
    }

    @Test(expected = RuntimeException.class)
    public void getOAuthUserNameTestJSONObjectNotParsed() {
        responseEntity = new ResponseEntity<>("not-object", HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity);
        oAuthService.getOAuthUserName(request);
    }

    @Test
    public void getOAuthUserEmailTest() {
        String ouAuthUserEmail = oAuthService.getOAuthUserEmail(request);
        assertEquals("user@email.com", ouAuthUserEmail);
    }

    @Test(expected = JSONException.class)
    public void getOAuthUserEmailTestEmailNotFound() {
        responseEntity = new ResponseEntity<>("{}", HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity);
        String ouAuthUserId = oAuthService.getOAuthUserEmail(request);
        assertNull(ouAuthUserId);
    }

    @Test(expected = RuntimeException.class)
    public void getOAuthUserEmailTestJSONObjectNotParsed() {
        responseEntity = new ResponseEntity<>("not-object", HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity);
        oAuthService.getOAuthUserEmail(request);
    }
}
