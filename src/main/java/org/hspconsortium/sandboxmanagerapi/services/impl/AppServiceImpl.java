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
    private SmartAppService smartAppService;

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

        Integer authDatabaseId = app.getAuthClient().getAuthDatabaseId();
        if (authDatabaseId != null) {
            try {
                // TODO: don't delete in auth server if default app
//                JSONArray apps = getDefaultAppList();
                oAuthClientService.deleteOAuthClient(authDatabaseId);
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

        int authClientId = app.getAuthClient().getId();
        app.setAuthClient(null);
        save(app);
        authClientService.delete(authClientId);
        delete(app.getId());
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public App create(final App app, final Sandbox sandbox) {
        app.setLogo(null);
        app.setCreatedTimestamp(new Timestamp(new Date().getTime()));

        if (!ruleService.checkIfUserCanCreateApp(sandbox.getPayerUserId(), findBySandboxId(sandbox.getSandboxId()).size())) {
            return null;
        }

        String entity = oAuthClientService.postOAuthClient(app.getClientJSON());
        try {
            JSONObject jsonObject = new JSONObject(entity);
            app.getAuthClient().setAuthDatabaseId((Integer)jsonObject.get("id"));
            app.getAuthClient().setClientId((String)jsonObject.get("clientId"));
            app.getAuthClient().setClientName((String)jsonObject.get("clientName"));
            AuthClient authClient = authClientService.save(app.getAuthClient());
            app.setAuthClient(authClient);
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
        String entity = oAuthClientService.putOAuthClient(existingApp.getAuthClient().getAuthDatabaseId(), app.getClientJSON());

        try {
            JSONObject jsonObject = new JSONObject(entity);
            existingApp.getAuthClient().setClientName((String)jsonObject.get("clientName"));
            existingApp.getAuthClient().setLogoUri(app.getLogoUri());
            authClientService.save(existingApp.getAuthClient());
        } catch (JSONException e) {
            LOGGER.error(JSON_ERROR_READING_ENTITY, entity, e);
            throw new RuntimeException(e);
        }
        existingApp.setLaunchUri(app.getLaunchUri());
        existingApp.setLogoUri(app.getLogoUri());
        existingApp.setSamplePatients(app.getSamplePatients());
        existingApp.setBriefDescription(app.getBriefDescription());
        existingApp.setAuthor(app.getAuthor());
        existingApp.setClientJSON(app.getClientJSON());
        return save(existingApp);
    }

    @Override
    public App getClientJSON(final App app) {
        if (app.getAuthClient().getAuthDatabaseId() != null) {
            String clientJSON = oAuthClientService.getOAuthClient(app.getAuthClient().getAuthDatabaseId());
            app.setClientJSON(clientJSON);
        }
        return app;
    }

    @Override
    public App updateAppImage(final App app, final Image image) {
        String clientJSON = oAuthClientService.getOAuthClient(app.getAuthClient().getAuthDatabaseId());
        try {
            JSONObject jsonObject = new JSONObject(clientJSON);
            jsonObject.put("logoUri", app.getLogoUri());
            oAuthClientService.putOAuthClient(app.getAuthClient().getAuthDatabaseId(), jsonObject.toString());
        } catch (JSONException e) {
            LOGGER.error(JSON_ERROR_READING_ENTITY, clientJSON, e);
            throw new RuntimeException(e);
        }

        if (app.getLogo() != null) {
            imageService.delete(app.getLogo().getId());
        }
        app.setLogo(image);
        app.getAuthClient().setLogoUri(app.getLogoUri());
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
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
//        List<SmartApp> smartApps = sandbox.getSmartApps();
//        List<App> apps = new ArrayList<>();
//        // TODO: the frontend should have to do this
//        for (SmartApp smartApp: smartApps) {
//            if (smartApp.getManifestUrl() != null) {
//                try {
//                    URL url = new URL(smartApp.getManifestUrl());
//                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                    con.setRequestMethod("GET");
//                    BufferedReader in = new BufferedReader(
//                            new InputStreamReader(con.getInputStream()));
//                    String inputLine;
//                    StringBuffer content = new StringBuffer();
//                    while ((inputLine = in.readLine()) != null) {
//                        content.append(inputLine);
//                    }
//                    JSONObject manifest = new JSONObject(content.toString());
//                    in.close();
//                    App app = new App();
//                    String oAuthClient = oAuthClientService.getOAuthClientWithClientId(smartApp.getClientId());
//                    JSONObject oAuthClientObject = new JSONObject(oAuthClient);
//                    // TODO: get rid of Authclient object
//                    AuthClient authclient = new AuthClient();
//                    authclient.setClientName(oAuthClientObject.get("clientName").toString());
//                    authclient.setAuthDatabaseId(Integer.parseInt(oAuthClientObject.get("id").toString()));
//                    authclient.setClientId(oAuthClientObject.get("clientId").toString());
//                    app.setAuthClient(authclient);
//                    app.setLaunchUri(manifest.get("launch_url").toString());
//                    app.setSandbox(sandbox);
//                    app.setLogoUri(manifest.get("logo_uri").toString());
//                    app.setAppManifestUri(smartApp.getManifestUrl());
//                    app.setBriefDescription(smartApp.getBriefDescription());
////                    app.setVisibility(smartApp.getVisibility());
//
//                    apps.add(app);
//                } catch (Exception e) {
//
//                }
//            }
//
//        }
//        List<App> other_apps = repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
//        apps.addAll(other_apps);
//        return apps;
        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<App> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

//    @Value("${default-apps-file}")
//    private String defaultAppsFile;
//
//    @Override
//    public void registerDefaultApps(final Sandbox sandbox) {
//
//        JSONArray apps = getDefaultAppList();
//
//        for (int i = 0; i < apps.length(); ++i) {
//            JSONObject appInfoJson = apps.getJSONObject(i);
//            String clientId = appInfoJson.getJSONObject("authClient").getString("clientId");
//            String clientName = appInfoJson.getJSONObject("authClient").getString("clientName");
//            Integer authClientId = getAuthClientId(clientId);
//
//            AuthClient authClient = new AuthClient();
//            authClient.setClientId(clientId);
//            authClient.setClientName(clientName);
//            authClient.setLogoUri((String) appInfoJson.get("logoUri"));
//            authClient.setAuthDatabaseId(authClientId);
//            authClientService.save(authClient);
//
//            App app = new App();
//            app.setCreatedTimestamp(new Timestamp(new Date().getTime()));
//            app.setLogoUri((String) appInfoJson.get("logoUri"));
//            app.setLaunchUri((String) appInfoJson.get("launchUri"));
//            app.setBriefDescription((String) appInfoJson.get("briefDescription"));
//            app.setSandbox(sandbox);
//            app.setCreatedBy(sandbox.getCreatedBy());
//            app.setAuthClient(authClient);
//            save(app);
//        }
//    }
//
//    private JSONArray getDefaultAppList() {
//        try {
//            Resource resource = resourceLoader.getResource(defaultAppsFile);
//            InputStream in = resource.getInputStream();
//            BufferedReader input = new BufferedReader(new InputStreamReader(in));
//            StringBuilder sb = new StringBuilder();
//
//            String line;
//            while ((line = input.readLine()) != null) {
//                sb.append(line);
//            }
//
//            return new JSONArray(sb.toString());
//        } catch (Exception e) {
//            LOGGER.error("Unable to load default app information.", e);
//            return new JSONArray();
//        }
//    }
//

}
