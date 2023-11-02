package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.repositories.CdsServiceEndpointRepository;
import org.logicahealth.sandboxmanagerapi.services.CdsHookService;
import org.logicahealth.sandboxmanagerapi.services.CdsServiceEndpointService;
import org.logicahealth.sandboxmanagerapi.services.LaunchScenarioService;
import org.logicahealth.sandboxmanagerapi.services.UserLaunchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Service
public class CdsServiceEndpointServiceImpl implements CdsServiceEndpointService {
    private static Logger LOGGER = LoggerFactory.getLogger(CdsServiceEndpointServiceImpl.class.getName());

    private final CdsServiceEndpointRepository repository;
    private CdsHookService cdsHookService;
    private LaunchScenarioService launchScenarioService;
    private UserLaunchService userLaunchService;

    @Value("${hspc.platform.validCdsHooks}")
    private String[] validCdsHooks;

    @Value("${hspc.platform.deprecatedCdsHooks}")
    private String[] deprecatedCdsHooks;

    private static final String INVALID_CDS_HOOKS = "invalidCdsHooks";
    private static final int VALID_CDS_HOOKS_INDEX = 0;
    private static final int INVALID_CDS_HOOKS_INDEX = 1;

    @Inject
    public CdsServiceEndpointServiceImpl(final CdsServiceEndpointRepository cdsServiceEndpointRepository) {
        this.repository = cdsServiceEndpointRepository;
    }

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
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - save");

        CdsServiceEndpoint retVal = repository.save(cdsServiceEndpoint);

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - save: "
        +"Parameters: cdsServiceEndpoint = "+cdsServiceEndpoint
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public void delete(final int id) {

        LOGGER.info("Inside CdsServiceEndpointServiceImpl - delete");

        repository.deleteById(id);

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - delete: "
        +"Parameters: id = "+id+"; No return value");
    }

    @Override
    @Transactional
    public void delete(final CdsServiceEndpoint cdsServiceEndpoint) {
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - delete");

        // Delete all associated CDS-Hook and  CDS-Hook Launch Scenarios
        List<CdsHook> cdsHooks = cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId());
        for (CdsHook cdsHook : cdsHooks) {
            for (LaunchScenario launchScenario : launchScenarioService.findByCdsHookIdAndSandboxId(cdsHook.getId(), cdsServiceEndpoint.getSandbox()
                                                                                                                                      .getSandboxId())) {
                for (UserLaunch userLaunch : userLaunchService.findByLaunchScenarioId(launchScenario.getId())) {
                    userLaunchService.delete(userLaunch.getId());
                }
                launchScenarioService.delete(launchScenario.getId());
            }
            cdsHookService.delete(cdsHook);
        }
        delete(cdsServiceEndpoint.getId());

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - delete: "
        +"Parameters: cdsServiceEndpoint = "+cdsServiceEndpoint+"; No return value");

    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public List<CdsServiceEndpoint> create(final CdsServiceEndpoint cdsServiceEndpoint, final Sandbox sandbox) {
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - create");

        CdsServiceEndpoint existingCdsServiceEndpoint = findByCdsServiceEndpointUrlAndSandboxId(cdsServiceEndpoint.getUrl(), cdsServiceEndpoint.getSandbox()
                                                                                                                                               .getSandboxId());
        if (existingCdsServiceEndpoint != null) {
            cdsServiceEndpoint.setId(existingCdsServiceEndpoint.getId());

            List<CdsServiceEndpoint> retVal = List.of(update(cdsServiceEndpoint));

            LOGGER.debug("Inside CdsServiceEndpointServiceImpl - create: "
            +"Parameters: cdsServiceEndpoint = "+cdsServiceEndpoint+", sandbox = "+sandbox
            +"; Return value = "+retVal);

            return retVal;
        }
        Timestamp timestamp = new Timestamp(new Date().getTime());
        cdsServiceEndpoint.setCreatedTimestamp(timestamp);
        cdsServiceEndpoint.setLastUpdated(timestamp);
        var cdsEndpoints = separateOutInvalidCdsHooks(cdsServiceEndpoint);
        CdsServiceEndpoint cdsServiceEndpointSaved = save(cdsEndpoints.get(VALID_CDS_HOOKS_INDEX));
        List<CdsHook> cdsHooks = cdsEndpoints.get(VALID_CDS_HOOKS_INDEX).getCdsHooks();
        for (CdsHook cdsHook : cdsHooks) {
            cdsHook.setCdsServiceEndpointId(cdsServiceEndpointSaved.getId());
            cdsHook.setHookUrl(cdsServiceEndpointSaved.getUrl() + "/" + cdsHook.getHookId());
            cdsHookService.create(cdsHook);
        }

        List<CdsServiceEndpoint> retVal = List.of(cdsServiceEndpointSaved, cdsEndpoints.get(INVALID_CDS_HOOKS_INDEX));
            
        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - create: "
        +"Parameters: cdsServiceEndpoint = "+cdsServiceEndpoint+", sandbox = "+sandbox
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public CdsServiceEndpoint update(final CdsServiceEndpoint cdsServiceEndpoint) {
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - update");

        CdsServiceEndpoint existingCdsServiceEndpoint = getById(cdsServiceEndpoint.getId());
        existingCdsServiceEndpoint.setTitle(cdsServiceEndpoint.getTitle());
        existingCdsServiceEndpoint.setDescription(cdsServiceEndpoint.getDescription());
        existingCdsServiceEndpoint.setLastUpdated(new Timestamp(new Date().getTime()));
        List<CdsHook> cdsHooks = cdsServiceEndpoint.getCdsHooks();
        List<CdsHook> existingCdsHooks = cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId());

        for (CdsHook existingCdsHook : existingCdsHooks) {
            List<CdsHook> c = cdsHooks.stream()
                                      .filter(p1 -> existingCdsHook.getHookId()
                                                                   .equals(p1.getHookId()))
                                      .collect(Collectors.toList());
            if (c.size() == 0) {
                // Delete all associated Launch Scenarios
                List<LaunchScenario> launchScenarios = launchScenarioService.findByCdsHookIdAndSandboxId(existingCdsHook.getId(), existingCdsServiceEndpoint.getSandbox()
                                                                                                                                                            .getSandboxId());
                launchScenarioService.deleteAssociatedLaunchScenarios(launchScenarios);
                cdsHookService.delete(existingCdsHook);
            }
        }

        for (CdsHook cdsHook : cdsHooks) {
            cdsHook.setCdsServiceEndpointId(cdsServiceEndpoint.getId());
            cdsHook.setHookUrl(cdsServiceEndpoint.getUrl() + "/" + cdsHook.getHookId());
            cdsHookService.create(cdsHook);
        }
        
        CdsServiceEndpoint retVal = save(existingCdsServiceEndpoint);

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - update: "
        +"Parameters: cdsServiceEndpoint = "+cdsServiceEndpoint+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public CdsServiceEndpoint getById(final int id) {
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - getById");

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - getById: "
        +"Parameters: id = "+id+"; Return value = "+repository.findById(id).orElse(null));

        return repository.findById(id)
                         .orElse(null);
    }

    @Override
    public List<CdsServiceEndpoint> findBySandboxId(final String sandboxId) {
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - findBySandboxId");

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - findBySandboxId: "
        +"Parameters: sandboxId = "+sandboxId+"; Return value = "+repository.findBySandboxId(sandboxId));

        return repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<CdsServiceEndpoint> findBySandboxIdAndCreatedByOrVisibility(final String sandboxId, final String createdBy, final Visibility visibility) {
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - findBySandboxIdAndCreatedByOrVisibility");

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - findBySandboxIdAndCreatedByOrVisibility: "
        +"Parameters: sandboxId = "+sandboxId+", createdBy = "+createdBy+", visibility = "+visibility
        +"; Return value = "+repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility));

        return repository.findBySandboxIdAndCreatedByOrVisibility(sandboxId, createdBy, visibility);
    }

    @Override
    public List<CdsServiceEndpoint> findBySandboxIdAndCreatedBy(final String sandboxId, final String createdBy) {
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - findBySandboxIdAndCreatedBy");

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - findBySandboxIdAndCreatedBy: "
        +"Parameters: sandboxId = "+sandboxId+", createdBy = "+createdBy
        +"; Return value = "+repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy));

        return repository.findBySandboxIdAndCreatedBy(sandboxId, createdBy);
    }

    @Override
    public CdsServiceEndpoint findByCdsServiceEndpointUrlAndSandboxId(final String url, final String sandboxId) {
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - findByCdsServiceEndpointUrlAndSandboxId");

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - findByCdsServiceEndpointUrlAndSandboxId: "
        +"Parameters: url = "+url+", sandboxId = "+sandboxId
        +"; Return value = "+repository.findByCdsServiceEndpointUrlAndSandboxId(url, sandboxId));

        return repository.findByCdsServiceEndpointUrlAndSandboxId(url, sandboxId);
    }

    private List<CdsServiceEndpoint> separateOutInvalidCdsHooks(final CdsServiceEndpoint cdsServiceEndpoint) {
        
        LOGGER.info("Inside CdsServiceEndpointServiceImpl - separateOutInvalidCdsHooks");

        List<CdsServiceEndpoint> cdsEndpoints = new ArrayList<>(2);
        cdsEndpoints.add(cdsServiceEndpoint);
        CdsServiceEndpoint invalidCdsHooks = new CdsServiceEndpoint();
        invalidCdsHooks.setUrl(cdsServiceEndpoint.getUrl());
        invalidCdsHooks.setTitle(INVALID_CDS_HOOKS);
        invalidCdsHooks.setCdsHooks(new ArrayList<>(cdsServiceEndpoint.getCdsHooks().size()));
        cdsEndpoints.add(invalidCdsHooks);
        var savedCdsHooks = cdsServiceEndpoint.getCdsHooks();
        cdsServiceEndpoint.setCdsHooks(new ArrayList<>(cdsServiceEndpoint.getCdsHooks().size()));
        savedCdsHooks.forEach(hook -> {
            if (isValidCdsHook(hook.getHook()) && isActiveCdsHook(hook.getHook())) {
                cdsServiceEndpoint.getCdsHooks().add(hook);
            } else {
                invalidCdsHooks.getCdsHooks().add(hook);
            }
        });

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - separateOutInvalidCdsHooks: "
        +"Parameters: cdsServiceEndpoint = "+cdsServiceEndpoint+"; Return value = "+cdsEndpoints);

        return cdsEndpoints;
    }

    private boolean isValidCdsHook(String hook) {

        LOGGER.info("Inside CdsServiceEndpointServiceImpl - isValidCdsHook");

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - isValidCdsHook: "
        +"Parameters: hook = "+hook+"; Return value = "+asList(this.validCdsHooks).contains(hook));

        return asList(this.validCdsHooks).contains(hook);
    }

    private boolean isActiveCdsHook(String hook) {

        LOGGER.info("Inside CdsServiceEndpointServiceImpl - isActiveCdsHook");

        LOGGER.debug("Inside CdsServiceEndpointServiceImpl - isActiveCdsHook: "
        +"Parameters: hook = "+hook+"; Return value = "+!asList(this.deprecatedCdsHooks).contains(hook));

        return !asList(this.deprecatedCdsHooks).contains(hook);
    }

}
