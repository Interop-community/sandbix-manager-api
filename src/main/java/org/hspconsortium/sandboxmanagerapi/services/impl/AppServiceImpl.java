package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.services.*;
import org.hspconsortium.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.AppRepository;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.*;
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
    private LaunchScenarioService launchScenarioService;
    private UserLaunchService userLaunchService;

    @Inject
    public AppServiceImpl(final AppRepository repository) {
        this.repository = repository;
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
        if (app.getCopyType() == CopyType.MASTER && !app.isCustomApp()) {
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

        // TODO: remove, skip testing
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

        String entity = oAuthClientService.postOAuthClient(app.getClientJSON());
        app.setClientJSON(entity);
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
            oAuthClientService.putOAuthClientWithClientId(app.getClientId(), app.getClientJSON());
            existingApp.setLaunchUri(app.getLaunchUri());
            try {
                JSONObject jsonObject = new JSONObject(app.getClientJSON());
                String clientName = jsonObject.get("clientName").toString();
                if (clientName.equals("null")) {
                    existingApp.setClientName(null);
                } else {
                    existingApp.setClientName(clientName);
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
            return save(existingApp);
        }
        return app;
    }

    @Override
    public App getClientJSON(final App app) {
        String clientJSON = oAuthClientService.getOAuthClientWithClientId(app.getClientId());
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
    public App deleteAppImage(final App existingApp) {
        if (existingApp.getCopyType() == CopyType.MASTER) {
            try {
                JSONObject jsonObject = new JSONObject(oAuthClientService.getOAuthClientWithClientId(existingApp.getClientId()));
                jsonObject.put("logoUri", "null");
                oAuthClientService.putOAuthClientWithClientId(existingApp.getClientId(), jsonObject.toString());
            } catch (JSONException e) {
                LOGGER.error(JSON_ERROR_READING_ENTITY, existingApp.getClientJSON(), e);
                throw new RuntimeException(e);
            }
            if (existingApp.getLogo() != null) {
                imageService.delete(existingApp.getLogo().getId());
            }
            existingApp.setLogoUri(null);
            existingApp.setLogo(null);
            return save(existingApp);
        }
        return existingApp;
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

    //TODO: remove after release of new sandbox manager and custom apps are dead
    @Override
    public List<App> findBySandboxIdIncludingCustomApps(final String sandboxId) {
        return repository.findBySandboxIdIncludingCustomApps(sandboxId);
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
