package org.logicahealth.sandboxmanagerapi.services;

import org.junit.Before;
import org.junit.Test;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.services.impl.RuleServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuleServiceTest {

    private SandboxService sandboxService = mock(SandboxService.class);
    private UserService userService = mock(UserService.class);
    private AnalyticsService analyticsService = mock(AnalyticsService.class);
    private AppService appService = mock(AppService.class);
    private RulesList rulesList = mock(RulesList.class);
    private NotificationService notificationService = mock(NotificationService.class);

    private RuleServiceImpl ruleService = new RuleServiceImpl();
    private Sandbox sandbox;
    private User user;
    private User userNoTier;
    private String token = "token";

    @Before
    public void setup() {
        ruleService.setAnalyticsService(analyticsService);
        ruleService.setAppService(appService);
        ruleService.setSandboxService(sandboxService);
        ruleService.setUserService(userService);
        ruleService.setRulesList(rulesList);
        ruleService.setNotificationService(notificationService);

        sandbox = new Sandbox();
        user = new User();
        user.setSbmUserId("me");
        user.setId(1);
        user.setTierLevel(Tier.FREE);
        sandbox.setPayerUserId(user.getId());
        userNoTier = new User();
        user.setSbmUserId("me2");
        user.setId(2);
        Rule rule = new Rule();
        rule.setApps(2);
        rule.setSandboxes(1);
        rule.setStorage(1000);
        rule.setTransactions(50000);
        rule.setUsers(2);
        HashMap<String, Rule> rules = new HashMap<>();
        rules.put("FREE", rule);

        when(rulesList.getTierRuleList()).thenReturn(rules);
        when(rulesList.getThreshold()).thenReturn(0.9);
    }

    @Test
    public void checkIfUserCanCreateSandboxTest() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Boolean bool = ruleService.checkIfUserCanCreateSandbox(user, token);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanCreateSandboxTestNoRules() {
        when(rulesList.getTierRuleList()).thenReturn(null);
        Boolean bool = ruleService.checkIfUserCanCreateSandbox(user, token);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanCreateSandboxTestNoTier() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(userNoTier);
        Boolean bool = ruleService.checkIfUserCanCreateSandbox(userNoTier, token);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanCreateSandboxTestNotEnoughMemory() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Double storage = new Double(rulesList.getTierRuleList().get("FREE").getStorage() + 1);
        when(analyticsService.retrieveTotalMemoryByUser(user, token)).thenReturn(storage);
        Boolean bool = ruleService.checkIfUserCanCreateSandbox(user, token);
        assertEquals(false, bool);
    }

    @Test
    public void checkIfUserCanCreateSandboxTestTooManySandboxes() {
        List<Sandbox> sandboxList = new ArrayList<>();
        for (int i = 0; i < rulesList.getTierRuleList().get("FREE").getSandboxes(); i++) {
            sandboxList.add(new Sandbox());
        }
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        when(sandboxService.findByPayerId(user.getId())).thenReturn(sandboxList);
        Boolean bool = ruleService.checkIfUserCanCreateSandbox(user, token);
        assertEquals(false, bool);
    }

    @Test
    public void checkIfUserCanCreateAppTest() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Boolean bool = ruleService.checkIfUserCanCreateApp(sandbox);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanCreateAppTestNoRules() {
        when(rulesList.getTierRuleList()).thenReturn(null);
        Boolean bool = ruleService.checkIfUserCanCreateApp(sandbox);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanCreateAppTestNoTier() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(userNoTier);
        Boolean bool = ruleService.checkIfUserCanCreateApp(sandbox);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanCreateAppTestNoPayer() {
        sandbox.setPayerUserId(null);
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Boolean bool = ruleService.checkIfUserCanCreateApp(sandbox);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanCreateAppTestTooManyApps() {
        List<App> appList = new ArrayList<>();
        for (int i = 0; i < rulesList.getTierRuleList().get("FREE").getApps() + 1; i++) {
            App newApp = new App();
            newApp.setCopyType(CopyType.MASTER);
            appList.add(newApp);
        }
        when(appService.findBySandboxId(sandbox.getSandboxId())).thenReturn(appList);
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Boolean bool = ruleService.checkIfUserCanCreateApp(sandbox);
        assertEquals(false, bool);
    }

    @Test
    public void checkIfUserCanBeAddedTest() {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Boolean bool = ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId());
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanBeAddedTestNoRules() {
        when(rulesList.getTierRuleList()).thenReturn(null);
        Boolean bool = ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId());
        assertEquals(true, bool);
    }
    @Test
    public void checkIfUserCanBeAddedTestNoTier() {
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(userNoTier);
        Boolean bool = ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId());
        assertEquals(true, bool);
    }
    @Test
    public void checkIfUserCanBeAddedTestNoPayer() {
        sandbox.setPayerUserId(null);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        Boolean bool = ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId());
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanBeAddedTestTooManyUsers() {
        List<UserRole> userRoleList = new ArrayList<>();
        for (int i = 0; i < rulesList.getTierRuleList().get("FREE").getUsers() + 1; i++) {
            UserRole userRole = new UserRole();
            User user = new User();
            user.setEmail("email" + i);
            userRole.setUser(user);
            userRoleList.add(userRole);
        }
        sandbox.setUserRoles(userRoleList);
        when(sandboxService.findBySandboxId(sandbox.getSandboxId())).thenReturn(sandbox);
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Boolean bool = ruleService.checkIfUserCanBeAdded(sandbox.getSandboxId());
        assertEquals(false, bool);
    }

    @Test
    public void checkIfUserCanPerformTransactionTest() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Boolean bool = ruleService.checkIfUserCanPerformTransaction(sandbox, "POST", token);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanPerformTransactionTestNoRules() {
        when(rulesList.getTierRuleList()).thenReturn(null);
        Boolean bool = ruleService.checkIfUserCanPerformTransaction(sandbox, "POST", token);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanPerformTransactionTestNoTier() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(userNoTier);
        Boolean bool = ruleService.checkIfUserCanPerformTransaction(sandbox, "POST", token);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanPerformTransactionTestNoPayer() {
        sandbox.setPayerUserId(null);
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Boolean bool = ruleService.checkIfUserCanPerformTransaction(sandbox, "POST", token);
        assertEquals(true, bool);
    }

    @Test
    public void checkIfUserCanPerformTransactionTestNotInfoStorage() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Double storage = new Double(rulesList.getTierRuleList().get("FREE").getStorage() + 1);
        when(analyticsService.retrieveTotalMemoryByUser(user, token)).thenReturn(storage);
        Boolean bool = ruleService.checkIfUserCanPerformTransaction(sandbox, "POST", token);
        assertEquals(false, bool);
    }

    @Test
    public void checkIfUserCanPerformTransactionTestTooManyTransactions() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Integer transactions = rulesList.getTierRuleList().get("FREE").getTransactions() + 1;
        when(analyticsService.countTransactionsByPayer(user)).thenReturn(transactions);
        Boolean bool = ruleService.checkIfUserCanPerformTransaction(sandbox, "POST", token);
        assertEquals(false, bool);
    }

    @Test
    public void checkIfUserCanPerformTransactionTransactionOverThresholdLimit() {
        when(userService.findById(sandbox.getPayerUserId())).thenReturn(user);
        Integer transactions = (int)Math.round(rulesList.getTierRuleList().get("FREE").getTransactions() * rulesList.getThreshold()) + 20;
        when(analyticsService.countTransactionsByPayer(user)).thenReturn(transactions);
        Boolean bool = ruleService.checkIfUserCanPerformTransaction(sandbox, "POST", token);
        assertEquals(true, bool);
    }
}
