package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.CdsHook;
import org.logicahealth.sandboxmanagerapi.model.Image;
import org.logicahealth.sandboxmanagerapi.repositories.CdsHookRepository;
import org.logicahealth.sandboxmanagerapi.services.CdsHookService;
import org.logicahealth.sandboxmanagerapi.services.ImageService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class CdsHookServiceImpl implements CdsHookService {
    private static Logger LOGGER = LoggerFactory.getLogger(CdsHookServiceImpl.class.getName());

    private final CdsHookRepository repository;
    private ImageService imageService;

    @Inject
    public CdsHookServiceImpl(final CdsHookRepository repository) { this.repository = repository; }

    @Inject
    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    @Override
    @Transactional
    public CdsHook create(final CdsHook cdsHook) {
        
        LOGGER.info("Inside CdsHookServiceImpl - create");

        CdsHook existingCdsHook = findByHookIdAndCdsServiceEndpointId(cdsHook.getHookId(),
                cdsHook.getCdsServiceEndpointId());
        if (existingCdsHook != null) {
            cdsHook.setId(existingCdsHook.getId());

            CdsHook retVal = update(cdsHook);

            LOGGER.debug("Inside CdsHookServiceImpl - create: "
            +"Parameters: cdsHook = "+cdsHook+"; Return value = "+retVal);

            return retVal;
        }

        CdsHook retVal = save(cdsHook);

        LOGGER.debug("Inside CdsHookServiceImpl - create: "
        +"Parameters: cdsHook = "+cdsHook+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public CdsHook save(final CdsHook cdsHook) {

        LOGGER.info("Inside CdsHookServiceImpl - save");

        CdsHook retVal = repository.save(cdsHook);

        LOGGER.debug("Inside CdsHookServiceImpl - save: "
        +"Parameters: cdsHook = "+cdsHook+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public CdsHook update(final CdsHook cdsHook) {
        
        LOGGER.info("Inside CdsHookServiceImpl - update");

        CdsHook existingCdsHook = getById(cdsHook.getId());
        existingCdsHook.setLogo(cdsHook.getLogo());
        existingCdsHook.setLogoUri(cdsHook.getLogoUri());
        existingCdsHook.setHook(cdsHook.getHook());
        existingCdsHook.setTitle(cdsHook.getTitle());
        existingCdsHook.setDescription(cdsHook.getDescription());
        existingCdsHook.setPrefetch(cdsHook.getPrefetch());
        existingCdsHook.setHookUrl(cdsHook.getHookUrl());
        existingCdsHook.setScope(cdsHook.getScope());
        existingCdsHook.setContext(cdsHook.getContext());

        CdsHook retVal = save(existingCdsHook);

        LOGGER.debug("Inside CdsHookServiceImpl - update: "
        +"Parameters: cdsHook = "+cdsHook+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public void delete(final int id) {

        LOGGER.info("Inside CdsHookServiceImpl - delete");

        repository.deleteById(id);

        LOGGER.debug("Inside CdsHookServiceImpl - delete: "
        +"Parameters: id = "+id+"; No return value");
    }

    @Override
    @Transactional
    public void delete(CdsHook cdsHook) {
        
        LOGGER.info("Inside CdsHookServiceImpl - delete");

        if (cdsHook.getLogo() != null) {
            imageService.delete(cdsHook.getLogo().getId());
        }
        delete(cdsHook.getId());

        LOGGER.debug("Inside CdsHookServiceImpl - delete: "
        +"Parameters: cdsHook "+cdsHook+"; No return value");

    }

    @Override
    public CdsHook getById(final int id) {
        LOGGER.info("Inside CdsHookServiceImpl - getById");

        CdsHook retVal = repository.findById(id).orElse(null);

        LOGGER.debug("Inside CdsHookServiceImpl - getById: "
        +"Parameters: id = "+id+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public CdsHook updateCdsHookImage(final CdsHook cdsHook, final Image image) {

        LOGGER.info("Inside CdsHookServiceImpl - updateCdsHookImage");

        if (cdsHook.getLogo() != null) {
            imageService.delete(cdsHook.getLogo().getId());
        }
        cdsHook.setLogo(image);
        cdsHook.setLogoUri(cdsHook.getLogoUri());

        CdsHook retVal = save(cdsHook);

        LOGGER.debug("Inside CdsHookServiceImpl - updateCdsHookImage: "
        +"Parameters: cdsHook = "+cdsHook+", image = "+image+"; Return value "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public CdsHook deleteCdsHookImage(final CdsHook existingCdsHook) {
        
        LOGGER.info("Inside CdsHookServiceImpl - deleteCdsHookImage");

        if (existingCdsHook.getLogo() != null) {
            imageService.delete(existingCdsHook.getLogo().getId());
        }
        existingCdsHook.setLogoUri(null);
        existingCdsHook.setLogo(null);

        CdsHook retVal = save(existingCdsHook);

        LOGGER.debug("Inside CdsHookServiceImpl - deleteCdsHookImage: "
        +"Parameters: existingCdsHook = "+existingCdsHook+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public CdsHook findByHookIdAndCdsServiceEndpointId(final String hookId, final int cdsServiceEndpointId) {
        
        LOGGER.info("Inside CdsHookServiceImpl - findByHookIdAndCdsServiceEndpointId");

        LOGGER.debug("Inside CdsHookServiceImpl - findByHookIdAndCdsServiceEndpointId: "
        +"Parameters: hookId = "+hookId+", cdsServiceEndpointId = "+cdsServiceEndpointId
        +"; Return value = "+repository.findByHookIdAndCdsServiceEndpointId(hookId, cdsServiceEndpointId));

        return repository.findByHookIdAndCdsServiceEndpointId(hookId, cdsServiceEndpointId);
    }

    @Override
    public List<CdsHook> findByCdsServiceEndpointId(final int cdsServiceEndpointId) {
        
        LOGGER.info("Inside CdsHookServiceImpl - findByCdsServiceEndpointId");

        LOGGER.debug("Inside CdsHookServiceImpl - findByCdsServiceEndpointId: "
        +"Parameters: cdsServiceEndpointId = "+cdsServiceEndpointId
        +"; Return value = "+repository.findByCdsServiceEndpointId(cdsServiceEndpointId));

        return repository.findByCdsServiceEndpointId(cdsServiceEndpointId);
    }

}
