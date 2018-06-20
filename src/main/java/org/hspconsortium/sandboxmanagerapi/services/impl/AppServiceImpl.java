package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.json.JSONArray;
import org.json.simple.parser.JSONParser;
import org.hspconsortium.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.AppRepository;
import org.hspconsortium.sandboxmanagerapi.services.AppService;
import org.hspconsortium.sandboxmanagerapi.services.AuthClientService;
import org.hspconsortium.sandboxmanagerapi.services.ImageService;
import org.hspconsortium.sandboxmanagerapi.services.OAuthClientService;

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
import java.sql.*;
import java.util.Date;
import java.util.List;

@Service
public class AppServiceImpl implements AppService {
    public static final String JSON_ERROR_READING_ENTITY = "JSON Error reading entity: {}";
    private static Logger LOGGER = LoggerFactory.getLogger(AppServiceImpl.class.getName());

    private final AppRepository repository;
    private final AuthClientService authClientService;
    private final ImageService imageService;
    private final OAuthClientService oAuthClientService;
    private final ResourceLoader resourceLoader;


    @Inject
    public AppServiceImpl(final AppRepository repository,
                          final AuthClientService authClientService,
                          final ImageService imageService,
                          final OAuthClientService oAuthClientService, final ResourceLoader resourceLoader) {
        this.repository = repository;
        this.authClientService = authClientService;
        this.imageService = imageService;
        this.oAuthClientService = oAuthClientService;
        this.resourceLoader = resourceLoader;
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
                oAuthClientService.deleteOAuthClient(authDatabaseId);
            } catch (Exception ex) {
                // Ignoring this error. Failure to delete client from Auth server
                // shouldn't fail a sandbox delete.
            }
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
    public App create(final App app) {
        app.setLogo(null);
        app.setCreatedTimestamp(new Timestamp(new Date().getTime()));

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
        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<App> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Value("${default-apps-file}")
    private String defaultAppsFile;

    @Override
    public void registerDefaultApps(final Sandbox sandbox) {
        JSONParser parser = new JSONParser();
        try {

            Resource resource = resourceLoader.getResource(defaultAppsFile);
            InputStream in = resource.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = input.readLine()) != null) {
                sb.append(line);
            }

            JSONArray apps = new JSONArray(sb.toString());
            for (int i = 0; i < apps.length(); ++i) {
                JSONObject appInfoJson = apps.getJSONObject(i);
                String clientId = appInfoJson.getJSONObject("authClient").getString("clientId");
                String clientName = appInfoJson.getJSONObject("authClient").getString("clientName");
                Integer authClientId = getAuthClientId(clientId);

                AuthClient authClient = new AuthClient();
                authClient.setClientId(clientId);
                authClient.setClientName(clientName);
                authClient.setLogoUri((String) appInfoJson.get("logoUri"));
                authClient.setAuthDatabaseId(authClientId);
                authClientService.save(authClient);

                App app = new App();
                app.setCreatedTimestamp(new Timestamp(new Date().getTime()));
                app.setLogoUri((String) appInfoJson.get("logoUri"));
                app.setLaunchUri((String) appInfoJson.get("launchUri"));
                app.setBriefDescription((String) appInfoJson.get("briefDescription"));
                app.setSandbox(sandbox);
                app.setCreatedBy(sandbox.getCreatedBy());
                app.setAuthClient(authClient);
                save(app);
            }
        }
        catch (Exception e) {
            LOGGER.error("Error in registering default apps.", e);
        }

    }

    @Value("${spring.datasource.base_url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUserName;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    private Integer getAuthClientId(String clientId) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(databaseUrl, databaseUserName, databasePassword);
            Statement stmt = conn.createStatement() ;
            String query = "SELECT * FROM oic.client_details WHERE client_id='" + clientId + "';" ;
            ResultSet rs = stmt.executeQuery(query);
            Integer id = 0;
            while (rs.next()) {
                // Had to use this workaround to keep the id in scope
                id += Integer.parseInt(rs.getString(1));
            }
            conn.close();
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Error getting Id for client " + clientId, e);
        }
    }

}
