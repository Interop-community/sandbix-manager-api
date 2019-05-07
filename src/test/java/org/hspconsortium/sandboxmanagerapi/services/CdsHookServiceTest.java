package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.App;
import org.hspconsortium.sandboxmanagerapi.model.CdsHook;
import org.hspconsortium.sandboxmanagerapi.model.Image;
import org.hspconsortium.sandboxmanagerapi.repositories.CdsHookRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.CdsHookServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class CdsHookServiceTest {

    private CdsHookRepository cdsHookRepository = mock(CdsHookRepository.class);

    private ImageService imageService = mock(ImageService.class);

    private CdsHookServiceImpl cdsHookService = new CdsHookServiceImpl(cdsHookRepository);

    private Image logo;
    private CdsHook cdsHook;
    private List<CdsHook> cdsHooks;

    @Before
    public void setup() {
        cdsHookService.setImageService(imageService);

        cdsHook = new CdsHook();
        cdsHook.setId(1);
        cdsHook.setHook("patient-view");
        cdsHook.setTitle("Sends a Demo Info Card");
        cdsHook.setHookUrl("http://www.google.com");
        cdsHook.setHookId("demo-suggestion-card");
        cdsHook.setCdsServiceEndpointId(1);

        logo = new Image();
        logo.setId(1);
        cdsHook.setLogo(logo);

        cdsHooks = new ArrayList<>();
        cdsHooks.add(cdsHook);
    }

    @Test
    public void saveTest() {
        when(cdsHookRepository.save(cdsHook)).thenReturn(cdsHook);
        assertEquals(cdsHook, cdsHookService.save(cdsHook));
    }

    @Test
    public void createTest() {
        when(cdsHookService.findByHookIdAndCdsServiceEndpointId(cdsHook.getHookId(), 1)).thenReturn(null);
        when(cdsHookRepository.save(cdsHook)).thenReturn(cdsHook);
        when(cdsHookService.save(cdsHook)).thenReturn(cdsHook);
        assertEquals(cdsHook, cdsHookService.create(cdsHook));
    }

    @Test
    public void createTestIfCdsHookExists() {
        when(cdsHookService.findByHookIdAndCdsServiceEndpointId(cdsHook.getHookId(), 1)).thenReturn(cdsHook);
        when(cdsHookService.getById(cdsHook.getId())).thenReturn(cdsHook);
        when(cdsHookRepository.save(cdsHook)).thenReturn(cdsHook);
        assertEquals(cdsHook, cdsHookService.create(cdsHook));
    }

    @Test
    public void updateTest() {
        when(cdsHookService.save(cdsHook)).thenReturn(cdsHook);
        when(cdsHookService.getById(cdsHook.getId())).thenReturn(cdsHook);
        assertEquals(cdsHook, cdsHookService.update(cdsHook));
    }

    @Test
    public void deletById() {
        cdsHookService.delete(1);
        verify(cdsHookRepository).delete(1);
    }

    @Test
    public void deleteWithCdsHookTest() {
        cdsHookService.delete(cdsHook);
        verify(imageService).delete(logo.getId());
    }

    @Test
    public void deleteWithCdsHookLogoNullTest() {
        cdsHook.setLogo(null);
        cdsHookService.delete(cdsHook);
        verify(cdsHookRepository).delete(1);
    }

    @Test
    public void getByIdTest() {
        when(cdsHookRepository.findOne(cdsHook.getId())).thenReturn(cdsHook);
        assertEquals(cdsHook, cdsHookService.getById(cdsHook.getId()));
    }

    @Test
    public void updateCdsHookImageTest() {
        cdsHookService.updateCdsHookImage(cdsHook, logo);
        verify(imageService, atLeastOnce()).delete(cdsHook.getLogo().getId());
    }

    @Test
    public void updateCdsHookImageLogoNullTest() {
        cdsHook.setLogo(null);
        cdsHookService.updateCdsHookImage(cdsHook, logo);
        verify(cdsHookRepository).save(cdsHook);
    }

    @Test
    public void deleteCdsHookImageTest() {
        when(cdsHookService.save(cdsHook)).thenReturn(cdsHook);
        assertEquals(cdsHook, cdsHookService.deleteCdsHookImage(cdsHook));
    }

    @Test
    public void deleteCdsHookImageLogoNullTest() {
        cdsHook.setLogo(null);
        when(cdsHookService.save(cdsHook)).thenReturn(cdsHook);
        assertEquals(cdsHook, cdsHookService.deleteCdsHookImage(cdsHook));
    }

    @Test
    public void findByHookIdAndCdsServiceEndpointIdTest() {
        when(cdsHookRepository.findByHookIdAndCdsServiceEndpointId(cdsHook.getHookId(), 1)).thenReturn(cdsHook);
        assertEquals(cdsHook, cdsHookService.findByHookIdAndCdsServiceEndpointId(cdsHook.getHookId(), 1));
    }

    @Test
    public void findByCdsServiceEndpointId() {
        when(cdsHookRepository.findByCdsServiceEndpointId(1)).thenReturn(cdsHooks);
        assertEquals(cdsHooks, cdsHookService.findByCdsServiceEndpointId(1));
    }
}
