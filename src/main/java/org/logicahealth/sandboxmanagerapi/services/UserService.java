package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;

import java.sql.Timestamp;

public interface UserService {

    User save(final User user);

    void delete(final User user);

    Iterable<User> findAll();

    User findBySbmUserId(final String sbmUserId);

    User findByUserEmail(final String email);

    User findById(final Integer id);

    String fullCount();

    String fullCountForSpecificPeriod(Timestamp endDate);

    String intervalCount(final Timestamp intervalTime);

    String intervalCountForSpecificTimePeriod(Timestamp beginDate, Timestamp endDate);

    void removeSandbox(final Sandbox sandbox, final User user);

    void addSandbox(final Sandbox sandbox, final User user);

    boolean hasSandbox(final Sandbox sandbox, final User user);

    void acceptTermsOfUse(final User user, final String termsOfUseId);

}
