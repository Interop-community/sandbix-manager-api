package org.hspconsorotium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.hspconsortium.sandboxmanagerapi.services.impl.SandboxDeleteFailedException;
import org.hspconsortium.sandboxmanagerapi.services.impl.SandboxServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class SandboxServiceTest {

    private SandboxRepository repository = mock(SandboxRepository.class);
    private UserService userService = mock(UserService.class);
    private UserRoleService userRoleService = mock(UserRoleService.class);
    private UserPersonaService userPersonaService = mock(UserPersonaService.class);
    private UserLaunchService userLaunchService = mock(UserLaunchService.class);
    private AppService appService = mock(AppService.class);
    private LaunchScenarioService launchScenarioService = mock(LaunchScenarioService.class);
    private SandboxImportService sandboxImportService = mock(SandboxImportService.class);
    private SandboxActivityLogService sandboxActivityLogService = mock(SandboxActivityLogService.class);
    private RuleService ruleService = mock(RuleService.class);
    private UserAccessHistoryService userAccessHistoryService = mock(UserAccessHistoryService.class);

    private SandboxServiceImpl sandboxService = new SandboxServiceImpl(repository);

    private Sandbox sandbox;
    private User user;
    private String bearerToken = "token";
    private SandboxImport sandboxImport;

    @Before
    public void setup() {
        sandboxService.setUserAccessHistoryService(userAccessHistoryService);
        sandboxService.setUserLaunchService(userLaunchService);
        sandboxService.setUserPersonaService(userPersonaService);
        sandboxService.setUserRoleService(userRoleService);
        sandboxService.setUserService(userService);
        sandboxService.setAppService(appService);
        sandboxService.setLaunchScenarioService(launchScenarioService);
        sandboxService.setSandboxImportService(sandboxImportService);
        sandboxService.setSandboxActivityLogService(sandboxActivityLogService);
        sandboxService.setRuleService(ruleService);

        sandbox = new Sandbox();
        sandbox.setId(1);
        sandbox.setSandboxId("sandboxId");
        user = new User();
        user.setSbmUserId("userId");
        sandbox.setCreatedBy(user);
        List<SandboxImport> sandboxImportList = new ArrayList<>();
        sandboxImport = new SandboxImport();
        sandboxImportList.add(sandboxImport);
        sandbox.setImports(sandboxImportList);
    }

    @Test
    public void deleteTest() {
        sandboxService.delete(sandbox.getId());
        verify(repository).delete(sandbox.getId());
    }

    @Test(expected = SandboxDeleteFailedException.class)
    public void deleteTestAll() {
        sandboxService.delete(sandbox, bearerToken, user);
        verify(sandboxImportService).delete(sandboxImport);
        verify(sandboxActivityLogService).sandboxDelete(sandbox, user);
    }

    @Test(expected = SandboxDeleteFailedException.class)
    public void deleteTestAllAdminIsNull() {
        sandboxService.delete(sandbox, bearerToken, null);
        verify(sandboxActivityLogService).sandboxDelete(sandbox, sandbox.getCreatedBy());
    }
}
