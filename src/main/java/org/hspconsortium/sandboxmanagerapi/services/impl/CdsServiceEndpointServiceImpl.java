package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.CdsServiceEndpointRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class CdsServiceEndpointServiceImpl implements CdsServiceEndpointService {
    public static final String JSON_ERROR_READING_ENTITY = "JSON Error reading entity: {}";
    private static Logger LOGGER = LoggerFactory.getLogger(AppServiceImpl.class.getName());

    private final CdsServiceEndpointRepository repository;
    private ImageService imageService;
    private LaunchScenarioCdsService launchScenarioCdsService;
    private UserLaunchService userLaunchService;

    @Inject
    public CdsServiceEndpointServiceImpl(final CdsServiceEndpointRepository cdsServiceEndpointRepository) { this.repository = cdsServiceEndpointRepository; }

    @Inject
    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    @Inject
    public void setLaunchScenarioCdsService(LaunchScenarioCdsService launchScenarioCdsService) {
        this.launchScenarioCdsService = launchScenarioCdsService;
    }

    @Inject
    public void setUserLaunchService(UserLaunchService userLaunchService) {
        this.userLaunchService = userLaunchService;
    }

    @Inject
    public void setLaunchScenarioCdsServices(LaunchScenarioCdsService launchScenarioCdsService, UserLaunchService userLaunchService) {
        this.launchScenarioCdsService = launchScenarioCdsService;
        this.userLaunchService = userLaunchService; //TODO: Check AppServiceImpl doesn't set this.  Why??
    }

    @Override
    @Transactional
    public CdsServiceEndpoint save(final CdsServiceEndpoint cdsServiceEndpoint) {
        return repository.save(cdsServiceEndpoint);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final CdsServiceEndpoint cdsServiceEndpoint, CdsHook cdsHook) {
        // Delete all associated CDS-Service Launch Scenarios
        List<LaunchScenarioCds> launchScenariosCdsList = launchScenarioCdsService.findByCdsIdAndSandboxId(cdsServiceEndpoint.getId(), cdsServiceEndpoint.getSandbox().getSandboxId());
        for (LaunchScenarioCds launchScenarioCds: launchScenariosCdsList) {
            for (UserLaunch userLaunch: userLaunchService.findByLaunchScenarioCdsId(launchScenarioCds.getId())) {
                userLaunchService.delete(userLaunch.getId());
            }
            launchScenarioCdsService.delete(launchScenarioCds.getId());
        }
        if (cdsHook.getLogo() != null) {
            int logoId = cdsHook.getLogo().getId();
            cdsHook.setLogo(null);
            imageService.delete(logoId);
        }
        delete(cdsServiceEndpoint.getId());
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public CdsServiceEndpoint create(final CdsServiceEndpoint cdsServiceEndpoint, final CdsHook cdsHook, final Sandbox sandbox) {
        cdsHook.setLogo(null); //TODO: Why does AppServiceImpl has logo set to null;
        cdsServiceEndpoint.setCreatedTimestamp(new Timestamp(new Date().getTime()));
        return save(cdsServiceEndpoint);
    }

    @Override
    @Transactional
    public  CdsServiceEndpoint update(final CdsServiceEndpoint cdsServiceEndpoint) {
        CdsServiceEndpoint existingCdsServiceEndpoint = getById(cdsServiceEndpoint.getId());
        existingCdsServiceEndpoint.setTitle(cdsServiceEndpoint.getTitle());
        existingCdsServiceEndpoint.setDescription(cdsServiceEndpoint.getDescription());
        existingCdsServiceEndpoint.setUrl(cdsServiceEndpoint.getUrl());
        existingCdsServiceEndpoint.setCdsHooks(cdsServiceEndpoint.getCdsHooks());
        return save(cdsServiceEndpoint);
    }

    @Override
    public CdsServiceEndpoint getById(final int id) {
        return repository.findOne(id);
    }

    public List<CdsServiceEndpoint> findBySandboxId(final String sandboxId) {
        return repository.findBySandboxId(sandboxId);
    }

    public List<CdsServiceEndpoint> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility) {
        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    public List<CdsServiceEndpoint> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    public CdsServiceEndpoint findByUrlAndSandboxId (final String url, final String sandboxId) {
        return repository.findByUrlAndSandboxId(url, sandboxId);
    }
}
