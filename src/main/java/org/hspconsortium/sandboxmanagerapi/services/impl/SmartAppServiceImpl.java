package org.hspconsortium.sandboxmanagerapi.services.impl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.SmartAppRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class SmartAppServiceImpl implements SmartAppService {
    public static final String JSON_ERROR_READING_ENTITY = "JSON Error reading entity: {}";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartAppServiceImpl.class.getName());

    @Inject
    private SmartAppRepository smartAppRepository;

    private SandboxService sandboxService;
    private OAuthClientService oAuthClientService;
    private RuleService ruleService;
    private UserService userService;
    private LaunchScenarioService launchScenarioService;
    private UserLaunchService userLaunchService;
    private ImageService imageService;

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Inject
    public void setOAuthClientService(OAuthClientService oAuthClientService) { this.oAuthClientService = oAuthClientService; }

    @Inject
    public void setUserService(UserService userService) { this.userService = userService; }

    @Inject
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
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
    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    @Override
    public SmartApp save(@NonNull final SmartApp smartApp) {

        SmartApp existingSmartApp = getById(smartApp.getSmartAppId(), smartApp.getSandboxId());
        if (existingSmartApp == null) {
            Sandbox sandbox = sandboxService.findBySandboxId(smartApp.getSandboxId());
            if (!ruleService.checkIfUserCanCreateApp(sandbox.getPayerUserId(), findBySandboxId(sandbox.getSandboxId()).size())) {
                return null;
            }
            if (!smartApp.getCopyType().equals(CopyType.REPLICA)) {
                String entity = oAuthClientService.postOAuthClient(smartApp.getClientJSON());
                JSONObject entityObject = new JSONObject(entity);
                smartApp.setClientId(entityObject.get("clientId").toString());
                smartApp.setOwner(userService.findById(sandboxService.findBySandboxId(smartApp.getSandboxId()).getPayerUserId()));
                if (smartApp.getCreatedTimestamp() == null) {
                    smartApp.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
                }
                smartApp.setCopyType(CopyType.MASTER);
                smartApp.setVisibility(Visibility2.PRIVATE);
            }
            return smartAppRepository.save(smartApp);
        }

        String client = oAuthClientService.putOAuthClientWithClientId(smartApp.getClientId(), smartApp.getClientJSON());
        smartApp.setClientJSON(client);
        existingSmartApp.setLaunchUrl(smartApp.getLaunchUrl());
        existingSmartApp.setLogoUri(smartApp.getLogoUri());
        existingSmartApp.setSamplePatients(smartApp.getSamplePatients());
        existingSmartApp.setBriefDescription(smartApp.getBriefDescription());
        existingSmartApp.setAuthor(smartApp.getAuthor());
        existingSmartApp.setClientJSON(smartApp.getClientJSON());

        if (existingSmartApp.getCreatedTimestamp() == null) {
            existingSmartApp.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }

        return smartAppRepository.save(existingSmartApp);
    }

    @Override
    public void delete(@NonNull final String smartAppId, @NonNull final String sandboxId) {
        SmartApp smartApp = getById(smartAppId, sandboxId);
        if (smartApp.getCopyType() == CopyType.MASTER) {
            try {
                oAuthClientService.deleteOAuthClientWithClientId(smartApp.getClientId());
            } catch (Exception ex) {
                // Ignoring this error. Failure to delete client from Auth server
                // shouldn't fail a sandbox delete.
            }
        }

        // Delete all associated Launch Scenarios
        List<LaunchScenario> launchScenarios = launchScenarioService.findBySmartAppIdAndSandboxId(smartAppId, sandboxService.findBySandboxId(sandboxId).getId());
        for (LaunchScenario launchScenario: launchScenarios) {
            for (UserLaunch userLaunch: userLaunchService.findByLaunchScenarioId(launchScenario.getId())) {
                userLaunchService.delete(userLaunch.getId());
            }
            launchScenarioService.delete(launchScenario.getId());
        }

        if (smartApp.getLogo() != null) {
            int logoId = smartApp.getLogo().getId();
            smartApp.setLogo(null);
            imageService.delete(logoId);
        }
        smartAppRepository.delete(SmartAppCompositeId.of(smartAppId, sandboxId));
    }

    @Override
    public void delete(@NonNull final SmartApp smartApp) {
        if (smartApp.getCopyType() == CopyType.MASTER) {
            try {
                oAuthClientService.deleteOAuthClientWithClientId(smartApp.getClientId());
            } catch (Exception ex) {
                // Ignoring this error. Failure to delete client from Auth server
                // shouldn't fail a sandbox delete.
            }
        }

        // Delete all associated Launch Scenarios
        List<LaunchScenario> launchScenarios = launchScenarioService.findBySmartAppIdAndSandboxId(smartApp.getSmartAppId(), sandboxService.findBySandboxId(smartApp.getSandboxId()).getId());
        for (LaunchScenario launchScenario: launchScenarios) {
            for (UserLaunch userLaunch: userLaunchService.findByLaunchScenarioId(launchScenario.getId())) {
                userLaunchService.delete(userLaunch.getId());
            }
            launchScenarioService.delete(launchScenario.getId());
        }

        if (smartApp.getLogo() != null) {
            int logoId = smartApp.getLogo().getId();
            smartApp.setLogo(null);
            imageService.delete(logoId);
        }
        smartAppRepository.delete(smartApp);
    }

    @Override
    public SmartApp getById(@NonNull final String smartAppId, @NonNull final String sandboxId) {
        SmartApp smartApp = smartAppRepository.findOne(SmartAppCompositeId.of(smartAppId, sandboxId));
        if (smartApp != null) {
            smartApp.setClientJSON(oAuthClientService.getOAuthClientWithClientId(smartApp.getClientId()));
        }
        return smartApp;
    }

    @Override
    public List<SmartApp> findByOwnerId(@NonNull final int ownerId) {
        return smartAppRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<SmartApp> findBySandboxId(@NonNull final String sandboxId) {
        List<SmartApp> smartApps = smartAppRepository.findBySandboxId(sandboxId);
        return smartApps;
    }

    @Override
    public List<SmartApp> findPublic() {
        return smartAppRepository.findByVisibility(Visibility2.PUBLIC);
    }

    @Override
    public SmartApp updateAppImage(final SmartApp smartApp, final Image image) {
        String clientJSON = oAuthClientService.getOAuthClientWithClientId(smartApp.getClientId());
        try {
            JSONObject jsonObject = new JSONObject(clientJSON);
            jsonObject.put("logoUri", smartApp.getLogoUri());
            oAuthClientService.putOAuthClientWithClientId(smartApp.getClientId(), jsonObject.toString());
        } catch (JSONException e) {
            LOGGER.error(JSON_ERROR_READING_ENTITY, clientJSON, e);
            throw new RuntimeException(e);
        }

        if (smartApp.getLogo() != null) {
            imageService.delete(smartApp.getLogo().getId());
        }
        smartApp.setLogo(image);
        return smartAppRepository.save(smartApp);
    }
//    private String convertManifestToClientJSON(SmartApp smartApp) {
//        JSONObject manifest = new JSONObject(smartApp.getManifest());
//        JSONObject clientJSON = new JSONObject();
//        Iterator<?> keys = manifest.keys();
//
//        while( keys.hasNext() ) {
//            String key = (String)keys.next();
//            String newKey = key;
//            String value =  manifest.get(key).toString();
//            while(newKey.contains("_")) {
//                newKey = newKey.replaceFirst("_[a-z]", String.valueOf(Character.toUpperCase(newKey.charAt(newKey.indexOf("_") + 1))));
//            }
//            clientJSON.put(newKey, value);
//        }
//        clientJSON.put("clientId", smartApp.getClientId());
////        clientJSON.put("logo")
//        String clientJSONString = clientJSON.toString();
//
//        clientJSONString = clientJSONString.replace("\\", "");
//        clientJSONString = clientJSONString.replace("\"[", "[");
//        clientJSONString = clientJSONString.replace("]\"", "]");
//        return clientJSONString;
//    }

}
