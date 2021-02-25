package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Image;
import org.logicahealth.sandboxmanagerapi.repositories.ImageRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.ImageServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImageServiceTest {

    private ImageRepository repository = mock(ImageRepository.class);

    private ImageServiceImpl imageService = new ImageServiceImpl(repository);

    private Image image;

    @Before
    public void setup() {
        image = new Image();
        image.setId(1);
    }

    @Test
    public void saveTest() {
        when(repository.save(image)).thenReturn(image);
        Image returnedImage = imageService.save(image);
        assertEquals(image, returnedImage);
    }

    @Test
    public void deleteTest() {
        imageService.delete(image.getId());
        verify(repository).deleteById(image.getId());
    }
}
