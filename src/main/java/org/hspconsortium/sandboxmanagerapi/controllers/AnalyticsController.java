package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.springframework.security.oauth2.common.exceptions.UserDeniedAuthorizationException;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
    private SandboxActivityLogService sandboxActivityLogService;

    @Inject
    public AnalyticsController(final AnalyticsService analyticsService,
                               final UserService userService,
                               final SandboxService sandboxService,
                               final AppService appService,
                               final OAuthService oAuthService,
                               final RuleService ruleService,
                               final UserPersonaService userPersonaService,
                               final UserAccessHistoryService userAccessHistoryService,
                               final SandboxActivityLogService sandboxActivityLogService) {
        super(oAuthService);
        this.analyticsService = analyticsService;
        this.userService = userService;
        this.sandboxService = sandboxService;
        this.appService = appService;
        this.ruleService = ruleService;
        this.userPersonaService = userPersonaService;
        this.userAccessHistoryService = userAccessHistoryService;
        this.sandboxActivityLogService = sandboxActivityLogService;
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

    // TODO: remove after beta testing
    @GetMapping(value = "/beta-use", params = {"userId"})
    public @ResponseBody List<SandboxActivityLog> getBetaSandboxUsages(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        Boolean isAdmin = checkUserHasSystemRole(user, SystemRole.ADMIN);
        if (isAdmin) {
            return sandboxActivityLogService.findBySandboxActivity(SandboxActivity.LOGGED_IN_BETA);
        } else {
            throw new UserDeniedAuthorizationException("User denied access.");
        }

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
            } else if (transactionInfo.get("secured").equals("false")) {
                // do nothing
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

    @GetMapping(produces = APPLICATION_JSON_VALUE, params = {"interval"})
    public @ResponseBody String getSandboxStatistics(HttpServletRequest request, @RequestParam(value = "interval") String intervalDays) throws UnsupportedEncodingException {
        User user = userService.findBySbmUserId(getSystemUserId(request));
        checkUserSystemRole(user, SystemRole.ADMIN);
        return analyticsService.getSandboxStatistics(intervalDays);
    }

}
