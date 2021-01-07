package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.TermsOfUse;
import org.hspconsortium.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.repositories.UserRepository;
import org.hspconsortium.sandboxmanagerapi.services.TermsOfUseAcceptanceService;
import org.hspconsortium.sandboxmanagerapi.services.TermsOfUseService;
import org.hspconsortium.sandboxmanagerapi.services.UserAccessHistoryService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxInviteService;

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

    @Override
    @Transactional
    public User save(final User user) {
        return repository.save(user);
    }

    @Override
    @Transactional
    public void delete(final User user) {
        userAccessHistoryService.deleteUserAccessInstancesForUser(user);
        repository.delete(user);
    }

    public Iterable<User> findAll() {
        return repository.findAll();
    }

    public User findBySbmUserId(final String sbmUserId) {
        User user = repository.findBySbmUserId(sbmUserId);

        if(user == null)
            return null;

        userHasAcceptedTermsOfUse(user);
        return user;
    }

    public User findByUserEmail(final String email) {
        User user = repository.findByUserEmail(email);

        if(user == null)
            return null;

        userHasAcceptedTermsOfUse(user);
        return user;
    }

    public User findById(final Integer id) {
        User user = repository.findById(id).orElse(null);

        if(user == null)
            return null;

        userHasAcceptedTermsOfUse(user);
        return user;
    }

    @Override
    public String fullCount() {
        return repository.fullCount();
    }

    @Override
    public String fullCountForSpecificPeriod(Timestamp endDate) {
        return repository.fullCountForSpecificTimePeriod(endDate);
    }

    @Override
    public String intervalCount(final Timestamp intervalTime) {
        return repository.intervalCount(intervalTime);
    }

    @Override
    public String intervalCountForSpecificTimePeriod(Timestamp beginDate, Timestamp endDate) {
        return repository.intervalCountForSpecificTimePeriod(beginDate, endDate);
    }

    @Override
    @Transactional
    public void removeSandbox(Sandbox sandbox, User user) {
        List<Sandbox> sandboxes = user.getSandboxes();
        sandboxes.remove(sandbox);
        user.setSandboxes(sandboxes);
        save(user);
    }

    @Override
    @Transactional
    public void addSandbox(Sandbox sandbox, User user) {
        List<Sandbox> sandboxes = user.getSandboxes();
        if (!sandboxes.contains(sandbox)) {
            sandboxes.add(sandbox);
            user.setSandboxes(sandboxes);
            save(user);
        }
    }

    @Override
    public boolean hasSandbox(Sandbox sandbox, User user) {
        return user.getSandboxes().contains(sandbox);
    }

    @Override
    public void acceptTermsOfUse(final User user, final String termsOfUseId){
        TermsOfUse termsOfUse = termsOfUseService.getById(Integer.parseInt(termsOfUseId));
        TermsOfUseAcceptance termsOfUseAcceptance = new TermsOfUseAcceptance();
        termsOfUseAcceptance.setTermsOfUse(termsOfUse);
        termsOfUseAcceptance.setAcceptedTimestamp(new Timestamp(new Date().getTime()));
        termsOfUseAcceptance = termsOfUseAcceptanceService.save(termsOfUseAcceptance);
        List<TermsOfUseAcceptance> acceptances = user.getTermsOfUseAcceptances();
        acceptances.add(termsOfUseAcceptance);
        user.setTermsOfUseAcceptances(acceptances);
        save(user);
    }

    private void userHasAcceptedTermsOfUse(User user) {
        TermsOfUse latestTermsOfUse = termsOfUseService.mostRecent();
        if (latestTermsOfUse != null) {
            user.setHasAcceptedLatestTermsOfUse(false);
            for (TermsOfUseAcceptance termsOfUseAcceptance : user.getTermsOfUseAcceptances()) {
                if (termsOfUseAcceptance.getTermsOfUse().getId().equals(latestTermsOfUse.getId())) {
                    user.setHasAcceptedLatestTermsOfUse(true);
                    return;
                }
            }
        } else {
            // there are no terms so by default the user has accepted the latest
            user.setHasAcceptedLatestTermsOfUse(true);
        }
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void deleteSandboxUsersWhoDidNotAcceptInvitationWithinOneMonth() {
        LOGGER.info("Deleting rows from  user table and corresponding sandbox invites where sandbox invitation was not accepted within a month.");
        var staleUsers = repository.findAllBySbmUserIdIsNullAndCreatedTimestampLessThan(oneMonthAgo());
        sandboxInviteService.delete(staleUsers);
        repository.deleteAll(staleUsers);
    }

    private Timestamp oneMonthAgo() {
        var timestamp = new Timestamp(System.currentTimeMillis());
        var calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        calendar.add(Calendar.MONTH, -1);
        return new Timestamp(calendar.getTime().getTime());
    }

}
