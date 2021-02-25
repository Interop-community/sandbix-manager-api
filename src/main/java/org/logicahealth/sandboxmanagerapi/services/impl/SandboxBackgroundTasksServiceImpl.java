package org.logicahealth.sandboxmanagerapi.services.impl;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.SandboxCreationStatus;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.repositories.SandboxRepository;
import org.logicahealth.sandboxmanagerapi.services.SandboxBackgroundTasksService;
import org.logicahealth.sandboxmanagerapi.services.UserAccessHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
public class SandboxBackgroundTasksServiceImpl implements SandboxBackgroundTasksService {

    private final CloseableHttpClient httpClient;
    private final SandboxRepository repository;
    private final UserAccessHistoryService userAccessHistoryService;

    private static Logger LOGGER = LoggerFactory.getLogger(SandboxBackgroundTasksServiceImpl.class.getName());

    @Autowired
    public SandboxBackgroundTasksServiceImpl(CloseableHttpClient httpClient, SandboxRepository repository, UserAccessHistoryService userAccessHistoryService) {
        this.httpClient = httpClient;
        this.repository = repository;
        this.userAccessHistoryService = userAccessHistoryService;
    }

    @Override
    @Transactional
    @Async("sandboxCloneTaskExecutor")
    public void cloneSandboxSchema(final Sandbox newSandbox, final Sandbox clonedSandbox, final User user, final String bearerToken, final String sandboxApiURL) throws UnsupportedEncodingException {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        String url = sandboxApiURL + "/sandbox/clone";

        // TODO: change to using 'simpleRestTemplate'
        HttpPut cloneRequest = new HttpPut(url);
        cloneRequest.addHeader("Content-Type", "application/json");
        StringEntity entity;

        String jsonString = "{\"newSandbox\": {" +
                "\"teamId\": \"" + newSandbox.getSandboxId() +
                "\",\"allowOpenAccess\": \"" + newSandbox.isAllowOpenAccess() + "\"" +
                "}," +
                "\"clonedSandbox\": {" +
                "\"teamId\": \"" + clonedSandbox.getSandboxId() +
                "\",\"allowOpenAccess\": \"" + clonedSandbox.isAllowOpenAccess() + "\"" +
                "}" +
                "}";
        entity = new StringEntity(jsonString);
        cloneRequest.setEntity(entity);
        cloneRequest.setHeader("Authorization", "BEARER " + bearerToken);

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(cloneRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                String errorMsg = String.format("There was a problem cloning the sandbox.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                updateSandboxCreationStatus(newSandbox, SandboxCreationStatus.ERRORED);
                throw new RuntimeException(errorMsg);
            }
            this.userAccessHistoryService.saveUserAccessInstance(newSandbox, user);
            updateSandboxCreationStatus(newSandbox, SandboxCreationStatus.CREATED);
        } catch (IOException e) {
            updateSandboxCreationStatus(newSandbox, SandboxCreationStatus.ERRORED);
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        }
    }

    private void updateSandboxCreationStatus(Sandbox newSandbox, SandboxCreationStatus status) {
        newSandbox = repository.findBySandboxId(newSandbox.getSandboxId());
        newSandbox.setCreationStatus(status);
        this.repository.save(newSandbox);
    }

}
