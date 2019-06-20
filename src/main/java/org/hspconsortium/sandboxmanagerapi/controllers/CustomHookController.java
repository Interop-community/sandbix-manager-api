package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.CustomHook;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping({"/customHook"})
public class CustomHookController {

    private final CustomHookService customHookService;
    private final SandboxService sandboxService;
    private final AuthorizationService authorizationService;
    private final RuleService ruleService;
    private final UserService userService;

    @Inject
    public CustomHookController(final CustomHookService customHookService,
                                final SandboxService sandboxService,
                                final AuthorizationService authorizationService,
                                final RuleService ruleService, final UserService userService) {
        this.customHookService = customHookService;
        this.sandboxService = sandboxService;
        this.authorizationService = authorizationService;
        this.ruleService = ruleService;
        this.userService = userService;
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE)
    @Transactional
    @ResponseBody
    public Iterable<CustomHook> createCustomHooks(final HttpServletRequest request, @RequestBody List<CustomHook> customHooks) {

        for (CustomHook customHook: customHooks) {
            Sandbox sandbox = sandboxService.findBySandboxId(customHook.getSandbox().getSandboxId());
            if (sandbox == null) {
                throw new ResourceNotFoundException("Sandbox specified in Hook was not found");
            }
            String sbmUserId = authorizationService.checkSandboxUserNotReadOnlyAuthorization(request, sandbox);
            User user = userService.findBySbmUserId(sbmUserId);
            customHook.setCreatedBy(user);
            customHook.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            customHook.setSandbox(sandbox);
            customHook.setVisibility(authorizationService.getDefaultVisibility(user, sandbox));
        }

        return customHookService.createCustomHooks(customHooks);
    }

    @GetMapping(params = {"sandboxId"})
    @ResponseBody
    public List<CustomHook> getCustomHooks(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId) {
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found");
        }
        return customHookService.findBySandboxId(sandboxId);
    }
}
