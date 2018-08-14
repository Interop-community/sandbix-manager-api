package org.hspconsortium.sandboxmanagerapi.controllers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.SmartAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.IMAGE_GIF_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RestController
@RequestMapping("/smartapp")
@Slf4j
public class SmartAppController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(SmartAppController.class.getName());

    @Inject
    private SmartAppService smartAppService;

    @Inject
    private OAuthService oAuthService;

    @Inject
    private SandboxService sandboxService;

    @Builder
    public SmartAppController(OAuthService oAuthService){
        super(oAuthService);
    }

    // todo figure out how spring security works here
    // todo see if performedBy has rights
    @PostMapping(value = "", params = {"sandboxId"})
    public ResponseEntity<SmartApp> save(final @NonNull HttpServletRequest request, final @RequestParam(value = "sandboxId") String sandboxId,
                                         final @RequestBody @NonNull SmartApp smartApp) {
        Assert.isTrue(sandboxId.equals(smartApp.getSandboxId()), "ID must match smartApp.sandbox.ID");
        checkSandboxUserModifySmartAppAuthorization(request, sandboxService.findBySandboxId(sandboxId), smartApp);
        String oAuthUserId = oAuthService.getOAuthUserId(request);
        log.info("Saving Smart App performed by: " + oAuthUserId);
        smartApp.setSandboxId(sandboxId);
        smartApp.setSmartAppId(UUID.randomUUID().toString().toLowerCase());
        SmartApp result = smartAppService.save(smartApp);
        return ResponseEntity.ok(result);
    }

    @PutMapping(value = "/{smartAppId}", params = {"sandboxId"})
    public ResponseEntity<SmartApp> update(final @NonNull HttpServletRequest request, final @PathVariable @NonNull String smartAppId,
                                         final @RequestParam(value = "sandboxId") String sandboxId, final @RequestBody @NonNull SmartApp smartApp) {
        Assert.isTrue(smartAppId.equals(smartApp.getSmartAppId()), "ID must match smartApp.ID");
        Assert.isTrue(sandboxId.equals(smartApp.getSandboxId()), "ID must match smartApp.sandbox.ID");

        checkSandboxUserModifySmartAppAuthorization(request, sandboxService.findBySandboxId(sandboxId), smartApp);
        String oAuthUserId = oAuthService.getOAuthUserId(request);
        log.info("Saving Smart App: " + smartAppId + ", performed by: " + oAuthUserId);
        SmartApp result = smartAppService.save(smartApp);
        return ResponseEntity.ok(result);
    }

    // todo decide if this call should be public as it doesn't harm anything
    @GetMapping(value = "/{smartAppId}", params = {"sandboxId"})
    public ResponseEntity<SmartApp> get(final @NonNull HttpServletRequest request, final @PathVariable @NonNull String smartAppId,
                                        final @RequestParam(value = "sandboxId") String sandboxId) {
        String oAuthUserId = oAuthService.getOAuthUserId(request);
        log.info("Retrieving smart app: " + smartAppId + ", performed by: " + oAuthUserId);
        SmartApp result = smartAppService.getById(smartAppId, sandboxId);
        if (result != null) {
            log.info("Found smart app: " + smartAppId);
            return ResponseEntity.ok(result);
        } else {
            log.info("Could not find smart app: " + smartAppId);
            return ResponseEntity.notFound().build();
        }
    }

    // todo figure out how spring security works here
    @DeleteMapping(value = "/{smartAppId}", params = {"sandboxId"})
    public ResponseEntity<SmartApp> delete(final HttpServletRequest request, final @PathVariable String smartAppId,
                                           final @RequestParam(value = "sandboxId") String sandboxId) {
        SmartApp smartApp = smartAppService.getById(smartAppId, sandboxId);
        checkSandboxUserModifySmartAppAuthorization(request, sandboxService.findBySandboxId(sandboxId), smartApp);
        String oAuthUserId = oAuthService.getOAuthUserId(request);
        log.info("Deleting Smart App: " + smartAppId + ", performed by: " + oAuthUserId);
        smartAppService.delete(smartAppId, sandboxId);
        return ResponseEntity.ok().build();
    }

    // todo figure out how spring security works here
    @GetMapping(value = "", params = "ownerId")
    public ResponseEntity<List<SmartApp>> findByOwnerId(final HttpServletRequest request, final @RequestParam("ownerId") int ownerId) {
        String oAuthUserId = oAuthService.getOAuthUserId(request);
        log.info("Finding Smart Apps for owner: " + ownerId + ", performed by: " + oAuthUserId);
        List<SmartApp> result = smartAppService.findByOwnerId(ownerId);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "", params = "sandboxId")
    public ResponseEntity<List<SmartApp>> findBySandboxId(final HttpServletRequest request, final @RequestParam("sandboxId") String sandboxId) {
        checkSandboxUserReadAuthorization(request, sandboxService.findBySandboxId(sandboxId));
        List<SmartApp> result = smartAppService.findBySandboxId(sandboxId);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "", params = "visibility")
    public ResponseEntity findPublic(final HttpServletRequest request, final @RequestParam("visibility") String visibility) {
        log.info("Finding Public Smart Apps");

        if (Visibility2.PUBLIC.toString().equals(visibility)) {
            List<SmartApp> result = smartAppService.findPublic();
            return ResponseEntity.ok(result);
        }

        String errorMessage = "Only visibility=PUBLIC is supported, you searched for " + visibility;
        log.warn(errorMessage);

        return ResponseEntity.badRequest().body(errorMessage);

    }

    @GetMapping(value = "/{smartAppId}/image", params = "sandboxId", produces ={IMAGE_GIF_VALUE, IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE, "image/jpg"})
    public @ResponseBody void getFullImage(final HttpServletResponse response, final @RequestParam("sandboxId") String sandboxId, @PathVariable String smartAppId) {

        SmartApp smartApp = smartAppService.getById(smartAppId, sandboxId);
        try {
            response.setHeader("Content-Type", smartApp.getLogo().getContentType());
            response.getOutputStream().write(smartApp.getLogo().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/{smartAppId}/image", params = "sandboxId", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE} )
    @Transactional
    public @ResponseBody void putFullImage(final HttpServletRequest request, final @RequestParam("sandboxId") String sandboxId, @PathVariable String smartAppId, @RequestParam("file") MultipartFile file) {

        SmartApp smartApp = smartAppService.getById(smartAppId, sandboxId);
        checkSandboxUserModifySmartAppAuthorization(request, sandboxService.findBySandboxId(sandboxId), smartApp);
        smartApp.setLogoUri(request.getRequestURL().toString() + "?sandboxId=" + smartApp.getSandboxId());
        smartAppService.save(smartApp);
        try {
            Image image = new Image();
            image.setBytes(file.getBytes());
            image.setContentType(file.getContentType());
            smartAppService.updateAppImage(smartApp, image);
        } catch (IOException e) {
            if(LOGGER.isErrorEnabled()){
                LOGGER.error("Unable to update image", e);
            }
        }
    }
}
