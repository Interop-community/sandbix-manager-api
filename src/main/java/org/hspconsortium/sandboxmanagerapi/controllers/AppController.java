/*
 * #%L
 *
 * %%
 * Copyright (C) 2014 - 2015 Healthcare Services Platform Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.apache.http.HttpStatus;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
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
import java.util.List;

import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping({"/app"})
public class AppController extends AbstractController {
    private static Logger LOGGER = LoggerFactory.getLogger(AppController.class.getName());

    private final AppService appService;
    private final SandboxService sandboxService;
    private final UserService userService;
    private final RuleService ruleService;

    @Inject
    public AppController(final AppService appService, final OAuthService oAuthService,
                         final SandboxService sandboxService, final UserService userService,
                         final RuleService ruleService) {
        super(oAuthService);
        this.appService = appService;
        this.sandboxService = sandboxService;
        this.userService = userService;
        this.ruleService = ruleService;
    }

    @PostMapping
    @Transactional
    public @ResponseBody App createApp(final HttpServletRequest request, @RequestBody App app) {
        Sandbox sandbox = sandboxService.findBySandboxId(app.getSandbox().getSandboxId());
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox specified in app not found.");
        }
        if (!ruleService.checkIfUserCanCreateApp(sandbox)) {
            return null;
        }
        String sbmUserId = checkSandboxUserCreateAuthorization(request, sandbox);
        checkCreatedByIsCurrentUserAuthorization(request, app.getCreatedBy().getSbmUserId());

        app.setSandbox(sandbox);
        User user = userService.findBySbmUserId(sbmUserId);
        app.setVisibility(getDefaultVisibility(user, sandbox));
        app.setCreatedBy(user);
        return appService.create(app, sandbox);
    }

    @GetMapping(params = {"sandboxId"})
    public @ResponseBody List<App> getApps(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId) {
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        String sbmUserId = checkSandboxUserReadAuthorization(request, sandbox);
        return appService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, sbmUserId, Visibility.PUBLIC);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody App getApp(final HttpServletRequest request, @PathVariable Integer id) {
        App app = appService.getById(id);
        if (app != null) {
            checkSandboxUserReadAuthorization(request, app.getSandbox());
            return appService.getClientJSON(app);
        } else {
            throw new ResourceNotFoundException("Could not find app.");
        }
    }

    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody void deleteApp(final HttpServletRequest request, @PathVariable Integer id) {
        App app = appService.getById(id);
        if (app != null) {
            checkSandboxUserModifyAuthorization(request, app.getSandbox(), app);
            appService.delete(app);
        } else {
            throw new ResourceNotFoundException("Could not find app.");
        }
    }

    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @Transactional
    public @ResponseBody App updateApp(final HttpServletRequest request, @PathVariable Integer id, @RequestBody App app) {
        App existingApp = appService.getById(id);
        if (existingApp == null || existingApp.getId().intValue() != id.intValue()) {
            throw new RuntimeException(String.format("Response Status : %s.\n" +
                            "Response Detail : App Id doesn't match Id in JSON body."
                    , HttpStatus.SC_BAD_REQUEST));
        }
        checkSandboxUserModifyAuthorization(request, existingApp.getSandbox(), existingApp);
        return appService.update(app);
    }


    @GetMapping(value = "/{id}/image", produces ={IMAGE_GIF_VALUE, IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE, "image/jpg"})
    public @ResponseBody void getFullImage(final HttpServletResponse response, @PathVariable Integer id) {
        App app = appService.getById(id);
        if (app == null) {
            throw new ResourceNotFoundException("App not found.");
        }
        try {
            response.setHeader("Content-Type", app.getLogo().getContentType());
            response.getOutputStream().write(app.getLogo().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/{id}/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE} )
    @Transactional
    public @ResponseBody void putFullImage(final HttpServletRequest request, @PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        App app = appService.getById(id);
        if (app == null) {
            throw new ResourceNotFoundException("App does not exist. Cannot upload image.");
        }
        checkSandboxUserModifyAuthorization(request, app.getSandbox(), app);
        app.setLogoUri(request.getRequestURL().toString());
        appService.save(app);
        try {
            Image image = new Image();
            image.setBytes(file.getBytes());
            image.setContentType(file.getContentType());
            appService.updateAppImage(app, image);
        } catch (IOException e) {
            if(LOGGER.isErrorEnabled()){
                LOGGER.error("Unable to update image", e);
            }
        }
    }

    @DeleteMapping(value = "/{id}/image")
    @Transactional
    public App deleteFullImage(final HttpServletRequest request, @PathVariable Integer id) {
        App app = appService.getById(id);
        if (app == null) {
            throw new ResourceNotFoundException("App does not exist. Cannot delete image.");
        }
        checkSandboxUserModifyAuthorization(request, app.getSandbox(), app);
        return appService.deleteAppImage(app);
    }
}
