package org.logicahealth.sandboxmanagerapi.services.impl;

import org.json.JSONException;
import org.json.JSONObject;
import org.logicahealth.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.repositories.AppRepository;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class AppServiceImpl implements AppService {
    public static final String JSON_ERROR_READING_ENTITY = "JSON Error reading entity: {}";
    private static Logger LOGGER = LoggerFactory.getLogger(AppServiceImpl.class.getName());

    private final AppRepository repository;
    private ImageService imageService;
    private OAuthClientService oAuthClientService;
    private LaunchScenarioService launchScenarioService;
    private UserLaunchService userLaunchService;

    @Inject
    public AppServiceImpl(final AppRepository repository) {
        this.repository = repository;
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

        LOGGER.info("save");

        App retVal = repository.save(app);

        LOGGER.debug("save: "
        +"Parameters: app = "+app+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public void delete(final int id) {
        
        LOGGER.info("delete");
        
        repository.deleteById(id);

        LOGGER.debug("delete: "
        +"Parameters: id = "+id+"; No return value");
    }

    @Override
    @Transactional
    public void delete(final App app) {

        LOGGER.info("delete");

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
        launchScenarioService.deleteAssociatedLaunchScenarios(launchScenarios);

        if (app.getLogo() != null) {
            int logoId = app.getLogo().getId();
            app.setLogo(null);
            imageService.delete(logoId);
        }

        delete(app.getId());

        LOGGER.debug("delete: "
        +"Parameters: app = "+app+"; No return value");

    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public App create(final App app, final Sandbox sandbox) {
        
        LOGGER.info("create");

        LOGGER.debug("create: "
        +"(BEFORE) Parameters: app = "+app+", sandbox = "+sandbox);

        app.setLogo(null);
        app.setCreatedTimestamp(new Timestamp(new Date().getTime()));
        app.setCopyType(CopyType.MASTER);

        String entity = oAuthClientService.postOAuthClient(app.getClientJSON());
        app.setClientJSON(entity);
        try {
            JSONObject jsonObject = new JSONObject(entity);
            app.setClientId((String)jsonObject.get("clientId"));
            app.setClientName((String)jsonObject.get("clientName"));

            App retVal = save(app);

            LOGGER.debug("create: "
            +"(AFTER) Parameters: app = "+app+", sandbox = "+sandbox+"; Return value = "+retVal);

            return retVal;
        } catch (JSONException e) {
            LOGGER.error(JSON_ERROR_READING_ENTITY, entity, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public App update(final App app) {

        LOGGER.info("update");

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

            App retVal = save(existingApp);

            LOGGER.debug("update: "
            + "Parameters: app = "+app+"; Return value = "+retVal);

            return retVal;
        }

        LOGGER.debug("update: "
        + "Parameters: app = "+app+"; Return value = "+app);

        return app;
    }

    @Override
    public App getClientJSON(final App app) {
        
        LOGGER.info("getClientJSON");
        
        LOGGER.debug("getClientJSON: "
        +"(BEFORE) Parameters: app = "+app);
        
        String clientJSON = oAuthClientService.getOAuthClientWithClientId(app.getClientId());
        app.setClientJSON(clientJSON);
        
        LOGGER.debug("getClientJSON: "
        +"(AFTER) Parameters: app = "+app+"; Return value = "+app);
        
        return app;
    }

    @Override
    public App updateAppImage(final App app, final Image image) {
        
        LOGGER.info("updateAppImage");

        LOGGER.debug("updateAppImage: "
        +"(BEFORE) Parameters: app = "+app+", image = "+image);

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

        App retVal = save(app);

        LOGGER.debug("updateAppImage: "
        +"(AFTER) Parameters: app = "+app+", image = "+image+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public App deleteAppImage(final App existingApp) {
        
        LOGGER.info("deleteAppImage");

        LOGGER.debug("deleteAppImage: "
        +"Parameters: existingApp = "+existingApp+"; Return value = "+existingApp);

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
    
            App retVal = save(existingApp);

            LOGGER.debug("deleteAppImage: "
            +"(AFTER) Parameters: existingApp = "+existingApp+"; Return value = "+retVal);

            return retVal;
        }

        LOGGER.debug("deleteAppImage: "
        +"(AFTER) Parameters: existingApp = "+existingApp+"; Return value = "+existingApp);

        return existingApp;
    }

    @Override
    public App getById(final int id) {

        LOGGER.info("getById");

        LOGGER.debug("getById: "
        +"Parameters: id = "+id+"; Return Value = "+repository.findById(id).orElse(null));

        return repository.findById(id).orElse(null);
    }

    @Override
    public App findByLaunchUriAndClientIdAndSandboxId(final String launchUri, final String clientId, final String sandboxId) {
        LOGGER.info("findByLaunchUriAndClientIdAndSandboxId");

        LOGGER.debug("findByLaunchUriAndClientIdAndSandboxId: "
        +"Parameters: launchUri = "+launchUri+", clientId = "+clientId+", sandboxId = "+sandboxId
        +"; Return value = "+repository.findByLaunchUriAndClientIdAndSandboxId(launchUri, clientId, sandboxId));

        return repository.findByLaunchUriAndClientIdAndSandboxId(launchUri, clientId, sandboxId);
    }

    @Override
    public List<App> findBySandboxId(final String sandboxId){

        LOGGER.info("findBySandboxId");

        LOGGER.debug("findBySandboxId: "
        +"Parameters: sandboxId = "+sandboxId+"; Return value = "+repository.findBySandboxId(sandboxId));

        return repository.findBySandboxId(sandboxId);
    }

    //TODO: remove after release of new sandbox manager and custom apps are dead
    @Override
    public List<App> findBySandboxIdIncludingCustomApps(final String sandboxId) {
        
        LOGGER.info("findBySandboxIdIncludingCustomApps");

        LOGGER.debug("findBySandboxIdIncludingCustomApps: "
        +"Parameters: sandboxId = "+sandboxId
        +"; Return value = "+repository.findBySandboxIdIncludingCustomApps(sandboxId));

        return repository.findBySandboxIdIncludingCustomApps(sandboxId);
    }

    @Override
    public List<App> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility) {
        
        LOGGER.info("findBySandboxIdAndCreatedByOrVisibility");

        List<App> apps = repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
        for (App app: apps) {
            getClientJSON(app);
        }

        LOGGER.debug("findBySandboxIdAndCreatedByOrVisibility: "
        +"Parameters: sandboxId = "+sandboxId+", createdBy = "+createdBy+", visibility = "+visibility
        +"; Return value = "+apps);

        return apps;
    }

    @Override
    public List<App> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        
        LOGGER.info("findBySandboxIdAndCreatedBy");

        LOGGER.debug("findBySandboxIdAndCreatedBy: "
        +"Parameters: sandboxId = "+sandboxId+", createdBy = "+createdBy
        +"; Return value = "+repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy));
        
        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }
}
