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
    private LaunchScenarioCdsServiceEndpointService launchScenarioCdsServiceEndpointService;
    private UserLaunchCdsServiceEndpointService userLaunchCdsServiceEndpointService;

    @Inject
    public CdsServiceEndpointServiceImpl(final CdsServiceEndpointRepository cdsServiceEndpointRepository) { this.repository = cdsServiceEndpointRepository; }

    @Inject
    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    @Inject
    public void setLaunchScenarioCdsService(LaunchScenarioCdsServiceEndpointService launchScenarioCdsServiceEndpointService) {
        this.launchScenarioCdsServiceEndpointService = launchScenarioCdsServiceEndpointService;
    }

    @Inject
    public void setUserLaunchService(UserLaunchCdsServiceEndpointService userLaunchCdsServiceEndpointService) {
        this.userLaunchCdsServiceEndpointService = userLaunchCdsServiceEndpointService;
    }

    @Inject
    public void setLaunchScenarioCdsServices(LaunchScenarioCdsServiceEndpointService launchScenarioCdsServiceEndpointService, UserLaunchCdsServiceEndpointService userLaunchCdsServiceEndpointService) {
        this.launchScenarioCdsServiceEndpointService = launchScenarioCdsServiceEndpointService;
        this.userLaunchCdsServiceEndpointService = userLaunchCdsServiceEndpointService; //TODO: Check AppServiceImpl doesn't set this.  Why??
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
        List<LaunchScenarioCdsServiceEndpoint> launchScenarioCdsServiceEndpointList = launchScenarioCdsServiceEndpointService.findByCdsIdAndSandboxId(cdsServiceEndpoint.getId(), cdsServiceEndpoint.getSandbox().getSandboxId());
        for (LaunchScenarioCdsServiceEndpoint launchScenarioCdsServiceEndpoint: launchScenarioCdsServiceEndpointList) {
            for (UserLaunchCdsServiceEndpoint userLaunchCdsServiceEndpoint: userLaunchCdsServiceEndpointService.findByLaunchScenarioCdsServiceEndpointId(launchScenarioCdsServiceEndpoint.getId())) {
                userLaunchCdsServiceEndpointService.delete(userLaunchCdsServiceEndpoint.getId());
            }
            launchScenarioCdsServiceEndpointService.delete(launchScenarioCdsServiceEndpoint.getId());
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
    public CdsServiceEndpoint create(final CdsServiceEndpoint cdsServiceEndpoint, final Sandbox sandbox) {
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

    public void addCdsServiceEndpointCdsHook(final CdsServiceEndpoint cdsServiceEndpoint, final CdsHook cdsHook) {
        List<CdsHook> cdsHooks = cdsServiceEndpoint.getCdsHooks();
        cdsHooks.add(cdsHook);
        cdsServiceEndpoint.setCdsHooks(cdsHooks);
        save(cdsServiceEndpoint);
    }
}
