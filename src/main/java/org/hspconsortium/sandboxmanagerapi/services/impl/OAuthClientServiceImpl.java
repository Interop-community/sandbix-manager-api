/*
 * #%L
 *
 * %%
 * Copyright (C) 2014 - 2015 Healthcare Services Platform Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.services.OAuthClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class OAuthClientServiceImpl implements OAuthClientService {

    @Value("${hspc.platform.api.oauthClientEndpointURL}")
    String oauthClientEndpointURL;

    @Autowired
    private OAuth2RestOperations restTemplate;

    @Override
    public String postOAuthClient(String clientJSON) {

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(clientJSON, requestHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(oauthClientEndpointURL, HttpMethod.POST, requestEntity, String.class);
        return responseEntity.getBody();
    }

    @Override
    public String putOAuthClient(Integer id, String clientJSON) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(clientJSON, requestHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(oauthClientEndpointURL + "/" + id, HttpMethod.PUT, requestEntity, String.class);
        return responseEntity.getBody();
    }

    @Override
    public String getOAuthClient(Integer id) {

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(oauthClientEndpointURL + "/" + id, HttpMethod.GET, requestEntity, String.class);
        return responseEntity.getBody();
    }

    @Override
    public void deleteOAuthClient(Integer id) {
        restTemplate.delete(oauthClientEndpointURL + "/" + id);
    }
}
