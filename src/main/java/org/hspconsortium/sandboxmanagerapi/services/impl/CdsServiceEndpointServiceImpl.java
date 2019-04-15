package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.CdsServiceEndpointRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CdsServiceEndpointServiceImpl implements CdsServiceEndpointService {

    private final CdsServiceEndpointRepository repository;
    private CdsHookService cdsHookService;
    private LaunchScenarioService launchScenarioService;
    private UserLaunchService userLaunchService;

    @Inject
    public CdsServiceEndpointServiceImpl(final CdsServiceEndpointRepository cdsServiceEndpointRepository) { this.repository = cdsServiceEndpointRepository; }

    @Inject
    public void setCdsHookService(CdsHookService cdsHookService) {
        this.cdsHookService = cdsHookService;
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
    public void setLaunchScenarioCdsServices(LaunchScenarioService launchScenarioService, UserLaunchService userLaunchService) {
        this.launchScenarioService = launchScenarioService;
        this.userLaunchService = userLaunchService;
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
    public void delete(final CdsServiceEndpoint cdsServiceEndpoint) {
        // Delete all associated CDS-Hook and  CDS-Hook Launch Scenarios
        List<CdsHook> cdsHooks = cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId());
        for (CdsHook cdsHook: cdsHooks) {
            for (LaunchScenario launchScenario: launchScenarioService.findByCdsHookIdAndSandboxId(cdsHook.getId(), cdsServiceEndpoint.getSandbox().getSandboxId())) {
                for (UserLaunch userLaunch: userLaunchService.findByLaunchScenarioId(launchScenario.getId())) {
                    userLaunchService.delete(userLaunch.getId());
                }
                launchScenarioService.delete(launchScenario.getId());
            }
            cdsHookService.delete(cdsHook);
        }
        delete(cdsServiceEndpoint.getId());
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public CdsServiceEndpoint create(final CdsServiceEndpoint cdsServiceEndpoint, final Sandbox sandbox) {
        CdsServiceEndpoint existingCdsServiceEndpoint = findByCdsServiceEndpointUrlAndSandboxId(cdsServiceEndpoint.getUrl(), cdsServiceEndpoint.getSandbox().getSandboxId());
        if (existingCdsServiceEndpoint != null) {
            cdsServiceEndpoint.setId(existingCdsServiceEndpoint.getId());
            return update(cdsServiceEndpoint);
        }
        cdsServiceEndpoint.setCreatedTimestamp(new Timestamp(new Date().getTime()));
        cdsServiceEndpoint.setLastUpdated(new Timestamp(new Date().getTime()));
        CdsServiceEndpoint cdsServiceEndpointSaved = save(cdsServiceEndpoint);
        List<CdsHook> cdsHooks = cdsServiceEndpoint.getCdsHooks();
        for (CdsHook cdsHook: cdsHooks) {
            cdsHook.setCdsServiceEndpointId(cdsServiceEndpointSaved.getId());
            cdsHook.setHookUrl(cdsServiceEndpointSaved.getUrl() + "/" + cdsHook.getHookId());
            cdsHookService.create(cdsHook);
        }
        return cdsServiceEndpointSaved;
    }

    @Override
    @Transactional
    public  CdsServiceEndpoint update(final CdsServiceEndpoint cdsServiceEndpoint) {
        CdsServiceEndpoint existingCdsServiceEndpoint = getById(cdsServiceEndpoint.getId());
        existingCdsServiceEndpoint.setTitle(cdsServiceEndpoint.getTitle());
        existingCdsServiceEndpoint.setDescription(cdsServiceEndpoint.getDescription());
        existingCdsServiceEndpoint.setLastUpdated(new Timestamp(new Date().getTime()));
        List<CdsHook> cdsHooks = cdsServiceEndpoint.getCdsHooks();
        List<CdsHook> existingCdsHooks = cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId());

        for (CdsHook existingCdsHook: existingCdsHooks) {
            List<CdsHook> c = cdsHooks.stream().filter(p1 -> existingCdsHook.getHookId().equals(p1.getHookId())).collect(Collectors.toList());
            if (c.size() == 0) {
                cdsHookService.delete(existingCdsHook);
            }
        }

        for (CdsHook cdsHook: cdsHooks) {
            cdsHook.setCdsServiceEndpointId(cdsServiceEndpoint.getId());
            cdsHook.setHookUrl(cdsServiceEndpoint.getUrl() + "/" + cdsHook.getHookId());
            cdsHookService.create(cdsHook);
        }
        return save(existingCdsServiceEndpoint);
    }

    @Override
    public CdsServiceEndpoint getById(final int id) {
        return repository.findOne(id);
    }

    @Override
    public List<CdsServiceEndpoint> findBySandboxId(final String sandboxId) {
        return repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<CdsServiceEndpoint> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility) {
        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<CdsServiceEndpoint> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Override
    public CdsServiceEndpoint findByCdsServiceEndpointUrlAndSandboxId (final String url, final String sandboxId) {
        return repository.findByCdsServiceEndpointUrlAndSandboxId(url, sandboxId);
    }

}
