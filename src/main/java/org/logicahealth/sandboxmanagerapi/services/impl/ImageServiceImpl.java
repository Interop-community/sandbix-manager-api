package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.Image;
import org.logicahealth.sandboxmanagerapi.repositories.ImageRepository;
import org.logicahealth.sandboxmanagerapi.services.ImageService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ImageServiceImpl implements ImageService {
    private static Logger LOGGER = LoggerFactory.getLogger(ImageServiceImpl.class.getName());

    private final ImageRepository repository;

    @Inject
    public ImageServiceImpl(final ImageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Image save(final Image image) {

        LOGGER.info("Inside ImageServiceImpl - save");

        Image retVal = repository.save(image);

        LOGGER.debug("Inside ImageServiceImpl - save: "
        +"Parameters: image = "+image+"; Return value = "+retVal);
        
        return retVal;
    }

    @Override
    @Transactional
    public void delete(final int id) {
        
        LOGGER.info("Inside ImageServiceImpl - delete");

        repository.deleteById(id);

        LOGGER.debug("Inside ImageServiceImpl - delete: "
        +"Parameters: id = "+id+"; No return value");

    }
}
