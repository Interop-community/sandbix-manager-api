package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Image;

public interface ImageService {

    Image save(final Image image);

    void delete(final int id);
}
