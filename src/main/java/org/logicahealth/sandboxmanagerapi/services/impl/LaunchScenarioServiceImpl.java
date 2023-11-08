package org.logicahealth.sandboxmanagerapi.services.impl;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.repositories.LaunchScenarioRepository;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LaunchScenarioServiceImpl implements LaunchScenarioService {
    private static Logger LOGGER = LoggerFactory.getLogger(LaunchScenarioServiceImpl.class.getName());

    private LaunchScenarioRepository repository;
    private ContextParamsService contextParamsService;
    private AppService appService;
    private CdsHookService cdsHookService;
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
    public void setAppService(AppService appService) { this.appService = appService; }

    @Inject
    public void setCdsHookService(CdsHookService cdsHookService) {
        this.cdsHookService = cdsHookService;
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
        
        LOGGER.info("save");
        
        LaunchScenario retVal = repository.save(launchScenario);

        LOGGER.debug("save: "
        +"Parameters: launchScenario = "+launchScenario+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public void delete(final int id) {
        
        LOGGER.info("delete");

        repository.deleteById(id);

        LOGGER.debug("delete: "
        +"Parameter: id = "+id+"; No return value");

    }

    @Override
    @Transactional
    public void delete(final LaunchScenario launchScenario) {
        
        LOGGER.info("delete");

        LOGGER.debug("delete: "
        +"(BEFORE) Parameters: launchScenario = "+launchScenario);

        if (launchScenario.getApp() != null) {
            if (launchScenario.getApp().isCustomApp()) {
                // This is an anonymous App created for a custom launch
                App app = launchScenario.getApp();
                launchScenario.setApp(null);
                save(launchScenario);
                appService.delete(app);
            }
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

        LOGGER.debug("delete: "
        +"(AFTER) Parameters: launchScenario = "+launchScenario+"; No return value");

    }

    @Override
    @Transactional
    public void deleteAssociatedLaunchScenarios(List<LaunchScenario> launchScenarios) {

        LOGGER.info("deleteAssociatedLaunchScenarios");

        for (LaunchScenario launchScenario: launchScenarios) {
            for (ContextParams contextParams : launchScenario.getContextParams()) {
                contextParamsService.delete(contextParams);
            }

            for (UserLaunch userLaunch : userLaunchService.findByLaunchScenarioId(launchScenario.getId())) {
                userLaunchService.delete(userLaunch.getId());
            }
            delete(launchScenario.getId());
        }

        LOGGER.debug("deleteAssociatedLaunchScenarios: "
        +"launchScenarios = "+launchScenarios+"; No return value");

    }

    @Override
    @Transactional
    public LaunchScenario create(final LaunchScenario launchScenario) {
        
        LOGGER.info("create");

        LOGGER.debug("create: "
        +"(BEFORE) Parameters: launchScenario = "+launchScenario);

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

        if(launchScenario.getApp() != null & launchScenario.getCdsHook() == null) {
            if (launchScenario.getApp().isCustomApp()) {
                // Create an anonymous App for a custom launch
                launchScenario.getApp().setSandbox(sandbox);
                App app = appService.save(launchScenario.getApp());
                launchScenario.setApp(app);
            } else {
                App app = appService.findByLaunchUriAndClientIdAndSandboxId(launchScenario.getApp().getLaunchUri(), launchScenario.getApp().getClientId(), sandbox.getSandboxId());
                launchScenario.setApp(app);
            }
        } else if (launchScenario.getApp() == null & launchScenario.getCdsHook() != null) {
            CdsHook cdsHook = cdsHookService.findByHookIdAndCdsServiceEndpointId(launchScenario.getCdsHook().getHookId(), launchScenario.getCdsHook().getCdsServiceEndpointId());
            launchScenario.setCdsHook(cdsHook);
        } else {
            throw new ResourceNotFoundException("No app or CDS-Hook provided");
        }

        LaunchScenario retVal = save(launchScenario);

        LOGGER.debug("create: "
        +"(AFTER) Parameters: launchScenario = "+launchScenario+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional

    public LaunchScenario update(final LaunchScenario launchScenario) {
        
        LOGGER.info("update");

        LaunchScenario updateLaunchScenario = getById(launchScenario.getId());
        if (updateLaunchScenario != null) {
            updateLaunchScenario.setLastLaunchSeconds(launchScenario.getLastLaunchSeconds());
            updateLaunchScenario.setDescription(launchScenario.getDescription());
            updateLaunchScenario.setNeedPatientBanner(launchScenario.getNeedPatientBanner());
            updateLaunchScenario.setTitle(launchScenario.getTitle());
            updateLaunchScenario.setSmartStyleUrl(launchScenario.getSmartStyleUrl());
            updateLaunchScenario.setResource(launchScenario.getResource());
            updateLaunchScenario.setIntent(launchScenario.getIntent());
            updateLaunchScenario.setEncounter(launchScenario.getEncounter());
            updateLaunchScenario.setLocation(launchScenario.getLocation());
            updateLaunchScenario.setPatientName(launchScenario.getPatientName());
            updateLaunchScenario.setPatient(launchScenario.getPatient());
            updateLaunchScenario.setUserPersona(userPersonaService.getById(launchScenario.getUserPersona().getId()));
            if (launchScenario.getApp() != null & launchScenario.getCdsHook() == null) {
                updateLaunchScenario.setApp(appService.getById(launchScenario.getApp().getId()));
                if (launchScenario.getApp().isCustomApp()) {
                    // Create an anonymous App for a custom launch
                    App app = appService.getById(launchScenario.getApp().getId());
                    app.setLaunchUri(launchScenario.getApp().getLaunchUri());
                    app = appService.save(app);
                    updateLaunchScenario.setApp(app);
                }
            } else if (launchScenario.getApp() == null & launchScenario.getCdsHook() != null) {
                updateLaunchScenario.setCdsHook(launchScenario.getCdsHook());
                updateLaunchScenario.setContext(launchScenario.getContext());
            } else {
                throw new IllegalArgumentException("Both App and CDS-Hook can't be updated together ");
            }
            if (launchScenario.getContextParams() != null) {
                updateContextParams(updateLaunchScenario, launchScenario.getContextParams());
            }

            LaunchScenario retVal = save(updateLaunchScenario);

            LOGGER.debug("update: "
            +"Parameters: launchScenario = "+launchScenario
            +"; Return value = "+retVal);

            return retVal;
        }
        
        LOGGER.debug("update: "
        +"Parameters: launchScenario = "+launchScenario
        +"; Return value = null");

        return null;
    }

    @Override
    public LaunchScenario updateContextParams(final LaunchScenario launchScenario, final List<ContextParams> newContextParams) {

        LOGGER.info("updateContextParams");

        LOGGER.debug("updateContextParams: "
        +"(BEFORE) Parameters: launchScenario = "+launchScenario+", newContextParams = "+newContextParams);

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

        LOGGER.debug("updateContextParams: "
        +"(AFTER) Parameters: launchScenario = "+launchScenario+", newContextParams = "+newContextParams
        +"; Return value = "+launchScenario);

        return launchScenario;
    }

    @Override
    public Iterable<LaunchScenario> findAll(){

        LOGGER.info("findAll");

        LOGGER.debug("findAll: "
        +"No input parameters; Return value = "+repository.findAll());

        return repository.findAll();
    }

    @Override
    public LaunchScenario getById(final int id) {
        
        LOGGER.info("getById");

        LOGGER.debug("getById: "
        +"Parameters: id = "+id+"; Return value = "+repository.findById(id).orElse(null));

        return repository.findById(id).orElse(null);
    }

    @Override
    public List<LaunchScenario> findBySandboxId(final String sandboxId) {
        
        LOGGER.info("findBySandboxId");

        LOGGER.debug("findBySandboxId: "
        +"Parameters: sandboxId = "+sandboxId+"; Return value = "+repository.findBySandboxId(sandboxId));

        return  repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<LaunchScenario> findByAppIdAndSandboxId(final int appId, final String sandboxId) {
        
        LOGGER.info("findByAppIdAndSandboxId");

        LOGGER.debug("findByAppIdAndSandboxId: "
        +"Parameters: appId = "+appId+", sandboxId = "+sandboxId
        +"; Return value = "+repository.findByAppIdAndSandboxId(appId, sandboxId));

        return  repository.findByAppIdAndSandboxId(appId, sandboxId);
    }

    @Override
    public List<LaunchScenario> findByUserPersonaIdAndSandboxId(final int userPersonaId, final String sandboxId) {
        
        LOGGER.info("findByUserPersonaIdAndSandboxId");

        LOGGER.debug("findByUserPersonaIdAndSandboxId: "
        +"Parameters: userPersonaId = "+userPersonaId+", sandboxId = "+sandboxId
        +"; Return value = "+repository.findByUserPersonaIdAndSandboxId(userPersonaId, sandboxId));

        return  repository.findByUserPersonaIdAndSandboxId(userPersonaId, sandboxId);
    }

    @Override
    public List<LaunchScenario> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility) {
        
        LOGGER.info("findBySandboxIdAndCreatedByOrVisibility");

        LOGGER.debug("findBySandboxIdAndCreatedByOrVisibility: "
        +"Parameters: sandboxId = "+sandboxId+", createdBy = "+createdBy+", visibility = "+visibility
        +"; Return value = "+repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility));

        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<LaunchScenario> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {

        LOGGER.info("findBySandboxIdAndCreatedBy");

        LOGGER.debug("findBySandboxIdAndCreatedBy: "
        +"Parameters: sandboxId = "+sandboxId+", createdBy = "+createdBy
        +"; Return value = "+repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy));

        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Override
    public List<LaunchScenario> updateLastLaunchForCurrentUser(final List<LaunchScenario> launchScenarios, final User user) {
        
        LOGGER.info("updateLastLaunchForCurrentUser");

        LOGGER.debug("updateLastLaunchForCurrentUser: "
        +"(BEFORE) Parameters: launchScenarios = "+launchScenarios+", user = "+user);

        for (LaunchScenario launchScenario : launchScenarios) {
            UserLaunch userLaunch = userLaunchService.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenario.getId());
            if (userLaunch != null) {
                launchScenario.setLastLaunchSeconds(userLaunch.getLastLaunchSeconds());
            } else {
                // This user has never launched this launch scenario
                launchScenario.setLastLaunchSeconds(0L);
            }
        }

        LOGGER.debug("updateLastLaunchForCurrentUser: "
        +"(AFTER) Parameters: launchScenarios = "+launchScenarios+", user = "+user
        +"; Return value = "+launchScenarios);

        return launchScenarios;
    }

    @Override
    public List<LaunchScenario> findByCdsHookIdAndSandboxId(final int cdsHookId, final String sandboxId) {

        LOGGER.info("findByCdsHookIdAndSandboxId");

        LOGGER.debug("findByCdsHookIdAndSandboxId: "
        +"Parameters: cdsHookId = "+cdsHookId+", sandboxId = "+sandboxId
        +"; Return Value = "+repository.findByCdsHookIdAndSandboxId(cdsHookId, sandboxId));

        return repository.findByCdsHookIdAndSandboxId(cdsHookId, sandboxId);
    }
}
