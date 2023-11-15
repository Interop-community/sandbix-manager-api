package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RuleServiceImpl implements RuleService {
    private static Logger LOGGER = LoggerFactory.getLogger(RuleServiceImpl.class.getName());

    private RulesList rulesList;

    private SandboxService sandboxService;
    private UserService userService;
    private AnalyticsService analyticsService;
    private AppService appService;
    private NotificationService notificationService;

    public RuleServiceImpl() { }

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setAnalyticsService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Inject
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Inject
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Inject
    public void setRulesList(RulesList rulesList) {
        this.rulesList = rulesList;
    }

    public Boolean checkIfUserCanCreateSandbox(User user, String bearerToken) {
        
        LOGGER.info("checkIfUserCanCreateSandbox");

        if (rulesList.getTierRuleList() == null) {
            
            LOGGER.debug("checkIfUserCanCreateSandbox: "
            +"Parameters: user = "+user+", bearerToken = "+bearerToken
            +"Return value = true");

            return true;
        }
        Rule rules = findRulesByUser(user);
        if (rules == null) {

            LOGGER.debug("checkIfUserCanCreateSandbox: "
            +"Parameters: user = "+user+", bearerToken = "+bearerToken
            +"Return value = true");

            return true;
        }
        List<Sandbox> sandboxes = sandboxService.findByPayerId(user.getId());
        if (rules.getSandboxes() > sandboxes.size()) {
            if (rules.getStorage() > analyticsService.retrieveTotalMemoryByUser(user, bearerToken)) {
                
                LOGGER.debug("checkIfUserCanCreateSandbox: "
                +"Parameters: user = "+user+", bearerToken = "+bearerToken
                +"Return value = true");

                return true;
            }
        }

        LOGGER.debug("checkIfUserCanCreateSandbox: "
        +"Parameters: user = "+user+", bearerToken = "+bearerToken
        +"Return value = false");

        return false;
    }

    public Boolean checkIfUserCanCreateApp(Sandbox sandbox) {
        
        LOGGER.info("checkIfUserCanCreateApp");

        Integer payerId = sandbox.getPayerUserId();
        if (rulesList.getTierRuleList() == null) {

            LOGGER.debug("checkIfUserCanCreateApp: "
            +"Parameters: sandbox = "+sandbox+"; Return value = true");

            return true;
        }
        if (payerId == null) {

            LOGGER.debug("checkIfUserCanCreateApp: "
            +"Parameters: sandbox = "+sandbox+"; Return value = true");

            return true;
        }
        List<App> apps = appService.findBySandboxId(sandbox.getSandboxId());
        List<App> masterApps = apps.stream()
                                   .filter(p -> p.getCopyType() == CopyType.MASTER).collect(Collectors.toList());
        User user = userService.findById(payerId);
        Rule rules = findRulesByUser(user);
        if (rules == null) {
            
            LOGGER.debug("checkIfUserCanCreateApp: "
            +"Parameters: sandbox = "+sandbox+"; Return value = true");

            return true;
        }

        LOGGER.debug("checkIfUserCanCreateApp: "
        +"Parameters: sandbox = "+sandbox+"; Return value = "+(rules.getApps() > masterApps.size()));

        return rules.getApps() > masterApps.size();
    }

    public Boolean checkIfUserCanBeAdded(String sandBoxId) {
        
        LOGGER.info("checkIfUserCanBeAdded");

        if (rulesList.getTierRuleList() == null) {
            
            LOGGER.debug("checkIfUserCanBeAdded: "
            +"Parameters: sandboxId = "+sandBoxId+"; Return value = true");

            return true;
        }
        Integer payerId = sandboxService.findBySandboxId(sandBoxId).getPayerUserId();
        if (payerId == null) {
            
            LOGGER.debug("checkIfUserCanBeAdded: "
            +"Parameters: sandboxId = "+sandBoxId+"; Return value = true");

            return true;
        }
        User user = userService.findById(payerId);
        Rule rules = findRulesByUser(user);
        if (rules == null) {
            
            LOGGER.debug("checkIfUserCanBeAdded: "
            +"Parameters: sandboxId = "+sandBoxId+"; Return value = true");

            return true;
        }
        List<UserRole> usersRoles = sandboxService.findBySandboxId(sandBoxId).getUserRoles();
        Set<String> uniqueUsers = new HashSet<>();
        for (UserRole userRole : usersRoles) {
            // Creates unique list
            uniqueUsers.add(userRole.getUser().getEmail());
        }

        LOGGER.debug("checkIfUserCanBeAdded: "
            +"Parameters: sandboxId = "+sandBoxId+"; Return value = "+(rules.getUsers() > uniqueUsers.size()));

        return rules.getUsers() > uniqueUsers.size();
    }

    public Boolean checkIfUserCanPerformTransaction(Sandbox sandbox, String operation, String bearerToken) {
        
        LOGGER.info("checkIfUserCanPerformTransaction");

        if (rulesList.getTierRuleList() == null) {

            LOGGER.debug("checkIfUserCanPerformTransaction: "
            +"Parameters: sandbox = "+sandbox+", operation = "+operation+", bearerToken = "+bearerToken
            +"; Return value = true");

            return true;
        }
        Integer payerId = sandbox.getPayerUserId();
        if (payerId == null) {
            
            LOGGER.debug("checkIfUserCanPerformTransaction: "
            +"Parameters: sandbox = "+sandbox+", operation = "+operation+", bearerToken = "+bearerToken
            +"; Return value = true");

            return true;
        }
        User user = userService.findById(payerId);
        Rule rules = findRulesByUser(user);
        if (rules == null) {

            LOGGER.debug("checkIfUserCanPerformTransaction: "
            +"Parameters: sandbox = "+sandbox+", operation = "+operation+", bearerToken = "+bearerToken
            +"; Return value = true");

            return true;
        }
        Boolean hasNotifForTransaction = false;
        Boolean hasNotifForMemory = false;
        List<Notification> notifications = notificationService.getAllNotificationsByUser(user);
        for (Notification notification: notifications) {
            String title = notification.getNewsItem().getTitle();
            if(title.equalsIgnoreCase("Transactions more than threshold")){
                hasNotifForTransaction = true;
            }else if(title.equalsIgnoreCase("Used storage more than threshold")){
                hasNotifForMemory = true;
            }
        }
        Integer countedTransactionsByPayer = analyticsService.countTransactionsByPayer(user);
        if (rules.getTransactions() > countedTransactionsByPayer) {
            if(rules.getTransactions() * rulesList.getThreshold() <= countedTransactionsByPayer && !hasNotifForTransaction){
                notificationService.createNotificationForMoreThanThresholdTransaction(user);
            }
            if (!operation.equals("GET") && !operation.equals("DELETE")) {
                // Don't need to check memory for these operations
                Double countedTotalMemoryByUser = analyticsService.retrieveTotalMemoryByUser(user, bearerToken);
                if (rules.getStorage() > countedTotalMemoryByUser) {
                    if (rules.getStorage() * rulesList.getThreshold() <= countedTotalMemoryByUser && !hasNotifForMemory) {
                        notificationService.createNotificationForMoreThanThresholdMemory(user);
                    }
                    
                    LOGGER.debug("checkIfUserCanPerformTransaction: "
                    +"Parameters: sandbox = "+sandbox+", operation = "+operation+", bearerToken = "+bearerToken
                    +"; Return value = true");
                    
                    return true;
                }
            } else {
                
                LOGGER.debug("checkIfUserCanPerformTransaction: "
                +"Parameters: sandbox = "+sandbox+", operation = "+operation+", bearerToken = "+bearerToken
                +"; Return value = true");
                
                return true;
            }
        }
        
        LOGGER.debug("checkIfUserCanPerformTransaction: "
        +"Parameters: sandbox = "+sandbox+", operation = "+operation+", bearerToken = "+bearerToken
        +"; Return value = false");
        
        return false;
    }

    public Rule findRulesByUser(User user) {
        
        LOGGER.info("findRulesByUser");

        if (user.getTierLevel() == null) {

            LOGGER.debug("findRulesByUser: "
            +"Parameters: user = "+user+"; Return value = null");

            return null;
        }
        
        LOGGER.debug("findRulesByUser: "
        +"Parameters: user = "+user
        +"; Return value = "+rulesList.getTierRuleList().get(user.getTierLevel().name()));

        return rulesList.getTierRuleList().get(user.getTierLevel().name());
    }

}
