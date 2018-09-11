package org.hspconsorotium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.services.impl.OAuthServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuthServiceTest {

    private RestTemplate restTemplate = mock(RestTemplate.class);
    private HttpServletRequest request = mock(HttpServletRequest.class);

    private OAuthServiceImpl oAuthService = new OAuthServiceImpl();

    @Before
    public void setup() {
        oAuthService.setRestTemplate(restTemplate);
        ReflectionTestUtils.setField(oAuthService, "oauthUserInfoEndpointURL", "oauthUserInfoEndpointURL");
    }

    @Test
    public void getBearerTokenTest() {
        when(request.getHeader("Authorization")).thenReturn("auth-token");
        String returnedString = oAuthService.getBearerToken(request);
        assertEquals("ken", returnedString);
    }

    @Test
    public void getOAuthUserId() {
        oAuthService.getOAuthUserId(request);
    }

    @Test
    public void getOAuthUserName() {
        oAuthService.getOAuthUserName(request);
    }

    @Test
    public void getOAuthUserEmail() {
        oAuthService.getOAuthUserEmail(request);
    }
}
