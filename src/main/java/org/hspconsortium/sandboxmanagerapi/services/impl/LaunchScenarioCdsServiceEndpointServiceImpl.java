package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.LaunchScenarioCdsServiceEndpointRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class LaunchScenarioCdsServiceEndpointServiceImpl implements LaunchScenarioCdsServiceEndpointService {

    private LaunchScenarioCdsServiceEndpointRepository repository;
    private CdsServiceEndpointService cdsServiceEndpointService;
    private CdsHookService cdsHookService;
    private UserPersonaService userPersonaService;
    private UserLaunchCdsServiceEndpointService userLaunchCdsServiceEndpointService;

    @Inject
    public LaunchScenarioCdsServiceEndpointServiceImpl(LaunchScenarioCdsServiceEndpointRepository launchScenarioCdsServiceEndpointRepository) {
        this.repository = launchScenarioCdsServiceEndpointRepository;
    }

    @Inject
    public void setCdsServiceEndpointService(CdsServiceEndpointService cdsServiceEndpointService) {
        this.cdsServiceEndpointService = cdsServiceEndpointService;
    }

    @Inject
    public void setUserPersonaService(UserPersonaService userPersonaService) {
        this.userPersonaService = userPersonaService;
    }

    @Inject
    public void setUserLaunchService(UserLaunchCdsServiceEndpointService userLaunchCdsServiceEndpointService) {
        this.userLaunchCdsServiceEndpointService = userLaunchCdsServiceEndpointService;
    }

    @Override
    @Transactional
    public LaunchScenarioCdsServiceEndpoint save(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint) {
        return repository.save(launchScenarioCdsServiceEndpoint);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint) {
        List<UserLaunchCdsServiceEndpoint> userLaunches = userLaunchCdsServiceEndpointService.findByLaunchScenarioCdsServiceEndpointId(launchScenarioCdsServiceEndpoint.getId());
        for (UserLaunchCdsServiceEndpoint userLaunch : userLaunches) {
            userLaunchCdsServiceEndpointService.delete(userLaunch.getId());
        }
        delete(launchScenarioCdsServiceEndpoint.getId());
    }

    @Override
    @Transactional
    public LaunchScenarioCdsServiceEndpoint create(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint) {
        Sandbox sandbox = launchScenarioCdsServiceEndpoint.getSandbox();
        launchScenarioCdsServiceEndpoint.setCreatedTimestamp(new Timestamp(new Date().getTime()));

        UserPersona userPersona = null;
        if (launchScenarioCdsServiceEndpoint.getUserPersona() != null) {
            userPersona = userPersonaService.findByPersonaUserIdAndSandboxId(launchScenarioCdsServiceEndpoint.getUserPersona().getPersonaUserId(), sandbox.getSandboxId());
        }
        if (userPersona == null && launchScenarioCdsServiceEndpoint.getUserPersona() != null) {
            userPersona = launchScenarioCdsServiceEndpoint.getUserPersona();
            userPersona.setSandbox(sandbox);
            userPersona = userPersonaService.save(launchScenarioCdsServiceEndpoint.getUserPersona());
        }
        launchScenarioCdsServiceEndpoint.setUserPersona(userPersona);
        //TODO: Jacob asked me to get rid of CdsServiceEndpoint in the launchScenarioCdsServiceEndpoint, but this method uses it??
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.findByUrlAndSandboxId(
                launchScenarioCdsServiceEndpoint.getCdsServiceEndpoint().getUrl(), sandbox.getSandboxId());
        launchScenarioCdsServiceEndpoint.setCdsServiceEndpoint(cdsServiceEndpoint);

        return save(launchScenarioCdsServiceEndpoint);
    }

    @Override
    @Transactional
    public LaunchScenarioCdsServiceEndpoint update(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint) {
        LaunchScenarioCdsServiceEndpoint updateLaunchScenarioCds = getById(launchScenarioCdsServiceEndpoint.getId());
        if (updateLaunchScenarioCds != null) {
            updateLaunchScenarioCds.setDescription(launchScenarioCdsServiceEndpoint.getDescription());
            updateLaunchScenarioCds.setUserPersona(userPersonaService.getById(launchScenarioCdsServiceEndpoint.getUserPersona().getId()));
            updateLaunchScenarioCds.setCdsServiceEndpoint(cdsServiceEndpointService.getById(launchScenarioCdsServiceEndpoint.getCdsServiceEndpoint().getId()));
            updateLaunchScenarioCds.setContext(launchScenarioCdsServiceEndpoint.getContext()); //TODO: Check this later, if this is how to setup context
            return save(updateLaunchScenarioCds);
        }
        return null;
    }

    @Override
    public LaunchScenarioCdsServiceEndpoint updateContext(final LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint, final JSONObject newContext) {
        //TODO: check this later, how to update Context
        // context is now a saved as string, convert JsonObject to string to setContext
//        launchScenarioCdsServiceEndpoint.setContext(newContext);
        return launchScenarioCdsServiceEndpoint;
    }

    @Override
    public Iterable<LaunchScenarioCdsServiceEndpoint> findAll(){
        return repository.findAll();
    }

    @Override
    public LaunchScenarioCdsServiceEndpoint getById(final int id) {
        return repository.findOne(id);
    }

    @Override
    public List<LaunchScenarioCdsServiceEndpoint> findBySandboxId(final String sandboxId) {
        return  repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<LaunchScenarioCdsServiceEndpoint> findByCdsIdAndSandboxId(final int cdsId, final String sandboxId) {
        return  repository.findByCdsServiceEndpointIdAndSandboxId(cdsId, sandboxId);
    }

    @Override
    public List<LaunchScenarioCdsServiceEndpoint> findByUserPersonaIdAndSandboxId(final int userPersonaId, final String sandboxId) {
        return  repository.findByUserPersonaIdAndSandboxId(userPersonaId, sandboxId);
    }

    @Override
    public List<LaunchScenarioCdsServiceEndpoint> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility) {
        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<LaunchScenarioCdsServiceEndpoint> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Override
    public List<LaunchScenarioCdsServiceEndpoint> updateLastLaunchForCurrentUser(final List<LaunchScenarioCdsServiceEndpoint> launchScenariosCdsList, final User user) {
//        for (LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint : launchScenarioCdsServiceEndpointList) {
//            UserLaunch userLaunch = userLaunchService.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenarioCdsServiceEndpoint.getId());
//            if (userLaunch != null) {
//                launchScenarioCdsServiceEndpoint.setLastLaunchSeconds(userLaunch.getLastLaunchSeconds());
//            } else {
//                // This user has never launched this launch scenario
//                launchScenarioCdsServiceEndpoint.setLastLaunchSeconds(0L);
//            }
//        }
        return launchScenariosCdsList;
    }

}
