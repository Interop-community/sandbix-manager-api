package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping({"/analytics"})
public class AnalyticsController extends AbstractController {

    private AnalyticsService analyticsService;
    private UserService userService;
    private SandboxService sandboxService;
    private AppService appService;
    private RuleService ruleService;
    private UserPersonaService userPersonaService;
    private UserAccessHistoryService userAccessHistoryService;

    @Inject
    public AnalyticsController(final AnalyticsService analyticsService,
                               final UserService userService,
                               final SandboxService sandboxService,
                               final AppService appService,
                               final OAuthService oAuthService,
                               final RuleService ruleService,
                               final UserPersonaService userPersonaService,
                               final UserAccessHistoryService userAccessHistoryService) {
        super(oAuthService);
        this.analyticsService = analyticsService;
        this.userService = userService;
        this.sandboxService = sandboxService;
        this.appService = appService;
        this.ruleService = ruleService;
        this.userPersonaService = userPersonaService;
        this.userAccessHistoryService = userAccessHistoryService;
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
        List<Sandbox> userCreatedSandboxes = sandboxService.findByPayerId(primaryUser.getId());
        return userCreatedSandboxes.size();
    }

    @GetMapping(value = "/users", params = {"userId"})
    public @ResponseBody HashMap<String, Integer> countUsersBySandbox(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
//        checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        return analyticsService.countUsersPerSandboxByUser(user);
    }

    @GetMapping(value = "/apps", params = {"userId"})
    public @ResponseBody HashMap<String, Integer> countAppsBySandbox(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
//        checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        return analyticsService.countAppsPerSandboxByUser(user);
    }

    @GetMapping(value = "/memory", params = {"userId"})
    public @ResponseBody Double memoryUsedByUser(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
//        checkUserAuthorization(request, userId);
        Double memoryUseInMB = 0.0;
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
       return analyticsService.retrieveTotalMemoryByUser(user);
    }

    @PostMapping(value = "/transaction")
    public @ResponseBody
    FhirTransaction handleFhirTransaction(final HttpServletRequest request, @RequestBody final HashMap transactionInfo) {
        Sandbox sandbox = sandboxService.findBySandboxId(transactionInfo.get("tenant").toString());
        String userId = transactionInfo.get("userId").toString();
        User user;
        if(transactionInfo.get("secured").toString().equals("true")) {
            user = userService.findBySbmUserId(userId);
            if (user != null) {
                try {
                    checkSystemUserCanMakeTransaction(sandbox, user);
                } catch (UnauthorizedException e) {
                    throw new UnauthorizedException("User does not have access to this sandbox");
                }
            } else {
                try {
                    UserPersona userPersona = userPersonaService.findByPersonaUserId(userId);
                    checkIfPersonaAndHasAuthority(sandbox, userPersona);
                } catch (UnauthorizedException e2) {
                    throw new UnauthorizedException("Persona does not have access to this sandbox");
                }
            }

        } else {
            user = null;
        }
        return analyticsService.handleFhirTransaction(user, transactionInfo);
    }

    @GetMapping(value = "/last-sandbox-access", params = {"userId", "sandboxId"})
    public @ResponseBody
    Timestamp getLastSandboxAccess(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded,
                                   @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
//        checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        return userAccessHistoryService.getLatestUserAccessHistoryInsance(sandbox, user);
    }
}
