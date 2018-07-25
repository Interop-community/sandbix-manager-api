package org.hspconsortium.sandboxmanagerapi.controllers;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hspconsortium.sandboxmanagerapi.model.SmartApp;
import org.hspconsortium.sandboxmanagerapi.model.SmartAppCompositeId;
import org.hspconsortium.sandboxmanagerapi.model.Visibility2;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.hspconsortium.sandboxmanagerapi.services.SmartAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/smartapp")
@Slf4j
@AllArgsConstructor
public class SmartAppController {

    @Inject
    private SmartAppService smartAppService;

    @Inject
    private OAuthService oAuthService;

    // todo figure out how spring security works here
    // todo see if performedBy has rights
    @PutMapping(value = "/{smartAppId}", params = {"sandboxId"})
    public ResponseEntity<SmartApp> save(final @NonNull HttpServletRequest request, final @PathVariable @NonNull String smartAppId,
                                         final @RequestParam(value = "sandboxId") String sandboxId, final @RequestBody @NonNull SmartApp smartApp) {
        Assert.isTrue(smartAppId.equals(smartApp.getSmartAppId()), "ID must match smartApp.ID");
        Assert.isTrue(sandboxId.equals(smartApp.getSandboxId()), "ID must match smartApp.ID");

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
}
