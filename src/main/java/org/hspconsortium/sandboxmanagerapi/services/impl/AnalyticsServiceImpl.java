package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.AnalyticsService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserService userService;

    @Inject
    AnalyticsServiceImpl(final UserService userService) {
        this.userService = userService;
    }

    public Integer countSandboxesByUser(String userId) {
        User user = userService.findBySbmUserId(userId);
        return 1;
    }
}
