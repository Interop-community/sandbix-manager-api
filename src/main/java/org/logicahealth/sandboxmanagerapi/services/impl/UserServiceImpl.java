package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.TermsOfUse;
import org.logicahealth.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.repositories.UserRepository;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private TermsOfUseService termsOfUseService;
    private TermsOfUseAcceptanceService termsOfUseAcceptanceService;
    private UserAccessHistoryService userAccessHistoryService;
    private SandboxInviteService sandboxInviteService;
    private NotificationService notificationService;

    private static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class.getName());

    @Inject
    public UserServiceImpl(final UserRepository repository) {
        this.repository = repository;
    }

    @Inject
    public void setTermsOfUseService(TermsOfUseService termsOfUseService) {
        this.termsOfUseService = termsOfUseService;
    }

    @Inject
    public void setTermsOfUseAcceptanceService(TermsOfUseAcceptanceService termsOfUseAcceptanceService) {
        this.termsOfUseAcceptanceService = termsOfUseAcceptanceService;
    }

    @Inject
    public void setUserAccessHistoryService(UserAccessHistoryService userAccessHistoryService) {
        this.userAccessHistoryService = userAccessHistoryService;
    }

    @Inject
    public void setSandboxInviteService(SandboxInviteService sandboxInviteService) {
        this.sandboxInviteService = sandboxInviteService;
    }

    @Inject
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public User save(final User user) {
        
        LOGGER.info("save");

        User retVal = repository.save(user);

        LOGGER.debug("save: "
        +"Parameters: user = "+user+"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public void delete(final User user) {
        
        LOGGER.info("delete");

        userAccessHistoryService.deleteUserAccessInstancesForUser(user);
        repository.delete(user);

        LOGGER.debug("delete: "
        +"Parameters: user = "+user+"; No return value");

    }

    public Iterable<User> findAll() {
        
        LOGGER.info("findAll");

        LOGGER.debug("findAll: "
        +"No input parameters; Return value = "+repository.findAll());

        return repository.findAll();
    }

    @org.springframework.transaction.annotation.Transactional
    public User findBySbmUserId(final String sbmUserId) {
        
        LOGGER.info("findBySbmUserId");

        User user = repository.findBySbmUserId(sbmUserId);

        if(user == null){

            LOGGER.debug("findBySbmUserId: "
            +"Parameters: sbmUserId = "+sbmUserId+"; Return value = null");

            return null;
        }

        userHasAcceptedTermsOfUse(user);

        LOGGER.debug("findBySbmUserId: "
        +"Parameters: sbmUserId = "+sbmUserId+"; Return value = "+user);

        return user;
    }

    public User findByUserEmail(final String email) {
        
        LOGGER.info("findByUserEmail");

        User user = repository.findByUserEmail(email);

        if(user == null){
            
            LOGGER.debug("findByUserEmail: "
            +"Parameters: email = "+email+"; Return value = null");

            return null;
        }
        userHasAcceptedTermsOfUse(user);

        LOGGER.debug("findByUserEmail: "
        +"Parameters: email = "+email+"; Return value = "+user);

        return user;
    }

    public User findById(final Integer id) {
        
        LOGGER.info("findById");

        User user = repository.findById(id).orElse(null);

        if(user == null){

            LOGGER.debug("findById: "
            +"Parameters: id = "+id+"; Return value = null");

            return null;
        }

        userHasAcceptedTermsOfUse(user);
        
        LOGGER.debug("findById: "
        +"Parameters: id = "+id+"; Return value = "+user);

        return user;
    }

    @Override
    public String fullCount() {

        LOGGER.info("fullCount");

        LOGGER.debug("fullCount: "
        +"No input parameters; Return value = "+repository.fullCount());

        return repository.fullCount();
    }

    @Override
    public String fullCountForSpecificPeriod(Timestamp endDate) {
        
        LOGGER.info("fullCountForSpecificPeriod");

        LOGGER.debug("fullCountForSpecificPeriod: "
        +"Parameters: endDate = "+endDate+
        "; Return value = "+repository.fullCountForSpecificTimePeriod(endDate));

        return repository.fullCountForSpecificTimePeriod(endDate);
    }

    @Override
    public String intervalCount(final Timestamp intervalTime) {

        LOGGER.info("intervalCount");

        LOGGER.debug("intervalCount: "
        +"Parameters: intervalTime = "+intervalTime
        +"; Return value = "+repository.intervalCount(intervalTime));

        return repository.intervalCount(intervalTime);
    }

    @Override
    public String intervalCountForSpecificTimePeriod(Timestamp beginDate, Timestamp endDate) {
        
        LOGGER.info("intervalCountForSpecificTimePeriod");

        LOGGER.debug("intervalCountForSpecificTimePeriod: "
        +"Parameters: beginDate = "+beginDate+", endDate = "+endDate
        +"; Return value = "+repository.intervalCountForSpecificTimePeriod(beginDate, endDate));

        return repository.intervalCountForSpecificTimePeriod(beginDate, endDate);
    }

    @Override
    @Transactional
    public void removeSandbox(Sandbox sandbox, User user) {
        
        LOGGER.info("removeSandbox");

        LOGGER.debug("removeSandbox: "
        +"(BEFORE) Parameters: sandbox = "+sandbox+", user = "+user);

        List<Sandbox> sandboxes = user.getSandboxes();
        sandboxes.remove(sandbox);
        user.setSandboxes(sandboxes);
        save(user);

        LOGGER.debug("removeSandbox: "
        +"(AFTER) Parameters: sandbox = "+sandbox+", user = "+user
        +"; No return value");
    }

    @Override
    @Transactional
    public void addSandbox(Sandbox sandbox, User user) {
        
        LOGGER.info("addSandbox");

        LOGGER.debug("addSandbox: "
        +"(BEFORE) Parameters: sandbox = "+sandbox+", user = "+user);

        List<Sandbox> sandboxes = user.getSandboxes();
        if (!sandboxes.contains(sandbox)) {
            sandboxes.add(sandbox);
            user.setSandboxes(sandboxes);
            save(user);
        }
        
        LOGGER.debug("addSandbox: "
        +"(AFTER) Parameters: sandbox = "+sandbox+", user = "+user
        +"; No return value");
    }

    @Override
    public boolean hasSandbox(Sandbox sandbox, User user) {
        
        LOGGER.info("hasSandbox");

        LOGGER.debug("hasSandbox: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+user.getSandboxes().contains(sandbox));

        return user.getSandboxes().contains(sandbox);
    }

    @Override
    public void acceptTermsOfUse(final User user, final String termsOfUseId){
        
        LOGGER.info("acceptTermsOfUse");

        LOGGER.debug("acceptTermsOfUse: "
        +"(BEFORE) Parameters: user = "+user+", termsOfUseId = "+termsOfUseId);

        TermsOfUse termsOfUse = termsOfUseService.getById(Integer.parseInt(termsOfUseId));
        TermsOfUseAcceptance termsOfUseAcceptance = new TermsOfUseAcceptance();
        termsOfUseAcceptance.setTermsOfUse(termsOfUse);
        termsOfUseAcceptance.setAcceptedTimestamp(new Timestamp(new Date().getTime()));
        termsOfUseAcceptance = termsOfUseAcceptanceService.save(termsOfUseAcceptance);
        List<TermsOfUseAcceptance> acceptances = user.getTermsOfUseAcceptances();
        acceptances.add(termsOfUseAcceptance);
        user.setTermsOfUseAcceptances(acceptances);
        save(user);

        LOGGER.debug("acceptTermsOfUse: "
        +"(AFTER) Parameters: user = "+user+", termsOfUseId = "+termsOfUseId
        +"; No return value");

    }

    private void userHasAcceptedTermsOfUse(User user) {
        
        LOGGER.info("userHasAcceptedTermsOfUse");

        LOGGER.debug("userHasAcceptedTermsOfUse: "
        +"(BEFORE) Parameters: user = "+user);

        TermsOfUse latestTermsOfUse = termsOfUseService.mostRecent();
        if (latestTermsOfUse != null) {
            user.setHasAcceptedLatestTermsOfUse(false);
            for (TermsOfUseAcceptance termsOfUseAcceptance : user.getTermsOfUseAcceptances()) {
                if (termsOfUseAcceptance.getTermsOfUse().getId().equals(latestTermsOfUse.getId())) {
                    user.setHasAcceptedLatestTermsOfUse(true);
                    
                    LOGGER.debug("userHasAcceptedTermsOfUse: "
                    +"(AFTER) Parameters: user = "+user+"; No return value");

                    return;
                }
            }
        } else {
            // there are no terms so by default the user has accepted the latest
            user.setHasAcceptedLatestTermsOfUse(true);
        }
        
        LOGGER.debug("userHasAcceptedTermsOfUse: "
        +"(AFTER) Parameters: user = "+user+"; No return value");
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void deleteSandboxUsersWhoDidNotAcceptInvitationWithinOneMonth() {
        LOGGER.info("Deleting rows from  user table and corresponding sandbox invites where sandbox invitation was not accepted within a month.");
        var staleUsers = repository.findAllBySbmUserIdIsNullAndCreatedTimestampLessThan(oneMonthAgo());
        sandboxInviteService.delete(staleUsers);
        notificationService.delete(staleUsers);
        repository.deleteAll(staleUsers);
    }

    private Timestamp oneMonthAgo() {
        
        LOGGER.info("oneMonthAgo");

        var timestamp = new Timestamp(System.currentTimeMillis());
        var calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        calendar.add(Calendar.MONTH, -1);

        Timestamp retVal = new Timestamp(calendar.getTime().getTime());
        
        LOGGER.debug("oneMonthAgo: "
        +"No input parameters; Return value = "+retVal);

        return retVal;
    }

}
