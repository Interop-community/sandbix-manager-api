package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.services.*;
import org.json.JSONArray;
import org.json.simple.parser.JSONParser;
import org.hspconsortium.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.AppRepository;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AppServiceImpl implements AppService {
    public static final String JSON_ERROR_READING_ENTITY = "JSON Error reading entity: {}";
    private static Logger LOGGER = LoggerFactory.getLogger(AppServiceImpl.class.getName());

    private final AppRepository repository;
    private AuthClientService authClientService;
    private ImageService imageService;
    private OAuthClientService oAuthClientService;
    private ResourceLoader resourceLoader;
    private RuleService ruleService;
    private LaunchScenarioService launchScenarioService;
    private UserLaunchService userLaunchService;
    private SandboxService sandboxService;

    @Inject
    public AppServiceImpl(final AppRepository repository) {
        this.repository = repository;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Inject
    public void setAuthClientService(AuthClientService authClientService) {
        this.authClientService = authClientService;
    }

    @Inject
    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    @Inject
    public void setoAuthClientService(OAuthClientService oAuthClientService) {
        this.oAuthClientService = oAuthClientService;
    }

    @Inject
    public void setLaunchScenarioService(LaunchScenarioService launchScenarioService) {
        this.launchScenarioService = launchScenarioService;
    }

    @Inject
    public void setUserLaunchService(UserLaunchService userLaunchService) {
        this.userLaunchService = userLaunchService;
    }

    @Inject
    public void setLaunchScenarioServices(LaunchScenarioService launchScenarioService, UserLaunchService userLaunchService) {
        this.launchScenarioService = launchScenarioService;
    }

    @Inject
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Override
    @Transactional
    public App save(final App app) {
        return repository.save(app);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final App app) {

        //Integer authDatabaseId = app.getAuthClient().getAuthDatabaseId();
        if (app.getCopyType() == CopyType.MASTER) {
            try {
                // TODO: don't delete in auth server if default app
//                JSONArray apps = getDefaultAppList();
                oAuthClientService.deleteOAuthClientWithClientId(app.getClientId());
            } catch (Exception ex) {
                // Ignoring this error. Failure to delete client from Auth server
                // shouldn't fail a sandbox delete.
            }
        }

        // Delete all associated Launch Scenarios
        List<LaunchScenario> launchScenarios = launchScenarioService.findByAppIdAndSandboxId(app.getId(), app.getSandbox().getSandboxId());
        for (LaunchScenario launchScenario: launchScenarios) {
            for (UserLaunch userLaunch: userLaunchService.findByLaunchScenarioId(launchScenario.getId())) {
                userLaunchService.delete(userLaunch.getId());
            }
            launchScenarioService.delete(launchScenario.getId());
        }

        if (app.getLogo() != null) {
            int logoId = app.getLogo().getId();
            app.setLogo(null);
            imageService.delete(logoId);
        }

        if (app.getAuthClient() != null) {
            int authClientId = app.getAuthClient().getId();
            app.setAuthClient(null);
            save(app);
            authClientService.delete(authClientId);
        }
        delete(app.getId());
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public App create(final App app, final Sandbox sandbox) {
        app.setLogo(null);
        app.setCreatedTimestamp(new Timestamp(new Date().getTime()));
        app.setCopyType(CopyType.MASTER);

        if (!ruleService.checkIfUserCanCreateApp(sandbox.getPayerUserId(), findBySandboxId(sandbox.getSandboxId()).size())) {
            return null;
        }

        String entity = oAuthClientService.postOAuthClient(app.getClientJSON());
        try {
            JSONObject jsonObject = new JSONObject(entity);
            app.setClientId((String)jsonObject.get("clientId"));
            app.setClientName((String)jsonObject.get("clientName"));
            return save(app);
        } catch (JSONException e) {
            LOGGER.error(JSON_ERROR_READING_ENTITY, entity, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public App update(final App app) {
        App existingApp = getById(app.getId());
        if (app.getCopyType() == CopyType.MASTER) {
            oAuthClientService.putOAuthClientWithClientId(existingApp.getClientId(), app.getClientJSON());
            existingApp.setLaunchUri(app.getLaunchUri());
            try {
                JSONObject jsonObject = new JSONObject(app.getClientJSON());
                String clientName = jsonObject.get("clientName").toString();
                if (clientName.equals("null")) {
                    existingApp.setClientName(null);
                } else {
                    existingApp.setClientName(clientName);
                }
                String clientUri = jsonObject.get("clientUri").toString();
                if (clientUri.equals("null")) {
                    existingApp.setClientUri(null);
                } else {
                    existingApp.setClientUri(clientUri);
                }
            } catch (JSONException e) {
                LOGGER.error(JSON_ERROR_READING_ENTITY, app.getClientJSON(), e);
                throw new RuntimeException(e);
            }

            existingApp.setLogoUri(app.getLogoUri());
            existingApp.setSamplePatients(app.getSamplePatients());
            existingApp.setBriefDescription(app.getBriefDescription());
            existingApp.setAuthor(app.getAuthor());
            existingApp.setFhirVersions(app.getFhirVersions());
            existingApp.setManifestUrl(app.getManifestUrl());
            existingApp.setClientJSON(app.getClientJSON());
        }
        return save(existingApp);
    }

    @Override
    public App getClientJSON(final App app) {
        String clientJSON = oAuthClientService.getOAuthClientWithClientId(app.getClientId());
//        if (app.getAuthClient().getAuthDatabaseId() != null) {
//            String clientJSON = oAuthClientService.getOAuthClient(app.getAuthClient().getAuthDatabaseId());
//            app.setClientJSON(clientJSON);
//        }
        app.setClientJSON(clientJSON);
        return app;
    }

    @Override
    public App updateAppImage(final App app, final Image image) {
        if (app.getCopyType() == CopyType.MASTER) {
            String clientJSON = oAuthClientService.getOAuthClientWithClientId(app.getClientId());
            try {
                JSONObject jsonObject = new JSONObject(clientJSON);
                jsonObject.put("logoUri", app.getLogoUri());
                oAuthClientService.putOAuthClientWithClientId(app.getClientId(), jsonObject.toString());
            } catch (JSONException e) {
                LOGGER.error(JSON_ERROR_READING_ENTITY, clientJSON, e);
                throw new RuntimeException(e);
            }
        }
        if (app.getLogo() != null) {
            imageService.delete(app.getLogo().getId());
        }
        app.setLogo(image);
        app.setLogoUri(app.getLogoUri());
        return save(app);
    }

    @Override
    public App getById(final int id) {
        return repository.findOne(id);
    }

    @Override
    public App findByLaunchUriAndClientIdAndSandboxId(final String launchUri, final String clientId, final String sandboxId) {
        return repository.findByLaunchUriAndClientIdAndSandboxId(launchUri, clientId, sandboxId);
    }

    @Override
    public List<App> findBySandboxId(final String sandboxId){
        return repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<App> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility) {
        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<App> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }
}
