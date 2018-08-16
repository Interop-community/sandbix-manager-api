package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.LaunchScenarioRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LaunchScenarioServiceImpl implements LaunchScenarioService {

    private LaunchScenarioRepository repository;
    private ContextParamsService contextParamsService;
    private AppService appService;
    private UserPersonaService userPersonaService;
    private UserLaunchService userLaunchService;

    @Inject
    public LaunchScenarioServiceImpl(LaunchScenarioRepository launchScenarioRepository) {
        this.repository = launchScenarioRepository;
    }

    @Inject
    public void setContextParamsService(ContextParamsService contextParamsService) {
        this.contextParamsService = contextParamsService;
    }

    @Inject
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Inject
    public void setUserPersonaService(UserPersonaService userPersonaService) {
        this.userPersonaService = userPersonaService;
    }

    @Inject
    public void setUserLaunchService(UserLaunchService userLaunchService) {
        this.userLaunchService = userLaunchService;
    }

    @Override
    @Transactional
    public LaunchScenario save(final LaunchScenario launchScenario) {
        return repository.save(launchScenario);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final LaunchScenario launchScenario) {

        if (launchScenario.getApp().isCustomApp()) {
            // This is an anonymous App created for a custom launch
            App app = launchScenario.getApp();
            launchScenario.setApp(null);
            save(launchScenario);
            appService.delete(app);
        }

        List<ContextParams> contextParamsList = launchScenario.getContextParams();
        for (ContextParams contextParams : contextParamsList) {
            contextParamsService.delete(contextParams);
        }

        List<UserLaunch> userLaunches = userLaunchService.findByLaunchScenarioId(launchScenario.getId());
        for (UserLaunch userLaunch : userLaunches) {
            userLaunchService.delete(userLaunch.getId());
        }

        delete(launchScenario.getId());
    }

    @Override
    @Transactional
    public LaunchScenario create(final LaunchScenario launchScenario) {
        Sandbox sandbox = launchScenario.getSandbox();
        launchScenario.setCreatedTimestamp(new Timestamp(new Date().getTime()));

        UserPersona userPersona = null;
        if (launchScenario.getUserPersona() != null) {
            userPersona = userPersonaService.findByPersonaUserIdAndSandboxId(launchScenario.getUserPersona().getPersonaUserId(), sandbox.getSandboxId());
        }
        if (userPersona == null && launchScenario.getUserPersona() != null) {
            userPersona = launchScenario.getUserPersona();
            userPersona.setSandbox(sandbox);
            userPersona = userPersonaService.save(launchScenario.getUserPersona());
        }
        launchScenario.setUserPersona(userPersona);

        if (launchScenario.getApp().isCustomApp()) {
            // Create an anonymous App for a custom launch
            launchScenario.getApp().setSandbox(sandbox);
            App app = appService.save(launchScenario.getApp());
            launchScenario.setApp(app);
        } else {
            App app = appService.findByLaunchUriAndClientIdAndSandboxId(launchScenario.getApp().getLaunchUri(), launchScenario.getApp().getClientId(), sandbox.getSandboxId());
            launchScenario.setApp(app);
        }

        return save(launchScenario);
    }

    @Override
    @Transactional
    public LaunchScenario update(final LaunchScenario launchScenario) {
        LaunchScenario updateLaunchScenario = getById(launchScenario.getId());
        if (updateLaunchScenario != null) {
            updateLaunchScenario.setLastLaunchSeconds(launchScenario.getLastLaunchSeconds());
            updateLaunchScenario.setDescription(launchScenario.getDescription());
            updateLaunchScenario.setNeedPatientBanner(launchScenario.getNeedPatientBanner());
            updateContextParams(updateLaunchScenario, launchScenario.getContextParams());
            if (launchScenario.getApp().isCustomApp()) {
                // Create an anonymous App for a custom launch
                App app = appService.getById(launchScenario.getApp().getId());
                app.setLaunchUri(launchScenario.getApp().getLaunchUri());
                app = appService.save(app);
                updateLaunchScenario.setApp(app);
            }
            return save(updateLaunchScenario);
        }
        return null;
    }

    @Override
    public LaunchScenario updateContextParams(final LaunchScenario launchScenario, final List<ContextParams> newContextParams) {

        List<ContextParams> currentContextParams = launchScenario.getContextParams();
        List<ContextParams> removeContextParams = new ArrayList<>();
        for (ContextParams currentParam : currentContextParams) {
            boolean shouldRemove = true;
            for (ContextParams newParam : newContextParams) {
                if (currentParam.getName().equalsIgnoreCase(newParam.getName()) &&
                        currentParam.getValue().equalsIgnoreCase(newParam.getValue())) {
                    newParam.setId(currentParam.getId());
                    shouldRemove = false;
                }
            }
            if (shouldRemove) {
                removeContextParams.add(currentParam);
            }
        }
        for (ContextParams removeContextParam : removeContextParams) {
            contextParamsService.delete(removeContextParam);
        }
        launchScenario.setContextParams(newContextParams);
        return launchScenario;
    }

    @Override
    public Iterable<LaunchScenario> findAll(){
        return repository.findAll();
    }

    @Override
    public LaunchScenario getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    public List<LaunchScenario> findBySandboxId(final String sandboxId) {
        return  repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<LaunchScenario> findByAppIdAndSandboxId(final int appId, final String sandboxId) {
        return  repository.findByAppIdAndSandboxId(appId, sandboxId);
    }

    @Override
    public List<LaunchScenario> findByUserPersonaIdAndSandboxId(final int userPersonaId, final String sandboxId) {
        return  repository.findByUserPersonaIdAndSandboxId(userPersonaId, sandboxId);
    }

    @Override
    public List<LaunchScenario> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility) {
        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<LaunchScenario> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Override
    public List<LaunchScenario> updateLastLaunchForCurrentUser(final List<LaunchScenario> launchScenarios, final User user) {
        for (LaunchScenario launchScenario : launchScenarios) {
            UserLaunch userLaunch = userLaunchService.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId());
            if (userLaunch != null) {
                launchScenario.setLastLaunchSeconds(userLaunch.getLastLaunchSeconds());
            } else {
                // This user has never launched this launch scenario
                launchScenario.setLastLaunchSeconds(0L);
            }
        }
        return launchScenarios;
    }

}
