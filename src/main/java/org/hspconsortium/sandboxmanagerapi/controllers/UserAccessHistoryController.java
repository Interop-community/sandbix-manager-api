package org.hspconsortium.sandboxmanagerapi.controllers;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.UserAccessHistory;
import org.hspconsortium.sandboxmanagerapi.services.OAuthService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.UserAccessHistoryService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping({"/sandbox-access"})
public class UserAccessHistoryController extends AbstractController {

    private SandboxService sandboxService;
    private UserAccessHistoryService userAccessHistoryService;
    private UserService userService;

    public UserAccessHistoryController(final SandboxService sandboxService, final UserAccessHistoryService userAccessHistoryService,
                                       final UserService userService, final OAuthService oAuthService) {
        super(oAuthService);
        this.sandboxService = sandboxService;
        this.userAccessHistoryService = userAccessHistoryService;
        this.userService = userService;
    }

    @GetMapping(params = {"sandboxId"})
    public @ResponseBody
    List<UserAccessHistory> getLastSandboxAccessWithSandboxId(final HttpServletRequest request, @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException {
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        checkSandboxUserReadAuthorization(request, sandbox);
        return userAccessHistoryService.getLatestUserAccessHistoryInsancesWithSandbox(sandbox);
    }

    @GetMapping(params = {"sbmUserId"})
    public @ResponseBody
    List<UserAccessHistory> getLastSandboxAccessWithSbmUserId(final HttpServletRequest request, @RequestParam(value = "sbmUserId") String userIdEncoded) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        return userAccessHistoryService.getLatestUserAccessHistoryInsancesWithSbmUser(user);
    }

    @GetMapping(params = {"sbmUserId", "sandboxId"})
    public @ResponseBody
    Timestamp getLastSandboxAccess(final HttpServletRequest request, @RequestParam(value = "sbmUserId") String userIdEncoded,
                                   @RequestParam(value = "sandboxId") String sandboxId) throws UnsupportedEncodingException {
        String userId = java.net.URLDecoder.decode(userIdEncoded, StandardCharsets.UTF_8.name());
        checkUserAuthorization(request, userId);
        User user = userService.findBySbmUserId(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        Sandbox sandbox = sandboxService.findBySandboxId(sandboxId);
        if (sandbox == null) {
            throw new ResourceNotFoundException("Sandbox not found.");
        }
        checkSandboxUserReadAuthorization(request, sandbox);
        return userAccessHistoryService.getLatestUserAccessHistoryInsance(sandbox, user);
    }
}
