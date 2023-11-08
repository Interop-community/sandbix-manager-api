package org.logicahealth.sandboxmanagerapi.services.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.logicahealth.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.repositories.SandboxActivityLogRepository;
import org.logicahealth.sandboxmanagerapi.services.SandboxActivityLogService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SandboxActivityLogServiceImpl implements SandboxActivityLogService {
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxActivityLogServiceImpl.class.getName());

    private final SandboxActivityLogRepository repository;

    @Inject
    public SandboxActivityLogServiceImpl(final SandboxActivityLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SandboxActivityLog save(SandboxActivityLog sandboxActivityLog) {

        LOGGER.info("save");

        SandboxActivityLog retVal = repository.save(sandboxActivityLog);

        LOGGER.debug("save: "
        +"Parameters: sandboxActivityLog = "+sandboxActivityLog
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    public void delete(SandboxActivityLog sandboxActivityLog) {
        
        LOGGER.info("delete");

        repository.delete(sandboxActivityLog);

        LOGGER.debug("delete: "
        +"Parameters: sandboxActivityLog = "+sandboxActivityLog
        +"No return value");

    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxCreate(Sandbox sandbox, User user) {
        
        LOGGER.info("sandboxCreate");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.CREATED);

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxCreate: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxLogin(Sandbox sandbox, User user) {
        
        LOGGER.info("sandboxLogin");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.LOGGED_IN);

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxLogin: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxDelete(Sandbox sandbox, User user) {
        
        LOGGER.info("sandboxDelete");

        LOGGER.debug("sandboxDelete: "
        +"(BEFORE) Parameters: sandbox = "+sandbox+", user = "+user);

        List<SandboxActivityLog> sandboxActivityLogList = findBySandboxId(sandbox.getSandboxId());
        for (SandboxActivityLog sandboxActivityLog : sandboxActivityLogList) {
            delete(sandboxActivityLog);
        }

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(null, user);
        sandboxActivityLog.setActivity(SandboxActivity.DELETED);
        sandbox.setCreatedBy(null);
        sandboxActivityLog.setAdditionalInfo(toJson(sandbox));

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxDelete: "
        +"(AFTER) Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserInviteAccepted(Sandbox sandbox, User user) {
        
        LOGGER.info("sandboxUserInviteAccepted");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_ACCEPTED_INVITE);

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxUserInviteAccepted: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserInviteRevoked(Sandbox sandbox, User user) {
        
        LOGGER.info("sandboxUserInviteRevoked");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_INVITATION_REVOKED);

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxUserInviteRevoked: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserInviteRejected(Sandbox sandbox, User user) {
        
        LOGGER.info("sandboxUserInviteRejected");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_INVITATION_REJECTED);

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxUserInviteRejected: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserRemoved(Sandbox sandbox, User user, User removedUser) {
        
        LOGGER.info("sandboxUserRemoved");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_REMOVED);
        sandboxActivityLog.setAdditionalInfo(removedUser.getSbmUserId());

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxUserRemoved: "
        +"Parameters: sandbox = "+sandbox+", user = "+user+", removedUser = "+removedUser
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @Transactional
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserInvited(Sandbox sandbox, User user, User invitedUser) {
        
        LOGGER.info("sandboxUserInvited");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_INVITED);
        sandboxActivityLog.setAdditionalInfo("User Email: " + invitedUser.getEmail());

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxUserInvited: "
        +"Parameters: sandbox = "+sandbox+", user = "+user+", invitedUser = "+invitedUser
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxOpenEndpoint(Sandbox sandbox, User user, Boolean openEndpoint) {
        
        LOGGER.info("sandboxOpenEndpoint");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.OPEN_ENDPOINT);
        sandboxActivityLog.setAdditionalInfo(openEndpoint == Boolean.TRUE ? "Open Endpoint Enabled" : "Open Endpoint Disabled");
        
        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxOpenEndpoint: "
        +"Parameters: sandbox = "+sandbox+", user = "+user+", openEndpoint = "+openEndpoint
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxUserAdded(Sandbox sandbox, User user) {
        
        LOGGER.info("sandboxUserAdded");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_ADDED);

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxUserAdded: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public SandboxActivityLog sandboxUserRoleChange(Sandbox sandbox, User user, Role role, boolean roleAdded) {
        
        LOGGER.info("sandboxUserRoleChange");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_SANDBOX_ROLE_CHANGE);
        sandboxActivityLog.setAdditionalInfo("Role " + role.toString() + (roleAdded ? " added" : " removed"));

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxUserRoleChange: "
        +"Parameters: sandbox = "+sandbox+", user = "+user+", role = "+role+", roleAdded = "+roleAdded
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxImport(Sandbox sandbox, User user) {
        
        LOGGER.info("sandboxImport");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.SANDBOX_DATA_IMPORT);

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxImport: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog sandboxReset(Sandbox sandbox, User user) {
        
        LOGGER.info("sandboxReset");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.SANDBOX_RESET);

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("sandboxReset: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public SandboxActivityLog systemUserCreated(Sandbox sandbox, User user) {
        
        LOGGER.info("systemUserCreated");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(sandbox, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_CREATED);
        sandboxActivityLog.setAdditionalInfo("SBM User Id " + user.getSbmUserId());

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("systemUserCreated: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    public SandboxActivityLog systemUserRoleChange(User user, SystemRole systemRole, boolean roleAdded) {
        
        LOGGER.info("systemUserRoleChange");

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(null, user);
        sandboxActivityLog.setActivity(SandboxActivity.USER_SYSTEM_ROLE_CHANGE);
        sandboxActivityLog.setAdditionalInfo("Role " + systemRole.toString() + (roleAdded ? " added" : " removed"));
        
        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("systemUserRoleChange: "
        +"Parameters: user = "+user+", systemRole = "+systemRole+", roleAdded = "+roleAdded
        +"; Return value = "+retVal);

        return retVal;
    }

    @Override
    @PublishAtomicMetric
    public SandboxActivityLog userDelete(final User user) {
        
        LOGGER.info("userDelete");

        List<SandboxActivityLog> sandboxActivityLogList = findByUserId(user.getId());
        for (SandboxActivityLog sandboxActivityLog : sandboxActivityLogList) {
            delete(sandboxActivityLog);
        }

        SandboxActivityLog sandboxActivityLog = createSandboxActivityLog(null, null);
        sandboxActivityLog.setAdditionalInfo(userToJson(user));
        sandboxActivityLog.setActivity(SandboxActivity.USER_DELETED);

        SandboxActivityLog retVal = this.save(sandboxActivityLog);

        LOGGER.debug("userDelete: "
        +"Parameters: user = "+user+"; Return value = "+retVal);

        return retVal;
    }



    @Override
    public List<SandboxActivityLog> findBySandboxId(String sandboxId) {
        
        LOGGER.info("findBySandboxId");

        LOGGER.debug("findBySandboxId: "
        +"Parameters: sandboxId = "+sandboxId
        +"; Return value = "+repository.findBySandboxId(sandboxId));

        return repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<SandboxActivityLog> findByUserSbmUserId(String sbmUserId) {
        
        LOGGER.info("findByUserSbmUserId");

        LOGGER.debug("findByUserSbmUserId: "
        +"Parameters: sbmUserId = "+sbmUserId
        +"; Return value = "+repository.findByUserSbmUserId(sbmUserId));

        return repository.findByUserSbmUserId(sbmUserId);
    }

    @Override
    public List<SandboxActivityLog> findByUserId(int userId) {
        
        LOGGER.info("findByUserId");

        LOGGER.debug("findByUserId: "
        +"Parameters: userId = "+userId
        +"; Return value = "+repository.findByUserId(userId));

        return repository.findByUserId(userId);
    }

    @Override
    public List<SandboxActivityLog> findBySandboxActivity(SandboxActivity sandboxActivity) {
        
        LOGGER.info("findBySandboxActivity");

        LOGGER.debug("findBySandboxActivity: "
        +"Parameters: sandboxActivity = "+sandboxActivity
        +"; Return value = "+repository.findBySandboxActivity(sandboxActivity));

        return repository.findBySandboxActivity(sandboxActivity);
    }

    @Override
    public Iterable<SandboxActivityLog> findAll() {
        
        LOGGER.info("findAll");

        LOGGER.debug("findAll: "
        +"No input parameters; Return value = "+repository.findAll());

        return repository.findAll();
    }

    @Override
    public List<SandboxActivityLog> findAllForSpecificTimePeriod(Timestamp beginDate, Timestamp endDate) {
        
        LOGGER.info("findAllForSpecificTimePeriod");

        LOGGER.debug("findAllForSpecificTimePeriod: "
        +"Parameters: beginDate = "+beginDate+", endDate = "+endDate
        +"; Return value = "+repository.findAllForSpecificTimePeriod(beginDate, endDate));

        return repository.findAllForSpecificTimePeriod(beginDate, endDate);
    }

    @Override
    public String intervalActive(final Timestamp intervalTime) {
        
        LOGGER.info("intervalActive");

        String retVal = repository.intervalActive(intervalTime);

        LOGGER.debug("intervalActive: "
        +"Parameters: intervalTime = "+intervalTime
        +"; Return value = "+retVal);

        return retVal;
    }

    private SandboxActivityLog createSandboxActivityLog(Sandbox sandbox, User user) {
        
        LOGGER.info("createSandboxActivityLog");

        SandboxActivityLog sandboxActivityLog = new SandboxActivityLog();
        sandboxActivityLog.setSandbox(sandbox);
        sandboxActivityLog.setUser(user);
        sandboxActivityLog.setTimestamp(new Timestamp(new Date().getTime()));

        LOGGER.debug("createSandboxActivityLog: "
        +"Parameters: sandbox = "+sandbox+", user = "+user
        +"; Return value = "+sandboxActivityLog);

        return sandboxActivityLog;
    }

    private static String toJson(Sandbox sandbox) {
        
        LOGGER.info("toJson");

        Gson gson = new Gson();
        Type type = new TypeToken<Sandbox>() {
        }.getType();

        LOGGER.debug("toJson: "
        +"Parameters: sandbox = "+sandbox
        +"; Return value = "+gson.toJson(sandbox, type));

        return gson.toJson(sandbox, type);
    }

    private static String userToJson(User user) {
        
        LOGGER.info("userToJson");

        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();

        LOGGER.debug("userToJson: "
        +"Parameters: user = "+user+"; Return value = "+gson.toJson(user, type));

        return gson.toJson(user, type);
    }

}
