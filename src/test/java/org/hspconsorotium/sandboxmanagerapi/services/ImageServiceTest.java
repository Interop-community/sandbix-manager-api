package org.hspconsorotium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Image;
import org.hspconsortium.sandboxmanagerapi.repositories.ImageRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.ImageServiceImpl;
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
        verify(repository).delete(image.getId());
    }
}
