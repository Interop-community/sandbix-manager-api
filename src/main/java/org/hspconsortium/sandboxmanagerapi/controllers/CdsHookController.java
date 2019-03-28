package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.App;
import org.hspconsortium.sandboxmanagerapi.model.CdsHook;
import org.hspconsortium.sandboxmanagerapi.model.Image;
import org.hspconsortium.sandboxmanagerapi.services.CdsHookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;

import static org.springframework.http.MediaType.*;

// TODO: add endpoint to save images and cdsHook
@RestController
@RequestMapping({"/cds-hook"})
public class CdsHookController {
    private static Logger LOGGER = LoggerFactory.getLogger(AppController.class.getName());

    private final CdsHookService cdsHookService;

    @Inject
    public CdsHookController(final CdsHookService cdsHookService) {
        this.cdsHookService = cdsHookService;
    }

    @PostMapping
    @Transactional
    @ResponseBody
    public CdsHook saveImage(final HttpServletRequest request,
                                            @RequestBody CdsHook cdsServiceEndpoint) {
        return null;
    }

    //************************  Image Saving from appController ***************************

    @GetMapping(value = "/{id}/image", produces ={IMAGE_GIF_VALUE, IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE, "image/jpg"})
    @ResponseBody
    public void getFullImage(final HttpServletResponse response, @PathVariable Integer id) {
        CdsHook cdsHook = cdsHookService.getById(id);
        if (cdsHook == null) {
            throw new ResourceNotFoundException("CDS-Hook not found.");
        }
        try {
            response.setHeader("Content-Type", cdsHook.getLogo().getContentType());
            response.getOutputStream().write(cdsHook.getLogo().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/{id}/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE} )
    @Transactional
    public @ResponseBody void putFullImage(final HttpServletRequest request, @PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        CdsHook cdsHook = cdsHookService.getById(id);
        if (cdsHook == null) {
            throw new ResourceNotFoundException("CDS-Hook does not exist. Cannot upload image.");
        }
//        authorizationService.checkSandboxUserModifyAuthorization(request, cdsHook.getSandbox(), cdsHook);
        cdsHook.setLogoUri(request.getRequestURL().toString());
        cdsHookService.save(cdsHook);
        try {
            Image image = new Image();
            image.setBytes(file.getBytes());
            image.setContentType(file.getContentType());
            cdsHookService.updateCDSImage(cdsHook, image);
        } catch (IOException e) {
            if(LOGGER.isErrorEnabled()){
                LOGGER.error("Unable to update image", e);
            }
        }
    }

    @DeleteMapping(value = "/{id}/image")
    @Transactional
    public CdsHook deleteFullImage(final HttpServletRequest request, @PathVariable Integer id) {
        CdsHook cdsHook = cdsHookService.getById(id);
        if (cdsHook == null) {
            throw new ResourceNotFoundException("CDS-Hook does not exist. Cannot delete image.");
        }
//        authorizationService.checkSandboxUserModifyAuthorization(request, app.getSandbox(), app);
        return cdsHookService.deleteCDSImage(cdsHook);
    }

    //***********************************************************************************************
}
