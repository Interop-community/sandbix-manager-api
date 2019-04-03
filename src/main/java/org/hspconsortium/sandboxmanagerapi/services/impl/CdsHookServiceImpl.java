package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.CdsHook;
import org.hspconsortium.sandboxmanagerapi.model.CdsServiceEndpoint;
import org.hspconsortium.sandboxmanagerapi.model.Image;
import org.hspconsortium.sandboxmanagerapi.repositories.CdsHookRepository;
import org.hspconsortium.sandboxmanagerapi.services.CdsHookService;
import org.hspconsortium.sandboxmanagerapi.services.ImageService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class CdsHookServiceImpl implements CdsHookService {

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
    public CdsHook save(final CdsHook cdsHook) {
        return repository.save(cdsHook);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(CdsHook cdsHook) {
        if (cdsHook.getLogo() != null) {
            imageService.delete(cdsHook.getLogo().getId());
        }
        delete(cdsHook.getId());
    }

    @Override
    public CdsHook getById(final int id) {
        return repository.findOne(id);
    }

    @Override
    @Transactional
    public CdsHook updateCdsHookImage(final CdsHook cdsHook, final Image image) {
        if (cdsHook.getLogo() != null) {
            imageService.delete(cdsHook.getLogo().getId());
        }
        cdsHook.setLogo(image);
        cdsHook.setLogoUri(cdsHook.getLogoUri());
        return save(cdsHook);
    }

    @Override
    @Transactional
    public CdsHook deleteCdsHookImage(final CdsHook existingCdsHook) {
        if (existingCdsHook.getLogo() != null) {
            imageService.delete(existingCdsHook.getLogo().getId());
        }
        existingCdsHook.setLogoUri(null);
        existingCdsHook.setLogo(null);
        return save(existingCdsHook);
    }

    @Override
    public CdsHook findByHookIdAndCdsServiceEndpointId(final String hookId, final int cdsServiceEndpointId) {
        return repository.findByHookIdAndCdsServiceEndpointId(hookId, cdsServiceEndpointId);
    }

}
