package org.logicahealth.sandboxmanagerapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

@EnableOAuth2Client
@Configuration
public class OicClientConfig {
    @Value("${hspc.platform.authorization.authorizeEndpoint}")
    private String authorizeUrl;
    @Value("${hspc.platform.authorization.tokenEndpoint}")
    private String tokenUrl;
    @Value("${hspc.platform.authorization.adminAccess.clientId}")
    private String clientId;
    @Value("${hspc.platform.authorization.adminAccess.clientSecret}")
    private String clientSecret;


    @Bean
    protected OAuth2ProtectedResourceDetails resource() {

        ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();

        resource.setAccessTokenUri(tokenUrl);
        resource.setClientId(clientId);
        resource.setClientSecret(clientSecret);

        return resource;
    }

    @Bean
    public OAuth2RestOperations restTemplate() {
        AccessTokenRequest atr = new DefaultAccessTokenRequest();
        return new OAuth2RestTemplate(resource(), new DefaultOAuth2ClientContext(atr));
    }
}
