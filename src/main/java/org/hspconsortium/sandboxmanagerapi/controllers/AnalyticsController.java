package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.UserRole;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.web.bind.annotation.*;
import org.hspconsortium.sandboxmanagerapi.model.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.*;

@RestController
@RequestMapping({"/analytics"})
public class AnalyticsController extends AbstractController {

    private AnalyticsService analyticsService;
    private UserService userService;
    private SandboxService sandboxService;
    private AppService appService;

    @Inject
    public AnalyticsController(final AnalyticsService analyticsService,
                               final UserService userService,
                               final SandboxService sandboxService,
                               final AppService appService,
                               final OAuthService oAuthService) {
        super(oAuthService);
        this.analyticsService = analyticsService;
        this.userService = userService;
        this.sandboxService = sandboxService;
        this.appService = appService;
    }

    @GetMapping(value = "/sandboxes", params = {"userId"})
    public @ResponseBody
    Integer countSandboxesByUser(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
//        checkUserAuthorization(request, userId);

        User primaryUser = userService.findBySbmUserId(userId);
        if (primaryUser == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        List<Sandbox> userCreatedSandboxes = analyticsService.sandboxesCreatedByUser(primaryUser);
        return userCreatedSandboxes.size();
    }

    @GetMapping(value = "/users", params = {"userId"})
    public @ResponseBody HashMap<String, Integer> countUsersBySandbox(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
//        checkUserAuthorization(request, userId);
        User primaryUser = userService.findBySbmUserId(userId);
        if (primaryUser == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        HashMap<String, Integer> sandboxUsers = new HashMap<>();
        List<Sandbox> userCreatedSandboxes = analyticsService.sandboxesCreatedByUser(primaryUser);
        for (Sandbox sandbox: userCreatedSandboxes) {
            List<UserRole> usersRoles = sandbox.getUserRoles();
            Map<String, UserRole> uniqueUsers = new LinkedHashMap<>();
            for (UserRole userRole : usersRoles) {
                uniqueUsers.put(userRole.getUser().getEmail(), userRole);
            }
            sandboxUsers.put(sandbox.getSandboxId(), uniqueUsers.size());
        }

        return sandboxUsers;
    }

    @GetMapping(value = "/apps", params = {"userId"})
    public @ResponseBody HashMap<String, Integer> countAppsBySandbox(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
//        checkUserAuthorization(request, userId);
        User primaryUser = userService.findBySbmUserId(userId);
        if (primaryUser == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        HashMap<String, Integer> sandboxApps = new HashMap<>();
        List<Sandbox> userCreatedSandboxes = analyticsService.sandboxesCreatedByUser(primaryUser);
        for (Sandbox sandbox: userCreatedSandboxes) {
            String sandboxId = sandbox.getSandboxId();
            sandboxApps.put(sandboxId, appService.findBySandboxId(sandboxId).size());
        }
        return sandboxApps;
    }

    @GetMapping(value = "/memory", params = {"userId"})
    public @ResponseBody Double memoryUsedByUser(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
//        checkUserAuthorization(request, userId);
        Double memoryUseInMB = 0.0;
        User primaryUser = userService.findBySbmUserId(userId);
        if (primaryUser == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        List<Sandbox> userCreatedSandboxes = analyticsService.sandboxesCreatedByUser(primaryUser);
        for (Sandbox sandbox: userCreatedSandboxes) {
            memoryUseInMB += analyticsService.retrieveMemoryInSchema(sandbox.getSandboxId());
        }
        return memoryUseInMB;
    }
}
