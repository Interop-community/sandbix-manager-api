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

package org.hspconsortium.sandboxmanager.controllers;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.sandboxmanager.model.Role;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.model.UserRole;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.SandboxService;
import org.hspconsortium.sandboxmanager.services.UserRoleService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/REST/sandbox")
public class SandboxController {
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxController.class.getName());

    @Value("${hspc.platform.api.sandboxManagementEndpointURL}")
    private String sandboxManagementEndpointURL;

    @Value("${hspc.platform.api.oauthUserInfoEndpointURL}")
    private String oauthUserInfoEndpointURL;

    private final SandboxService sandboxService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final OAuthService oAuthUserService;

    @Inject
    public SandboxController(final SandboxService sandboxService, final UserService userService,
                             final OAuthService oAuthUserService, final UserRoleService userRoleService) {
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.oAuthUserService = oAuthUserService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces ="application/json")
    @Transactional
    public @ResponseBody Sandbox createSandbox(HttpServletRequest request, @RequestBody final Sandbox sandbox) throws UnsupportedEncodingException{

        Sandbox existingSandbox = sandboxService.findBySandboxId(sandbox.getSandboxId());
        if (existingSandbox != null) {
            return existingSandbox;
        }

        LOGGER.info("Creating sandbox: " + sandbox.getName());
        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        User user = userService.findByLdapId(sandbox.getCreatedBy().getLdapId());
        if (user == null) {
            user = userService.save(sandbox.getCreatedBy());
        } else if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(oAuthUserService.getOAuthUserName(request));
            userService.save(user);
        }

        sandbox.setCreatedBy(user);
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(new UserRole(user, Role.ADMIN));
        sandbox.setUserRoles(userRoles);

        String url = this.sandboxManagementEndpointURL + "/" + sandbox.getSandboxId();

        HttpPut putRequest = new HttpPut(url);
        putRequest.addHeader("Content-Type", "application/json");
        StringEntity entity;

        String jsonString = "{\"teamId\": \"" + sandbox.getSandboxId() + "\"}";
        entity = new StringEntity(jsonString);
        putRequest.setEntity(entity);
        putRequest.setHeader("Authorization", "BEARER " + oAuthUserService.getBearerToken(request));

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
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

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(putRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                String errorMsg = String.format("There was a problem creating the sandbox.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            Sandbox savedSandbox = sandboxService.save(sandbox);
            List<Sandbox> sandboxes = user.getSandboxes();
            sandboxes.add(savedSandbox);
            user.setSandboxes(sandboxes);
            userService.save(user);
            return savedSandbox;
        } catch (IOException e) {
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    @RequestMapping(method = RequestMethod.GET, params = {"lookUpId"})
    public @ResponseBody String checkForSandboxById(HttpServletResponse response, @RequestParam(value = "lookUpId")  String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        return (sandbox == null) ? null : sandbox.getSandboxId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces ="application/json")
    public @ResponseBody Sandbox getSandboxById(HttpServletRequest request, @PathVariable String id) {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
//        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        checkUserAuthorization(request, sandbox.getUserRoles());
        return sandbox;
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json", params = {"userId"})
    public @ResponseBody
    @SuppressWarnings("unchecked")
    List<Sandbox> getSandboxesByOwner(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, "UTF-8");
        checkUserAuthorization(request, userId);
        User user = userService.findByLdapId(userId);
        if (user != null) {
            return user.getSandboxes();
        }

        return Collections.EMPTY_LIST;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json", params = {"userId"})
    @Transactional
    public void removeSandboxUser(HttpServletRequest request, @PathVariable String id, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        Sandbox sandbox = sandboxService.findBySandboxId(id);
        //Only the Sandbox creater can remove a user right now
        checkUserAuthorization(request, sandbox.getCreatedBy().getLdapId());
        String removeUserId = java.net.URLDecoder.decode(userIdEncoded, "UTF-8");

        User user = userService.findByLdapId(removeUserId);
        if (user != null) {
            List<Sandbox> sandboxes = user.getSandboxes();
            sandboxes.remove(sandbox);
            user.setSandboxes(sandboxes);
            userService.save(user);
            List<UserRole> userRoles = sandbox.getUserRoles();
            Iterator<UserRole> iterator = userRoles.iterator();
            while (iterator.hasNext()) {
                UserRole userRole = iterator.next();
                if (userRole.getUser().getId().equals(user.getId())) {
                    userRoleService.delete(userRole.getId());
                    iterator.remove();
                }
            }
            sandbox.setUserRoles(userRoles);
            sandboxService.save(sandbox);
        }
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    @ResponseStatus(code = org.springframework.http.HttpStatus.UNAUTHORIZED)
    public void handleAuthorizationException(HttpServletResponse response, Exception e) throws IOException {
        response.getWriter().write(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.getWriter().write(e.getMessage());
    }

    private void checkUserAuthorization(HttpServletRequest request, String userId) {
        String oauthUserId = oAuthUserService.getOAuthUserId(request);

        if (!userId.equalsIgnoreCase(oauthUserId)) {
            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                    "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }

    private void checkUserAuthorization(HttpServletRequest request, List<UserRole> users) {
        String oauthUserId = oAuthUserService.getOAuthUserId(request);
        boolean userIsAuthorized = false;

        for(UserRole user : users) {
            if (user.getUser().getLdapId().equalsIgnoreCase(oauthUserId) && user.getRole() != Role.READONLY) {
                userIsAuthorized = true;
            }
        }

        if (!userIsAuthorized) {
            throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                            "Response Detail : User not authorized to perform this action."
                    , HttpStatus.SC_UNAUTHORIZED));
        }
    }

}
