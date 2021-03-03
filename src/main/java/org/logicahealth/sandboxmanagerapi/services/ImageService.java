package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Image;

public interface ImageService {

    Image save(final Image image);

    void delete(final int id);
}
