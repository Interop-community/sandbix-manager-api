package org.hspconsortium.sandboxmanagerapi.services.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hspconsortium.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxActivityLogRepository;
import org.hspconsortium.sandboxmanagerapi.services.SandboxActivityLogService;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class SandboxActivityLogServiceImpl implements SandboxActivityLogService {

    private final SandboxActivityLogRepository repository;

    @Inject
    public SandboxActivityLogServiceImpl(final SandboxActivityLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SandboxActivityLog save(SandboxActivityLog sandboxActivityLog) {
        return repository.save(sandboxActivityLog);
    }

    @Override
    @Transactional
    public void delete(SandboxActivityLog sandboxActivityLog) {
        repository.delete(sandboxActivityLog.getId());
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxCreate(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.CREATED);
        return this.save(sandboxActivityLog);
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxLogin(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.LOGGED_IN);
        return this.save(sandboxActivityLog);
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxDelete(Sandbox sandbox, User user) {
        List<SandboxActivityLog> sandboxActivityLogList = findBySandboxId(sandbox.getSandboxId());
        for (SandboxActivityLog sandboxActivityLog : sandboxActivityLogList) {
            delete(sandboxActivityLog);
        }

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(null, user);
        sandboxActivityLog.setActivity(SandboxActivity.DELETED);
        sandbox.setCreatedBy(null);
        sandboxActivityLog.setAdditionalInfo(toJson(sandbox));
        return this.save(sandboxActivityLog);
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserInviteAccepted(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_ACCEPTED_INVITE);
        return this.save(sandboxActivityLog);
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserInviteRevoked(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_INVITATION_REVOKED);
        return this.save(sandboxActivityLog);
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserInviteRejected(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_INVITATION_REJECTED);
        return this.save(sandboxActivityLog);
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserRemoved(Sandbox sandbox, User user, User removedUser) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_REMOVED);
        sandboxActivityLog.setAdditionalInfo(removedUser.getSbmUserId());
        return this.save(sandboxActivityLog);
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserInvited(Sandbox sandbox, User user, User invitedUser) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_INVITED);
        sandboxActivityLog.setAdditionalInfo("User Email: " + invitedUser.getEmail());
        return this.save(sandboxActivityLog);
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxOpenEndpoint(Sandbox sandbox, User user, Boolean openEndpoint) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.OPEN_ENDPOINT);
        sandboxActivityLog.setAdditionalInfo(openEndpoint == Boolean.TRUE ? "Open Endpoint Enabled" : "Open Endpoint Disabled");
        return this.save(sandboxActivityLog);
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserAdded(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_ADDED);
        return this.save(sandboxActivityLog);
    }

    @Override
    public SandboxActivityLog sandboxUserRoleChange(Sandbox sandbox, User user, Role role, boolean roleAdded) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_SANDBOX_ROLE_CHANGE);
        sandboxActivityLog.setAdditionalInfo("Role " + role.toString() + (roleAdded ? " added" : " removed"));
        return this.save(sandboxActivityLog);
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxImport(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.SANDBOX_DATA_IMPORT);
        return this.save(sandboxActivityLog);
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxReset(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.SANDBOX_RESET);
        return this.save(sandboxActivityLog);
    }

    @Override
    public SandboxActivityLog systemUserCreated(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_CREATED);
        sandboxActivityLog.setAdditionalInfo("SBM User Id " + user.getSbmUserId());
        return this.save(sandboxActivityLog);
    }

    @Override
    public SandboxActivityLog systemUserRoleChange(User user, SystemRole systemRole, boolean roleAdded) {
        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(null, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_SYSTEM_ROLE_CHANGE);
        sandboxActivityLog.setAdditionalInfo("Role " + systemRole.toString() + (roleAdded ? " added" : " removed"));
        return this.save(sandboxActivityLog);
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog userDelete(final User user) {
        List<SandboxActivityLog> sandboxActivityLogList = findByUserId(user.getId());
        for (SandboxActivityLog sandboxActivityLog : sandboxActivityLogList) {
            delete(sandboxActivityLog);
        }

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(null, null);
        sandboxActivityLog.setAdditionalInfo(userToJson(user));
        sandboxActivityLog.setActivity(SandboxActivity.USER_DELETED);
        return this.save(sandboxActivityLog);
    }



    @Override
    public List<SandboxActivityLog> findBySandboxId(String sandboxId) {
        return repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<SandboxActivityLog> findByUserSbmUserId(String sbmUserId) {
        return repository.findByUserSbmUserId(sbmUserId);
    }

    @Override
    public List<SandboxActivityLog> findByUserId(int userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<SandboxActivityLog> findBySandboxActivity(SandboxActivity sandboxActivity) {
        return repository.findBySandboxActivity(sandboxActivity);
    }

    @Override
    public Iterable<SandboxActivityLog> findAll() {
        return repository.findAll();
    }

    @Override
    public List<SandboxActivityLog> findAllForSpecificTimePeriod(Timestamp beginDate, Timestamp endDate) {
        return repository.findAllForSpecificTimePeriod(beginDate, endDate);
    }

    @Override
    public String intervalActive(final Timestamp intervalTime) {
        return repository.intervalActive(intervalTime);
    }

    private SandboxActivityLog createSandboxActivityLog(Sandbox sandbox, User user) {
        SandboxActivityLog sandboxActivityLog = new SandboxActivityLog();
        sandboxActivityLog.setSandbox(sandbox);
        sandboxActivityLog.setUser(user);
        sandboxActivityLog.setTimestamp(new Timestamp(new Date().getTime()));
        return sandboxActivityLog;
    }

    private static String toJson(Sandbox sandbox) {
        Gson gson = new Gson();
        Type type = new TypeToken<Sandbox>() {
        }.getType();
        return gson.toJson(sandbox, type);
    }

    private static String userToJson(User user) {
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        return gson.toJson(user, type);
    }

}
