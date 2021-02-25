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

package org.logicahealth.sandboxmanagerapi.services.impl;

import org.springframework.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.logicahealth.sandboxmanagerapi.controllers.UnauthorizedException;
import org.logicahealth.sandboxmanagerapi.services.OAuthService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Service
public class OAuthServiceImpl implements OAuthService {
    private static Logger LOGGER = LoggerFactory.getLogger(OAuthServiceImpl.class.getName());

    @Value("${hspc.platform.api.oauthUserInfoEndpointURL}")
    String oauthUserInfoEndpointURL;

    private RestTemplate simpleRestTemplate;

    @Inject
    public void setRestTemplate(RestTemplate simpleRestTemplate) {
        this.simpleRestTemplate = simpleRestTemplate;
    }

    @Override
    public String getBearerToken(HttpServletRequest request) {

        String authToken = request.getHeader("Authorization");
        if (authToken == null) {
            return null;
        }
        return authToken.substring(7);
    }

    @Override
    public String getOAuthUserId(HttpServletRequest request) {
        try {
            JSONObject jsonObject = getOAuthUser(request);
            if (jsonObject != null) {
                return (String) jsonObject.get("sub");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String getOAuthUserName(HttpServletRequest request) {
        try {
            JSONObject jsonObject = getOAuthUser(request);
            if (jsonObject != null) {
                return (String) jsonObject.get("name");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String getOAuthUserEmail(HttpServletRequest request) {
        try {
            JSONObject jsonObject = getOAuthUser(request);
            if (jsonObject != null) {
                //TODO change to email when FireBase starts sending email
                return (String) jsonObject.get("preferred_username");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    private JSONObject getOAuthUser(HttpServletRequest request) {

        String authToken = getBearerToken(request);
        if (authToken == null) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "BEARER " + authToken);

        HttpEntity entity = new HttpEntity(headers);

        try {
            ResponseEntity<String> response = simpleRestTemplate.exchange(this.oauthUserInfoEndpointURL, HttpMethod.GET, entity, String.class);
            try {
                return new JSONObject(response.getBody());
            } catch (JSONException e) {
                LOGGER.error("JSON Error reading entity: " + entity, e);
                throw new RuntimeException(e);
            }
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 401) {
                throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                                "Response Detail : User not authorized to perform this action."
                        , HttpStatus.SC_UNAUTHORIZED));
            } else {
                throw new UnknownError("Error with Auth Server");
            }
        }

    }

}
