package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.Image;
import org.logicahealth.sandboxmanagerapi.repositories.ImageRepository;
import org.logicahealth.sandboxmanagerapi.services.ImageService;
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
        repository.deleteById(id);
    }
}
