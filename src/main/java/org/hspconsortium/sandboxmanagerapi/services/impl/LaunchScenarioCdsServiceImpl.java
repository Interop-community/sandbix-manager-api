package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.LaunchScenarioCdsRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LaunchScenarioCdsServiceImpl implements LaunchScenarioCdsService {

    private LaunchScenarioCdsRepository repository;
    private CdsServiceEndpointService cdsServiceEndpointService;
    private CdsHookService cdsHookService;
    private UserPersonaService userPersonaService;
    private UserLaunchService userLaunchService;

    @Inject
    public LaunchScenarioCdsServiceImpl(LaunchScenarioCdsRepository launchScenariocdsRepository) {
        this.repository = launchScenariocdsRepository;
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
    public void setUserLaunchService(UserLaunchService userLaunchService) {
        this.userLaunchService = userLaunchService;
    }

    @Override
    @Transactional
    public LaunchScenarioCds save(final LaunchScenarioCds launchScenarioCds) {
        return repository.save(launchScenarioCds);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final LaunchScenarioCds launchScenarioCds) {
        List<UserLaunch> userLaunches = userLaunchService.findByLaunchScenarioCdsId(launchScenarioCds.getId());
        for (UserLaunch userLaunch : userLaunches) {
            userLaunchService.delete(userLaunch.getId());
        }
        delete(launchScenarioCds.getId());
    }

    @Override
    @Transactional
    public LaunchScenarioCds create(final LaunchScenarioCds launchScenarioCds) {
        Sandbox sandbox = launchScenarioCds.getSandbox();
        launchScenarioCds.setCreatedTimestamp(new Timestamp(new Date().getTime()));

        UserPersona userPersona = null;
        if (launchScenarioCds.getUserPersona() != null) {
            userPersona = userPersonaService.findByPersonaUserIdAndSandboxId(launchScenarioCds.getUserPersona().getPersonaUserId(), sandbox.getSandboxId());
        }
        if (userPersona == null && launchScenarioCds.getUserPersona() != null) {
            userPersona = launchScenarioCds.getUserPersona();
            userPersona.setSandbox(sandbox);
            userPersona = userPersonaService.save(launchScenarioCds.getUserPersona());
        }
        launchScenarioCds.setUserPersona(userPersona);
        //TODO: Jacob asked me to get rid of CdsServiceEndpoint in the launchScenarioCds, but this method uses it??
        CdsServiceEndpoint cdsServiceEndpoint = cdsServiceEndpointService.findByUrlAndSandboxId(
                launchScenarioCds.getCdsServiceEndpoint().getUrl(), sandbox.getSandboxId());
        launchScenarioCds.setCdsServiceEndpoint(cdsServiceEndpoint);

        return save(launchScenarioCds);
    }

    @Override
    @Transactional
    public LaunchScenarioCds update(final LaunchScenarioCds launchScenarioCds) {
        LaunchScenarioCds updateLaunchScenarioCds = getById(launchScenarioCds.getId());
        if (updateLaunchScenarioCds != null) {
            updateLaunchScenarioCds.setLastLaunchSeconds(launchScenarioCds.getLastLaunchSeconds());
            updateLaunchScenarioCds.setDescription(launchScenarioCds.getDescription());
            updateLaunchScenarioCds.setUserPersona(userPersonaService.getById(launchScenarioCds.getUserPersona().getId()));
            updateLaunchScenarioCds.setCdsServiceEndpoint(cdsServiceEndpointService.getById(launchScenarioCds.getCdsServiceEndpoint().getId()));
            updateLaunchScenarioCds.setContext(launchScenarioCds.getContext()); //TODO: Check this later, if this is how to setup context
            return save(updateLaunchScenarioCds);
        }
        return null;
    }

    @Override
    public LaunchScenarioCds updateContext(final LaunchScenarioCds launchScenarioCds, final JSONObject newContext) {
        //TODO: check this later, how to update Context
        launchScenarioCds.setContext(newContext);
        return launchScenarioCds;
    }

    @Override
    public Iterable<LaunchScenarioCds> findAll(){
        return repository.findAll();
    }

    @Override
    public LaunchScenarioCds getById(final int id) {
        return repository.findOne(id);
    }

    @Override
    public List<LaunchScenarioCds> findBySandboxId(final String sandboxId) {
        return  repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<LaunchScenarioCds> findByCdsIdAndSandboxId(final int cdsId, final String sandboxId) {
        return  repository.findByCdsIdAndSandboxId(cdsId, sandboxId);
    }

    @Override
    public List<LaunchScenarioCds> findByUserPersonaIdAndSandboxId(final int userPersonaId, final String sandboxId) {
        return  repository.findByUserPersonaIdAndSandboxId(userPersonaId, sandboxId);
    }

    @Override
    public List<LaunchScenarioCds> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility) {
        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<LaunchScenarioCds> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Override
    public List<LaunchScenarioCds> updateLastLaunchForCurrentUser(final List<LaunchScenarioCds> launchScenariosCdsList, final User user) {
        for (LaunchScenarioCds launchScenarioCds : launchScenariosCdsList) {
            UserLaunch userLaunch = userLaunchService.findByUserIdAndLaunchScenarioId(user.getSbmUserId(), launchScenarioCds.getId());
            if (userLaunch != null) {
                launchScenarioCds.setLastLaunchSeconds(userLaunch.getLastLaunchSeconds());
            } else {
                // This user has never launched this launch scenario
                launchScenarioCds.setLastLaunchSeconds(0L);
            }
        }
        return launchScenariosCdsList;
    }

}
