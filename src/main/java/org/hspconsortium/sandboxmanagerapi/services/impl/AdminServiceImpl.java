package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.services.AdminService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxActivityLogService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AdminServiceImpl implements AdminService {

    private UserService userService;
    private SandboxService sandboxService;
    private SandboxActivityLogService sandboxActivityLogService;

    @Inject
    public AdminServiceImpl() { }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Inject
    public void setSandboxActivityLogService(SandboxActivityLogService sandboxActivityLogService) {
        this.sandboxActivityLogService = sandboxActivityLogService;
    }
}
