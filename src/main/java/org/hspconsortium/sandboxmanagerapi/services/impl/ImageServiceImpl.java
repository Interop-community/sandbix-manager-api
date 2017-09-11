package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.Image;
import org.hspconsortium.sandboxmanagerapi.repositories.ImageRepository;
import org.hspconsortium.sandboxmanagerapi.services.ImageService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository repository;

    @Inject
    public ImageServiceImpl(final ImageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Image save(final Image image) {
        return repository.save(image);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }
}
