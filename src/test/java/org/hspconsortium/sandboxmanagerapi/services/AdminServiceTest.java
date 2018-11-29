package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxActivityLog;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.SandboxActivityLogService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.hspconsortium.sandboxmanagerapi.services.impl.AdminServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class AdminServiceTest {

    private AdminServiceImpl adminService = new AdminServiceImpl();

    @MockBean
    private UserService userService;

    @MockBean
    private SandboxService sandboxService;

    @MockBean
    private SandboxActivityLogService sandboxActivityLogService;

    @MockBean
    private RestTemplate restTemplate;

    private Iterable<Sandbox> sandboxesIterable;
    private Iterable<SandboxActivityLog> sandboxAccessHistories;
    private Sandbox sandbox;
    private Sandbox unusedSandbox;
    private SandboxActivityLog sandboxActivityLog;
    private User user;

    @Before
    public void setup() {
        adminService.setUserService(userService);
        adminService.setSandboxService(sandboxService);
        adminService.setSandboxActivityLogService(sandboxActivityLogService);
        adminService.setRestTemplate(restTemplate);

        sandbox = new Sandbox();
        sandbox.setSandboxId("sndb-1");
        sandboxesIterable = new ArrayList<>();
        ((ArrayList<Sandbox>) sandboxesIterable).add(sandbox);

        Date d = new Date();
        Timestamp timestamp = new Timestamp(d.getTime());

        sandboxActivityLog = new SandboxActivityLog();
        sandboxActivityLog.setId(1);
        sandboxActivityLog.setTimestamp(timestamp);
        sandboxAccessHistories = new ArrayList<>();
        ((ArrayList<SandboxActivityLog>) sandboxAccessHistories).add(sandboxActivityLog);
    }

    @Test
    public void syncSandboxManagerandReferenceApiTest() {
        when(sandboxService.findAll()).thenReturn(sandboxesIterable);
        //TODO: RestTemplate issue, do it later
    }

    @Test
    public void deleteUnusedSandboxes() {

    }



}
