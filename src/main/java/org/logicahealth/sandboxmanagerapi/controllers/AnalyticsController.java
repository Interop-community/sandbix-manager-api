package org.logicahealth.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping({"/analytics"})
public class AnalyticsController {

    private AnalyticsService analyticsService;
    private UserService userService;
    private SandboxService sandboxService;
    private AppService appService;
    private RuleService ruleService;
    private UserPersonaService userPersonaService;
    private UserAccessHistoryService userAccessHistoryService;
    private SandboxActivityLogService sandboxActivityLogService;
    private AuthorizationService authorizationService;

    @Value("${hspc.platform.sandboxesAllUsersCanAccess}")
    private String[] sandboxesAllUsersCanAccess;

    @Inject
    public AnalyticsController(final AnalyticsService analyticsService, final UserService userService,
                               final SandboxService sandboxService, final AppService appService,
                               final RuleService ruleService,
                               final UserPersonaService userPersonaService, final UserAccessHistoryService userAccessHistoryService,
                               final SandboxActivityLogService sandboxActivityLogService, final AuthorizationService authorizationService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
        this.sandboxService = sandboxService;
        this.appService = appService;
        this.ruleService = ruleService;
        this.userPersonaService = userPersonaService;
        this.userAccessHistoryService = userAccessHistoryService;
        this.sandboxActivityLogService = sandboxActivityLogService;
        this.authorizationService = authorizationService;
    }

    @GetMapping(value = "/sandboxes", params = {"userId"})
    public @ResponseBody
    Integer countSandboxesByUser(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);

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
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        return analyticsService.countUsersPerSandboxByUser(user);
    }

    @GetMapping(value = "/apps", params = {"userId"})
    public @ResponseBody HashMap<String, Integer> countAppsBySandbox(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        return analyticsService.countAppsPerSandboxByUser(user);
    }

    @GetMapping(value = "/memory", params = {"userId"})
    public @ResponseBody Double memoryUsedByUser(HttpServletRequest request, @RequestParam(value = "userId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        authorizationService.checkUserAuthorization(request, userId);
        Double memoryUseInMB = 0.0;
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
       return analyticsService.retrieveTotalMemoryByUser(user, authorizationService.getBearerToken(request));
    }

    @PostMapping(value = "/transaction")
    @Transactional
    public @ResponseBody
    FhirTransaction handleFhirTransaction(final HttpServletRequest request, @RequestBody final HashMap transactionInfo) {
        Sandbox sandbox = sandboxService.findBySandboxId(transactionInfo.get("tenant").toString());
        String userId = transactionInfo.get("userId").toString();
        User user;
        if(transactionInfo.get("secured").toString().equals("true") && !Arrays.asList(sandboxesAllUsersCanAccess).contains(transactionInfo.get("tenant").toString())) {
            user = userService.findBySbmUserId(userId);
            if (user != null) {
                try {
                    authorizationService.checkSystemUserCanMakeTransaction(sandbox, user);
                } catch (UnauthorizedException e) {
                    throw new UnauthorizedException("User does not have access to this sandbox");
                }
            } else if (transactionInfo.get("secured").equals("false")) {
                // do nothing
            } else {
                try {
                    UserPersona userPersona = userPersonaService.findByPersonaUserId(userId);
                    authorizationService.checkIfPersonaAndHasAuthority(sandbox, userPersona);
                } catch (UnauthorizedException e2) {
                    throw new UnauthorizedException("Persona does not have access to this sandbox");
                }
            }

        } else {
            user = null;
        }
        return analyticsService.handleFhirTransaction(user, transactionInfo, authorizationService.getBearerToken(request));
    }

    @GetMapping(value="/getStats", produces = APPLICATION_JSON_VALUE)
    public @ResponseBody void getStats(HttpServletRequest request) throws UnsupportedEncodingException {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        // TODO: Delete this method
        analyticsService.getSandboxAndUserStatsForLastTwoYears();
    }

    @GetMapping(value="/overallStats", produces = APPLICATION_JSON_VALUE, params = {"numberOfMonths"})
    public @ResponseBody List<Statistics> displayStats(HttpServletRequest request, @RequestParam(value = "numberOfMonths") String numberOfMonths) throws UnsupportedEncodingException {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return analyticsService.displayStatsForGivenNumberOfMonths(numberOfMonths);
    }

    @GetMapping(value="/overallStats", produces = APPLICATION_JSON_VALUE, params = {"numberOfDays"})
    public @ResponseBody Statistics getStatsOverNumberOfDays(HttpServletRequest request, @RequestParam(value = "numberOfDays") String numberOfDays) throws UnsupportedEncodingException {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return analyticsService.getSandboxStatisticsOverNumberOfDays(numberOfDays);
    }

    @GetMapping(value="/overallStats/transactions", params = {"interval"})
    public HashMap<String, Object> transactionStats(HttpServletRequest request, @RequestParam(value = "interval") Integer intervalDays, @RequestParam(value = "n", required = false) Integer n) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return analyticsService.transactionStats(intervalDays, n);
    }

    @GetMapping(value="/overallStats/sandboxMemory", params = {"interval"})
    public HashMap<String, Object> sandboxMemoryStats(HttpServletRequest request, @RequestParam(value = "interval") Integer intervalDays, @RequestParam(value = "n", required = false) Integer n) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return analyticsService.sandboxMemoryStats(intervalDays, n, authorizationService.getBearerToken(request));
    }

    @GetMapping(value="/overallStats/usersPerSandbox", params = {"interval"})
    public HashMap<String, Object> usersPerSandboxStats(HttpServletRequest request, @RequestParam(value = "interval") Integer intervalDays, @RequestParam(value = "n", required = false) Integer n) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return analyticsService.usersPerSandboxStats(intervalDays, n);
    }

    @GetMapping(value="/overallStats/sandboxesPerUser", params = {"interval"})
    public HashMap<String, Object> sandboxesPerUserStats(HttpServletRequest request, @RequestParam(value = "interval") Integer intervalDays, @RequestParam(value = "n", required = false) Integer n) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return analyticsService.sandboxesPerUserStats(intervalDays, n);
    }

    @GetMapping(value = "/userStatistics")
    public UserStatistics currentStatisticsByUser(HttpServletRequest request) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);
        return analyticsService.getUserStats(user, authorizationService.getBearerToken(request));
    }

    @GetMapping(value="/overallStatsForSpecificTimePeriod", params = {"begin", "end"},  produces = APPLICATION_JSON_VALUE)
    public Statistics getStatsForSpecificTimePeriod(HttpServletRequest request, @RequestParam(value = "begin") String begin, @RequestParam(value = "end") String end) {
        User user = userService.findBySbmUserId(authorizationService.getSystemUserId(request));
        if (user == null) {
            throw new ResourceNotFoundException("User not found in authorization header.");
        }
        authorizationService.checkUserSystemRole(user, SystemRole.ADMIN);

        try {
            Date beginDate = new SimpleDateFormat("MM-dd-yyyy").parse(begin);
            Date endDate = new SimpleDateFormat("MM-dd-yyyy").parse(end);
            Calendar c = Calendar.getInstance();
            c.setTime(endDate);
            c.add(Calendar.DATE, 1);
            endDate = c.getTime();
            return analyticsService.getSandboxStatisticsForSpecificTimePeriod(beginDate, endDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage() + ". Please enter date in MM-DD-YYYY format");
        }
    }
}
