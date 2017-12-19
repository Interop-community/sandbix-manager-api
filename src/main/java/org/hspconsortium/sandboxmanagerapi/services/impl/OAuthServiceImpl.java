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


import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.sandboxmanagerapi.controllers.UnauthorizedException;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Service
public class OAuthServiceImpl implements OAuthService {
    private static Logger LOGGER = LoggerFactory.getLogger(OAuthServiceImpl.class.getName());

    @Value("${hspc.platform.api.oauthUserInfoEndpointURL}")
    String oauthUserInfoEndpointURL;

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
        } catch (JSONException e) {
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
        } catch (JSONException e) {
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
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    private JSONObject getOAuthUser(HttpServletRequest request) {

        String authToken = getBearerToken(request);
        if (authToken == null) {
            return null;
        }

        HttpGet getRequest = new HttpGet(this.oauthUserInfoEndpointURL);
        getRequest.setHeader("Authorization", "BEARER " + authToken);

        SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(getRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                                    "Response Detail : User not authorized to perform this action."
                            , HttpStatus.SC_UNAUTHORIZED));
                }
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                throw new RuntimeException(String.format("Response Status : %s .%nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }

            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            String entity = IOUtils.toString(httpEntity.getContent());
            try {
                return new JSONObject(entity);
            } catch (JSONException e) {
                LOGGER.error("JSON Error reading entity: " + entity, e);
                throw new RuntimeException(e);
            }
        } catch (IOException io_ex) {
            LOGGER.error("Error on HTTP GET", io_ex);
            throw new RuntimeException(io_ex);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient", e);
            }
        }
    }

}
