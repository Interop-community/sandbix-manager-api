package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.TermsOfUse;
import org.hspconsortium.sandboxmanager.model.TermsOfUseAcceptance;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.repositories.UserRepository;
import org.hspconsortium.sandboxmanager.services.TermsOfUseAcceptanceService;
import org.hspconsortium.sandboxmanager.services.TermsOfUseService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final TermsOfUseService termsOfUseService;
    private final TermsOfUseAcceptanceService termsOfUseAcceptanceService;

    @Inject
    public UserServiceImpl(final UserRepository repository,
                           final TermsOfUseService termsOfUseService,
                           final TermsOfUseAcceptanceService termsOfUseAcceptanceService) {
        this.repository = repository;
        this.termsOfUseService = termsOfUseService;
        this.termsOfUseAcceptanceService = termsOfUseAcceptanceService;
    }

    @Override
    @Transactional
    public User save(final User user) {
        return repository.save(user);
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

    public String fullCount() {
        return repository.fullCount();
    }

    public String intervalCount(final Timestamp intervalTime) {
        return repository.intervalCount(intervalTime);
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
        if (termsOfUseService.orderByCreatedTimestamp().size() > 0) {
            TermsOfUse latestTermsOfUse = termsOfUseService.orderByCreatedTimestamp().get(0);
            user.setHasAcceptedLatestTermsOfUse(false);
            for (TermsOfUseAcceptance termsOfUseAcceptance : user.getTermsOfUseAcceptances()) {
                if (termsOfUseAcceptance.getTermsOfUse().getId().equals(latestTermsOfUse.getId())) {
                    user.setHasAcceptedLatestTermsOfUse(true);
                    return;
                }
            }
        }
    }
}


