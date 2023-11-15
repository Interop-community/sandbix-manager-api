package org.logicahealth.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.model.UserAccessHistory;
import org.logicahealth.sandboxmanagerapi.services.AuthorizationService;
import org.logicahealth.sandboxmanagerapi.services.SandboxService;
import org.logicahealth.sandboxmanagerapi.services.UserAccessHistoryService;
import org.logicahealth.sandboxmanagerapi.services.UserService;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping({"/sandbox-access"})
public class UserAccessHistoryController {
    private static Logger LOGGER = LoggerFactory.getLogger(UserAccessHistoryController.class.getName());

    private SandboxService sandboxService;
    private UserAccessHistoryService userAccessHistoryService;
    private UserService userService;
    private AuthorizationService authorizationService;

    public UserAccessHistoryController(final SandboxService sandboxService, final UserAccessHistoryService userAccessHistoryService,
                                       final UserService userService, final AuthorizationService authorizationService) {
        this.sandboxService = sandboxService;
        this.userAccessHistoryService = userAccessHistoryService;
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @GetMapping(params = {"sandboxId"})
    public @ResponseBody
    List<UserAccessHistory> getLastSandboxAccessWithSandboxId(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException {
        
        LOGGER.info("getLastSandboxAccessWithSandboxId");
        
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        return userAccessHistoryService.getLatestUserAccessHistoryInstancesWithSandbox(sandbox);
    }

    @GetMapping(params = {"sbmUserId"})
    public @ResponseBody
    List<UserAccessHistory> getLastSandboxAccessWithSbmUserId(final HttpServletRequest request, @RequestParam(value = "sbmUserId") String userIdEncoded) throws UnsupportedEncodingException {
        
        LOGGER.info("getLastSandboxAccessWithSbmUserId");
        
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        return userAccessHistoryService.getLatestUserAccessHistoryInstancesWithSbmUser(user);
    }

    @GetMapping(params = {"sbmUserId", "sandboxId"})
    public @ResponseBody
    Timestamp getLastSandboxAccess(final HttpServletRequest request, @RequestParam(value = "sbmUserId") String userIdEncoded,
                                   @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException {
        
        LOGGER.info("getLastSandboxAccess");
        
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        authorizationService.checkSandboxUserReadAuthorization(request, sandbox);
        return userAccessHistoryService.getLatestUserAccessHistoryInstance(sandbox, user);
    }
}
