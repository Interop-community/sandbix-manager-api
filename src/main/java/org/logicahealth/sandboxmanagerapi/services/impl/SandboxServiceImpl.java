package org.logicahealth.sandboxmanagerapi.services.impl;

import com.amazonaws.services.cloudwatch.model.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.repositories.SandboxRepository;
import org.logicahealth.sandboxmanagerapi.repositories.UserSandboxRepository;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class SandboxServiceImpl implements SandboxService {

    @Value("${hspc.platform.defaultPublicSandboxRoles}")
    private String[] defaultPublicSandboxRoles;

    @Value("${hspc.platform.defaultPrivateSandboxRoles}")
    private String[] defaultPrivateSandboxRoles;

    @Value("${hspc.platform.defaultSandboxCreatorRoles}")
    private String[] defaultSandboxCreatorRoles;

    @Value("${hspc.platform.defaultSandboxVisibility}")
    private String defaultSandboxVisibility;

    private static Logger LOGGER = LoggerFactory.getLogger(SandboxServiceImpl.class.getName());
    private final SandboxRepository repository;

    @Autowired
    private ApiEndpointIndex apiEndpointIndexObj;

    @Value("${hspc.platform.api.oauthUserInfoEndpointURL}")
    private String oauthUserInfoEndpointURL;

    @Value("${expiration-message}")
    private String expirationMessage;

    @Value("${expiration-date}")
    private String expirationDate;

    @Value("${default-public-apps}")
    private String[] defaultPublicApps;

    private UserService userService;
    private UserRoleService userRoleService;
    private UserPersonaService userPersonaService;
    private UserLaunchService userLaunchService;
    private AppService appService;
    private LaunchScenarioService launchScenarioService;
    private SandboxImportService sandboxImportService;
    private SandboxActivityLogService sandboxActivityLogService;
    private RuleService ruleService;
    private UserAccessHistoryService userAccessHistoryService;
    private SandboxInviteService sandboxInviteService;
    private CloseableHttpClient httpClient;
    @Qualifier("sandboxDeleteHttpClient")
    private CloseableHttpClient sandboxDeleteHttpClient;
    private CdsServiceEndpointService cdsServiceEndpointService;
    private FhirProfileDetailService fhirProfileDetailService;
    private FhirProfileService fhirProfileService;
    private SandboxBackgroundTasksService sandboxBackgroundTasksService;
    private UserSandboxRepository userSandboxRepository;
    private CdsHookService cdsHookService;

    private static final int SANDBOXES_TO_RETURN = 2;
    private static final String CLONED_SANDBOX = "cloned";
    private static final String SAVED_SANDBOX = "saved";
    private static final String FHIR_SERVER_VERSION = "platform-version";
    private static final String HAPI_VERSION = "hapi-version";
    private static final String FHIR_VERSION = "fhir-version";
    private static final String IMAGE_FOLDER = "img/";
    private static final String PATIENT_FHIR_QUERY = "Patient?_id=";

    @Inject
    public SandboxServiceImpl(final SandboxRepository repository) {
        this.repository = repository;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setUserRoleService(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @Inject
    public void setUserPersonaService(UserPersonaService userPersonaService) {
        this.userPersonaService = userPersonaService;
    }

    @Inject
    public void setUserLaunchService(UserLaunchService userLaunchService) {
        this.userLaunchService = userLaunchService;
    }

    @Inject
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Inject
    public void setLaunchScenarioService(LaunchScenarioService launchScenarioService) {
        this.launchScenarioService = launchScenarioService;
    }

    @Inject
    public void setSandboxImportService(SandboxImportService sandboxImportService) {
        this.sandboxImportService = sandboxImportService;
    }

    @Inject
    public void setSandboxActivityLogService(SandboxActivityLogService sandboxActivityLogService) {
        this.sandboxActivityLogService = sandboxActivityLogService;
    }

    @Inject
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
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
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Inject
    public void setSandboxDeleteHttpClient(CloseableHttpClient sandboxDeleteHttpClient) {
        this.sandboxDeleteHttpClient = sandboxDeleteHttpClient;
    }

    @Inject
    public void setCdsServiceEndpointService(CdsServiceEndpointService cdsServiceEndpointService) {
        this.cdsServiceEndpointService = cdsServiceEndpointService;
    }

    @Inject
    public void setFhirProfileDetailService(@Lazy FhirProfileDetailService fhirProfileDetailService) {
        this.fhirProfileDetailService = fhirProfileDetailService;
    }

    @Inject
    public void setFhirProfileService(@Lazy FhirProfileService fhirProfileService) {
        this.fhirProfileService = fhirProfileService;
    }

    @Inject
    public void setSandboxBackgroundTasksService(@Lazy SandboxBackgroundTasksService sandboxBackgroundTasksService) {
        this.sandboxBackgroundTasksService = sandboxBackgroundTasksService;
    }

    @Inject
    public void setUserSandboxRepository(@Lazy UserSandboxRepository userSandboxRepository) {
        this.userSandboxRepository = userSandboxRepository;
    }

    @Inject
    public void setCdsHookService(CdsHookService cdsHookService) {
        this.cdsHookService = cdsHookService;
    }

    @Override
    @Transactional
    public void deleteQueuedSandboxes() {
        var queuedSandboxes = repository.findByCreationStatus(SandboxCreationStatus.QUEUED);
        queuedSandboxes.forEach(this::delete);
    }

    private void delete(final Sandbox sandbox) {
        deleteSandboxFromSandman(sandbox, null);
    }

    @Override
    public void delete(final int id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public synchronized void delete(final Sandbox sandbox, final String bearerToken, final User admin, final boolean sync) {
        if (!sync) {
            // Want this done first in case there's an error with Reference API so that everything else doesn't get deleted
            try {
                callDeleteSandboxAPI(sandbox, bearerToken);
            } catch (Exception ex) {
                throw new SandboxDeleteFailedException(String.format("Failed to delete sandbox: %s %n %s", sandbox.getSandboxId(), ex.getMessage()), ex);
            }
        }

        deleteSandboxFromSandman(sandbox, admin);

    }

    private void deleteSandboxFromSandman(final Sandbox sandbox, final User admin) {

        deleteAllSandboxItems(sandbox);

        List<SandboxImport> imports = sandbox.getImports();
        for (SandboxImport sandboxImport : imports) {
            sandboxImportService.delete(sandboxImport);
        }
        sandbox.setImports(null);
        save(sandbox);

        //remove user memberships
        removeAllMembers(sandbox);

        if (admin != null) {
            sandboxActivityLogService.sandboxDelete(sandbox, admin);
        } else {
            sandboxActivityLogService.sandboxDelete(sandbox, sandbox.getCreatedBy());
        }

        this.userSandboxRepository.deleteAllBySandboxId(sandbox.getId());

        delete(sandbox.getId());

    }

    @Override
    @Transactional
    public synchronized void delete(final Sandbox sandbox, final String bearerToken) {
        delete(sandbox, bearerToken, null, false);
    }

    private void deleteAllSandboxItems(final Sandbox sandbox) {

        deleteSandboxItemsExceptApps(sandbox);

        //delete all registered app, authClients, images
        List<App> apps = appService.findBySandboxIdIncludingCustomApps(sandbox.getSandboxId());
        for (App app : apps) {
            appService.delete(app);
        }
    }

    private void deleteSandboxItemsExceptApps(final Sandbox sandbox) {

        //delete launch scenarios, context params
        List<LaunchScenario> launchScenarios = launchScenarioService.findBySandboxId(sandbox.getSandboxId());
        for (LaunchScenario launchScenario : launchScenarios) {
            launchScenarioService.delete(launchScenario);
        }

        List<UserPersona> userPersonas = userPersonaService.findBySandboxId(sandbox.getSandboxId());
        for (UserPersona userPersona : userPersonas) {
            userPersonaService.delete(userPersona);
        }

        List<SandboxInvite> sandboxInvites = sandboxInviteService.findInvitesBySandboxId(sandbox.getSandboxId());
        for (SandboxInvite sandboxInvite : sandboxInvites) {
            sandboxInviteService.delete(sandboxInvite);
        }

        List<CdsServiceEndpoint> cdsServiceEndpoints = cdsServiceEndpointService.findBySandboxId(sandbox.getSandboxId());
        for (CdsServiceEndpoint cdsServiceEndpoint : cdsServiceEndpoints) {
            cdsServiceEndpointService.delete(cdsServiceEndpoint);
        }

        List<FhirProfileDetail> fhirProfileDetails = fhirProfileDetailService.getAllProfilesForAGivenSandbox(sandbox.getSandboxId());
        for (FhirProfileDetail fhirProfileDetail : fhirProfileDetails) {
            fhirProfileDetailService.delete(fhirProfileDetail.getId());
        }

        userAccessHistoryService.deleteUserAccessInstancesForSandbox(sandbox);
    }

    // TODO: create no longer used
    @Override
    @Transactional
    public Sandbox create(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException {

        Boolean canCreate = ruleService.checkIfUserCanCreateSandbox(user, bearerToken);
        if (!canCreate) {
            return null;
        }
        UserPersona userPersona = userPersonaService.findByPersonaUserId(user.getSbmUserId());

        if (userPersona == null && callCreateOrUpdateSandboxAPI(sandbox, bearerToken)) {
            sandbox.setCreatedBy(user);
            sandbox.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            sandbox.setVisibility(Visibility.valueOf(defaultSandboxVisibility));

            sandbox.setPayerUserId(user.getId());
            Sandbox savedSandbox = save(sandbox);
            addMember(savedSandbox, user, Role.ADMIN);
            for (String roleName : defaultSandboxCreatorRoles) {
                addMemberRole(sandbox, user, Role.valueOf(roleName));
            }
            sandboxActivityLogService.sandboxCreate(sandbox, user);
            return savedSandbox;
        }
        return null;
    }

    @Override
    @Transactional
    public void clone(final Sandbox newSandbox, final String clonedSandboxId, final User user, final String bearerToken) throws UnsupportedEncodingException {
        Map<String, Sandbox> savedAndClonedSandboxes = cloneSandbox(newSandbox, clonedSandboxId, user, bearerToken);
        if (savedAndClonedSandboxes != null) {
            this.sandboxBackgroundTasksService.cloneSandboxSchema(savedAndClonedSandboxes.get(SAVED_SANDBOX), savedAndClonedSandboxes.get((CLONED_SANDBOX)), user, bearerToken, getSandboxApiURL(newSandbox));
        }
    }

    private Map<String, Sandbox> cloneSandbox(final Sandbox newSandbox, final String clonedSandboxId, final User user, final String bearerToken) {
        Boolean canCreate = ruleService.checkIfUserCanCreateSandbox(user, bearerToken);
        if (!canCreate) {
            return null;
        }
        UserPersona initialUserPersona = userPersonaService.findByPersonaUserId(user.getSbmUserId());
        Sandbox clonedSandbox = findBySandboxId(clonedSandboxId);
        Sandbox newSandboxExists = findBySandboxId(newSandbox.getSandboxId());
        if (newSandboxExists != null) {
            throw new IllegalArgumentException("Sandbox with id " + newSandbox.getSandboxId() + " already exists.");
        }
        if (clonedSandbox == null) {
            throw new ResourceNotFoundException("Cloned sandbox does not exist.");
        }
        if (initialUserPersona == null) {// && callCloneSandboxApi(newSandbox, existingSandbox, bearerToken)) {
            newSandbox.setCreatedBy(user);
            newSandbox.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            newSandbox.setVisibility(Visibility.valueOf(defaultSandboxVisibility));
            newSandbox.setPayerUserId(user.getId());
            newSandbox.setCreationStatus(SandboxCreationStatus.QUEUED);
            Sandbox savedSandbox = save(newSandbox);
            addMember(savedSandbox, user, Role.ADMIN);
            for (String roleName : defaultSandboxCreatorRoles) {
                addMemberRole(savedSandbox, user, Role.valueOf(roleName));
            }
            sandboxActivityLogService.sandboxCreate(newSandbox, user);
            if (newSandbox.getDataSet()
                          .equals(DataSet.DEFAULT)) {
                cloneUserPersonas(savedSandbox, clonedSandbox, user);
            }
            if (newSandbox.getApps()
                          .equals(DataSet.DEFAULT)) {
                if (newSandbox.getDataSet()
                              .equals(DataSet.DEFAULT)) {
                    cloneApps(savedSandbox, clonedSandbox, user);
                    cloneLaunchScenarios(savedSandbox, clonedSandbox, user);
                } else {
                    //Clone only the apps if the dataset is empty
                    cloneApps(savedSandbox, clonedSandbox, user);
                }
            }
            Map<String, Sandbox> savedAndClonedSandboxes = new HashMap<>(SANDBOXES_TO_RETURN);
            savedAndClonedSandboxes.put(SAVED_SANDBOX, savedSandbox);
            savedAndClonedSandboxes.put(CLONED_SANDBOX, clonedSandbox);
            return savedAndClonedSandboxes;
        }
        return null;
    }

    @Override
    @Transactional
    public Sandbox update(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException {
        Sandbox existingSandbox = findBySandboxId(sandbox.getSandboxId());
        existingSandbox.setName(sandbox.getName());
        existingSandbox.setDescription(sandbox.getDescription());
        if (existingSandbox.isAllowOpenAccess() != sandbox.isAllowOpenAccess()) {
            sandboxActivityLogService.sandboxOpenEndpoint(existingSandbox, user, sandbox.isAllowOpenAccess());
            existingSandbox.setAllowOpenAccess(sandbox.isAllowOpenAccess());
            callCreateOrUpdateSandboxAPI(existingSandbox, bearerToken);
        }
        return save(existingSandbox);
    }

    @Override
    @Transactional
    public void removeMember(final Sandbox sandbox, final User user, final String bearerToken) {
        if (user != null) {
            userService.removeSandbox(sandbox, user);

            //delete launch scenarios, context params
            List<LaunchScenario> launchScenarios = launchScenarioService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId());
            launchScenarios.stream()
                           .filter(launchScenario -> launchScenario.getVisibility() == Visibility.PRIVATE)
                           .forEach(launchScenarioService::delete);

            //delete user launches for public launch scenarios in this sandbox
            List<UserLaunch> userLaunches = userLaunchService.findByUserId(user.getSbmUserId());
            userLaunches.stream()
                        .filter(userLaunch -> userLaunch.getLaunchScenario()
                                                        .getSandbox()
                                                        .getSandboxId()
                                                        .equalsIgnoreCase(sandbox.getSandboxId()))
                        .forEach(userLaunchService::delete);

            //delete all registered app, authClients, images
            List<App> apps = appService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId());
            apps.stream()
                .filter(app -> app.getVisibility() == Visibility.PRIVATE)
                .forEach(appService::delete);

            List<UserPersona> userPersonas = userPersonaService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId());
            userPersonas.stream()
                        .filter(userPersona -> userPersona.getVisibility() == Visibility.PRIVATE)
                        .forEach(userPersonaService::delete);

            List<UserRole> allUserRoles = sandbox.getUserRoles();
            List<UserRole> currentUserRoles = new ArrayList<>();
            Iterator<UserRole> iterator = allUserRoles.iterator();
            while (iterator.hasNext()) {
                UserRole userRole = iterator.next();
                if (userRole.getUser()
                            .getId()
                            .equals(user.getId())) {
                    currentUserRoles.add(userRole);
                    iterator.remove();
                }
            }
            if (!currentUserRoles.isEmpty()) {
                sandbox.setUserRoles(allUserRoles);
                save(sandbox);
                currentUserRoles.forEach(userRoleService::delete);
            }
            sandboxActivityLogService.sandboxUserRemoved(sandbox, sandbox.getCreatedBy(), user);
        }
    }

    @Override
    @Transactional
    public void addMember(final Sandbox sandbox, final User user) {
        String[] defaultRoles = sandbox.getVisibility() == Visibility.PUBLIC ? defaultPublicSandboxRoles : defaultPrivateSandboxRoles;
        for (String roleName : defaultRoles) {
            addMemberRole(sandbox, user, Role.valueOf(roleName));
        }
    }

    @Override
    @Transactional
    public void addMember(final Sandbox sandbox, final User user, final Role role) {
        List<UserRole> userRoles = sandbox.getUserRoles();
        userRoles.add(new UserRole(user, role));
        sandboxActivityLogService.sandboxUserRoleChange(sandbox, user, role, true);
        sandbox.setUserRoles(userRoles);
        userService.addSandbox(sandbox, user);
        sandboxActivityLogService.sandboxUserAdded(sandbox, user);
        save(sandbox);
    }

    @Override
    @Transactional
    public void addMemberRole(final Sandbox sandbox, final User user, final Role role) {
        if (hasMemberRole(sandbox, user, role)) {
            return;
        }
        if (!isSandboxMember(sandbox, user)) {
            addMember(sandbox, user, role);
        } else {
            List<UserRole> userRoles = sandbox.getUserRoles();
            userRoles.add(new UserRole(user, role));
            sandboxActivityLogService.sandboxUserRoleChange(sandbox, user, role, true);
            sandbox.setUserRoles(userRoles);
            save(sandbox);
        }
    }

    @Override
    @Transactional
    public void removeMemberRole(final Sandbox sandbox, final User user, final Role role) {
        if (isSandboxMember(sandbox, user)) {
            List<UserRole> allUserRoles = sandbox.getUserRoles();
            UserRole removeUserRole = null;
            Iterator<UserRole> iterator = allUserRoles.iterator();
            while (iterator.hasNext()) {
                UserRole userRole = iterator.next();
                if (userRole.getUser()
                            .getId()
                            .equals(user.getId()) &&
                        userRole.getRole()
                                .equals(role)) {
                    removeUserRole = userRole;
                    iterator.remove();
                }
            }
            if (removeUserRole != null) {
                sandbox.setUserRoles(allUserRoles);
                save(sandbox);
                userRoleService.delete(removeUserRole);
            }
        }
    }

    @Override
    @Transactional
    public void changePayerForSandbox(final Sandbox sandbox, final User payer) {
        sandbox.setPayerUserId(payer.getId());
        save(sandbox);
    }

    @Override
    public boolean hasMemberRole(final Sandbox sandbox, final User user, final Role role) {
        List<UserRole> userRoles = sandbox.getUserRoles();
        for (UserRole userRole : userRoles) {
            if (userRole.getUser()
                        .getSbmUserId()
                        .equalsIgnoreCase(user.getSbmUserId()) && userRole.getRole() == role) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addSandboxImport(final Sandbox sandbox, final SandboxImport sandboxImport) {
        List<SandboxImport> imports = sandbox.getImports();
        imports.add(sandboxImport);
        sandbox.setImports(imports);
        save(sandbox);
    }

    @Override
    public void reset(final Sandbox sandbox, final String bearerToken) {
        deleteSandboxItemsExceptApps(sandbox);
    }

    @Override
    public boolean isSandboxMember(final Sandbox sandbox, final User user) {
        for (UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser()
                        .getSbmUserId()
                        .equalsIgnoreCase(user.getSbmUserId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void sandboxLogin(final String sandboxId, final String userId) {
        Sandbox sandbox = findBySandboxId(sandboxId);
        User user = userService.findBySbmUserId(userId);
        if (isSandboxMember(sandbox, user)) {
            sandboxActivityLogService.sandboxLogin(sandbox, user);
        }
    }

    @Override
    @Transactional
    public Sandbox save(final Sandbox sandbox) {
        return repository.save(sandbox);
    }

    @Override
    public List<Sandbox> getAllowedSandboxes(final User user) {
        List<Sandbox> sandboxes = new ArrayList<>();
        if (user != null) {
            sandboxes = user.getSandboxes();
        }

        for (Sandbox sandbox : findByVisibility(Visibility.PUBLIC)) {
            List<UserRole> userRoles = sandbox.getUserRoles();
            userRoles.removeIf((UserRole userRole) -> !userRole.getUser()
                                                               .getSbmUserId()
                                                               .equals(user.getSbmUserId()));
            if (!sandboxes.contains(sandbox)) {
                sandboxes.add(sandbox);
            }
        }

        return sandboxes;
    }

    @Override
    public Sandbox findBySandboxId(final String sandboxId) {
        return repository.findBySandboxId(sandboxId);
    }

    @Override
    public List<Sandbox> findByVisibility(final Visibility visibility) {
        return repository.findByVisibility(visibility);
    }

    @Override
    public String fullCount() {
        return repository.fullCount();
    }

    @Override
    public String fullCountForSpecificTimePeriod(Timestamp endDate) {
        return repository.fullCountForSpecificTimePeriod(endDate);
    }

    @Override
    public String schemaCount(String apiEndpointIndex) {
        return repository.schemaCount(apiEndpointIndex);
    }

    @Override
    public String schemaCountForSpecificTimePeriod(final String apiEndpointIndex, final Timestamp endDate) {
        return repository.schemaCountForSpecificTimePeriod(apiEndpointIndex, endDate);
    }

    @Override
    public String intervalCount(Timestamp intervalTime) {
        return repository.intervalCount(intervalTime);
    }

    @Override
    public String newSandboxesInIntervalCount(Timestamp intervalTime, String apiEndpointIndex) {
        return repository.newSandboxesInIntervalCount(intervalTime, apiEndpointIndex);
    }

    @Override
    public List<Sandbox> findByPayerId(Integer payerId) {
        return repository.findByPayerUserId(payerId);
    }

    @Override
    public String getSandboxApiURL(final Sandbox sandbox) {
        return getApiSchemaURL(sandbox.getApiEndpointIndex()) + "/" + sandbox.getSandboxId();
    }

    @Override
    public String getSystemSandboxApiURL() {
        return apiEndpointIndexObj.getCurrent()
                                  .getApiBaseURL_dstu2() + "/system";
    }

    @Override
    public Iterable<Sandbox> findAll() {
        return repository.findAll();
    }

    @Override
    public String getApiSchemaURL(final String apiEndpointIndex) {

        if (apiEndpointIndex.equals(apiEndpointIndexObj.getPrev()
                                                       .getDstu2())) {
            return apiEndpointIndexObj.getPrev()
                                      .getApiBaseURL_dstu2();
        }
        if (apiEndpointIndex.equals(apiEndpointIndexObj.getPrev()
                                                       .getStu3())) {
            return apiEndpointIndexObj.getPrev()
                                      .getApiBaseURL_stu3();
        }
        if (apiEndpointIndex.equals(apiEndpointIndexObj.getPrev()
                                                       .getR4())) {
            return apiEndpointIndexObj.getPrev()
                                      .getApiBaseURL_r4();
        }
        if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent()
                                                       .getDstu2())) {
            return apiEndpointIndexObj.getCurrent()
                                      .getApiBaseURL_dstu2();
        }
        if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent()
                                                       .getStu3())) {
            return apiEndpointIndexObj.getCurrent()
                                      .getApiBaseURL_stu3();
        }
        if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent()
                                                       .getR4())) {
            return apiEndpointIndexObj.getCurrent()
                                      .getApiBaseURL_r4();
        }
        if (apiEndpointIndex.equals(apiEndpointIndexObj.getCurrent()
                                                       .getR5())) {
            return apiEndpointIndexObj.getCurrent()
                                      .getApiBaseURL_r5();
        }
        return "";
    }

    private void removeAllMembers(final Sandbox sandbox) {

        List<UserRole> userRoles = sandbox.getUserRoles();
        sandbox.setUserRoles(Collections.emptyList());
        save(sandbox);

        for (UserRole userRole : userRoles) {
            userService.removeSandbox(sandbox, userRole.getUser());
            userRoleService.delete(userRole);
        }
    }

    private boolean callCreateOrUpdateSandboxAPI(final Sandbox sandbox, final String bearerToken) throws UnsupportedEncodingException {
        String url = getSandboxApiURL(sandbox) + "/sandbox";
        if (!sandbox.getDataSet()
                    .equals(DataSet.NA)) {
            url = getSandboxApiURL(sandbox) + "/sandbox?dataSet=" + sandbox.getDataSet();
        }

        // TODO: change to using 'simpleRestTemplate'
        HttpPut putRequest = new HttpPut(url);
        putRequest.addHeader("Content-Type", "application/json");
        StringEntity entity;

        String jsonString = "{\"teamId\": \"" + sandbox.getSandboxId() + "\",\"allowOpenAccess\": \"" + sandbox.isAllowOpenAccess() + "\"}";
        entity = new StringEntity(jsonString);
        putRequest.setEntity(entity);
        putRequest.setHeader("Authorization", "BEARER " + bearerToken);

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(putRequest)) {
            if (closeableHttpResponse.getStatusLine()
                                     .getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                String errorMsg = String.format("There was a problem creating the sandbox.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            return true;
        } catch (IOException e) {
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
//            try {
//                httpClient.close();
//            }catch (IOException e) {
//                LOGGER.error("Error closing HttpClient");
//            }
        }
    }

    private boolean callDeleteSandboxAPI(final Sandbox sandbox, final String bearerToken) {
        String url = getSandboxApiURL(sandbox) + "/sandbox";

        // TODO: change to using 'simpleRestTemplate'
        HttpDelete deleteRequest = new HttpDelete(url);
        deleteRequest.addHeader("Authorization", "BEARER " + bearerToken);

        try (CloseableHttpResponse closeableHttpResponse = sandboxDeleteHttpClient.execute(deleteRequest)) {
            if (closeableHttpResponse.getStatusLine()
                                     .getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                String errorMsg = String.format("There was a problem deleting the sandbox.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            return true;
        } catch (IOException e) {
            LOGGER.error("Error deleting to " + url, e);
            throw new RuntimeException(e);
        } finally {
//            try {
//                httpClient.close();
//            }catch (IOException e) {
//                LOGGER.error("Error closing HttpClient");
//            }
        }
    }

    private java.sql.Date formatDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date parsed = format.parse(expirationDate);
            return new java.sql.Date(parsed.getTime());
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return null;
        }
    }

    private void cloneUserPersonas(Sandbox newSandbox, Sandbox existingSandbox, User user) {
        List<UserPersona> userPersonas = userPersonaService.findBySandboxId(existingSandbox.getSandboxId());
        for (UserPersona userPersona : userPersonas) {
            UserPersona newUserPersona = new UserPersona();
            newUserPersona.setPassword(userPersona.getPassword());
            newUserPersona.setSandbox(newSandbox);
            String[] personaSplit = userPersona.getPersonaUserId()
                                               .split("@");
            newUserPersona.setPersonaUserId(personaSplit[0] + "@" + newSandbox.getSandboxId());
            newUserPersona.setCreatedBy(user);
            newUserPersona.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            newUserPersona.setVisibility(userPersona.getVisibility());
            newUserPersona.setFhirId(userPersona.getFhirId());
            newUserPersona.setFhirName(userPersona.getFhirName());
            newUserPersona.setPersonaName(userPersona.getPersonaName());
            newUserPersona.setResource(userPersona.getResource());
            newUserPersona.setResourceUrl(userPersona.getResourceUrl());
            userPersonaService.save(newUserPersona);
        }
    }

    private void cloneApps(Sandbox newSandbox, Sandbox existingSandbox, User user) {
        List<App> clonedSmartApps = appService.findBySandboxId(existingSandbox.getSandboxId());
        for (App app : clonedSmartApps) {
            App newApp = new App();
            newApp.setBriefDescription(app.getBriefDescription());
            newApp.setClientId(app.getClientId());
            newApp.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            newApp.setAuthor(app.getAuthor());
            newApp.setInfo(app.getInfo());
            newApp.setManifestUrl(app.getManifestUrl());
            newApp.setSamplePatients(app.getSamplePatients());
            newApp.setCreatedBy(user);
            newApp.setLogoUri(app.getLogoUri());
            newApp.setLogo(app.getLogo());
            newApp.setLaunchUri(app.getLaunchUri());
            newApp.setClientName(app.getClientName());
            newApp.setClientUri(app.getClientUri());
            newApp.setFhirVersions(app.getFhirVersions());

            newApp.setSandbox(newSandbox);
            newApp.setVisibility(Visibility.PUBLIC);
            newApp.setCopyType(CopyType.REPLICA);

            appService.save(newApp);
        }

    }

    private void cloneLaunchScenarios(Sandbox newSandbox, Sandbox existingSandbox, User user) {
        List<LaunchScenario> launchScenarios = launchScenarioService.findBySandboxId(existingSandbox.getSandboxId());
        for (LaunchScenario launchScenario : launchScenarios) {
            LaunchScenario newLaunchScenario = new LaunchScenario();
            newLaunchScenario.setSandbox(newSandbox);
            newLaunchScenario.setPatient(launchScenario.getPatient());
            newLaunchScenario.setPatientName(launchScenario.getPatientName());
            newLaunchScenario.setNeedPatientBanner(launchScenario.getNeedPatientBanner());
            newLaunchScenario.setLocation(launchScenario.getLocation());
            newLaunchScenario.setEncounter(launchScenario.getEncounter());
            newLaunchScenario.setIntent(launchScenario.getIntent());
            newLaunchScenario.setResource(launchScenario.getResource());
            newLaunchScenario.setSmartStyleUrl(launchScenario.getSmartStyleUrl());
            newLaunchScenario.setLastLaunchSeconds(launchScenario.getLastLaunchSeconds());
            newLaunchScenario.setApp(appService.findByLaunchUriAndClientIdAndSandboxId(launchScenario.getApp()
                                                                                                     .getLaunchUri(), launchScenario.getApp()
                                                                                                                                    .getClientId(), newSandbox.getSandboxId()));
            List<ContextParams> contextParamsList = launchScenario.getContextParams();
            List<ContextParams> newContextParamsList = new ArrayList<>();
            for (ContextParams contextParams : contextParamsList) {
                ContextParams newContextParams = new ContextParams();
                newContextParams.setName(contextParams.getName());
                newContextParams.setValue(contextParams.getValue());
                newContextParamsList.add(newContextParams);
            }
            newLaunchScenario.setContextParams(newContextParamsList);
            newLaunchScenario.setCreatedBy(user);
            newLaunchScenario.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            newLaunchScenario.setDescription(launchScenario.getDescription());
            newLaunchScenario.setTitle(launchScenario.getTitle());
            String personaId = launchScenario.getUserPersona()
                                             .getPersonaUserId()
                                             .split("@")[0] + "@" + newLaunchScenario.getSandbox()
                                                                                     .getSandboxId();
            newLaunchScenario.setUserPersona(userPersonaService.findByPersonaUserIdAndSandboxId(personaId, newLaunchScenario.getSandbox()
                                                                                                                            .getSandboxId()));
            newLaunchScenario.setVisibility(launchScenario.getVisibility());
            launchScenarioService.save(newLaunchScenario);
        }
    }

    @Override
    public String newSandboxesInIntervalCountForSpecificTimePeriod(String apiEndpointIndex, Timestamp beginDate, Timestamp endDate) {
        return repository.newSandboxesInIntervalCountForSpecificTimePeriod(apiEndpointIndex, beginDate, endDate);
    }

    @Override
    public String intervalCountForSpecificTimePeriod(Timestamp beginDate, Timestamp endDate) {
        return repository.intervalCountForSpecificTimePeriod(beginDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public SandboxCreationStatusQueueOrder getQueuedCreationStatus(String sandboxId) {
        var sandboxes = repository.findByCreationStatusOrderByCreatedTimestampAsc(SandboxCreationStatus.QUEUED);
        var maybeSandbox = IntStream.range(0, sandboxes.size())
                                    .mapToObj(i -> {
                                        if (sandboxes.get(i)
                                                     .getSandboxId()
                                                     .equals(sandboxId)) {
                                            return new SandboxCreationStatusQueueOrder(i, sandboxes.get(i)
                                                                                                   .getCreationStatus());
                                        }
                                        return null;
                                    })
                                    .filter(Objects::nonNull)
                                    .findAny();
        if (maybeSandbox.isPresent()) {
            return maybeSandbox.get();
        }
        var sandbox = repository.findBySandboxId(sandboxId);
        return new SandboxCreationStatusQueueOrder(0, sandbox.getCreationStatus());
    }

    @Override
    public StreamingResponseBody getZippedSandboxStream(Sandbox sandbox, String sbmUserId, ZipOutputStream zipOutputStream, String bearerToken) {
        return out -> {
            addSandboxFhirServerDetailsToZipFile(sandbox, zipOutputStream, bearerToken);
            var appsManifests = addAppsManifestToZipFile(sandbox.getSandboxId(), sbmUserId, zipOutputStream);
            addUserPersonasToZipFile(sandbox.getSandboxId(), sbmUserId, zipOutputStream);
            addCdsHooksToZipFile(sandbox.getSandboxId(), sbmUserId, zipOutputStream);
            addLaunchScenariosToZipFile(sandbox.getSandboxId(), sbmUserId, zipOutputStream, appsManifests);
            addProfilesToZipFile(sandbox.getSandboxId(), zipOutputStream);
            zipOutputStream.close();
        };
    }

    private void addSandboxFhirServerDetailsToZipFile(Sandbox sandbox, ZipOutputStream zipOutputStream, String bearerToken) {
        String url = getSandboxApiURL(sandbox) + "/sandbox/download";
        var downloadRequest = new HttpGet(url);
        downloadRequest.setHeader("Authorization", "BEARER " + bearerToken);

        InputStream inputStream = null;
        ZipInputStream zipInputStream = null;
        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(downloadRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                String errorMsg = String.format("There was a problem downloading the sandbox.\n" +
                                "Response Status : %s .\nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            inputStream = closeableHttpResponse.getEntity().getContent();
            zipInputStream = new ZipInputStream(inputStream);
            addZipFileEntry(zipInputStream, zipInputStream.getNextEntry(), zipOutputStream);
            zipInputStream.getNextEntry();
            addSandboxDetailsToZipFile(sandbox.getSandboxId(), zipOutputStream, Objects.requireNonNull(getZipEntryContents(zipInputStream)));
            addSandboxUserRolesAndInviteesToZipFile(sandbox.getSandboxId(), zipOutputStream);
        } catch (IOException e) {
            LOGGER.error("Exception while adding fhir server details for sandbox download", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
            }
        }

    }

    private void addZipFileEntry(InputStream inputStream, ZipEntry zipEntry, ZipOutputStream zipOutputStream) {
        try {
            zipOutputStream.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = inputStream.read(bytes)) >= 0) {
                zipOutputStream.write(bytes, 0, length);
            }
        } catch (IOException e) {
            LOGGER.error("Exception while adding zip file entry for sandbox download", e);
        }
    }

    private Map<String, String> getZipEntryContents(ZipInputStream inputStream) {
        try (var outputStream = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[1024];
            int length;
            while ((length = inputStream.read(bytes)) >= 0) {
                outputStream.write(bytes, 0, length);
            }
            return convertFhirVersionsJsonStringToMap(outputStream.toString());
        } catch (IOException e) {
            LOGGER.error("Exception while extracting fhir server versions json", e);
        }
        return null;
    }

    private Map<String, String> convertFhirVersionsJsonStringToMap(String fhirServerVersions) {
        var fhirServerVersionsMap = new HashMap<String, String>();
        var jsonObject = new JSONObject(fhirServerVersions);
        fhirServerVersionsMap.put(FHIR_SERVER_VERSION, jsonObject.getString(FHIR_SERVER_VERSION));
        fhirServerVersionsMap.put(HAPI_VERSION, jsonObject.getString(HAPI_VERSION));
        fhirServerVersionsMap.put(FHIR_VERSION, jsonObject.getString(FHIR_VERSION));
        return fhirServerVersionsMap;
    }

    private void addSandboxDetailsToZipFile(String sandboxId, ZipOutputStream zipOutputStream, Map<String, String> fhirServerVersions) {
        var sandbox = findBySandboxId(sandboxId);
        var sandboxApiURL = getSandboxApiURL(findBySandboxId(sandboxId));
        var sandboxDetails = new HashMap<String, Object>();
        sandboxDetails.put("id", sandbox.getSandboxId());
        sandboxDetails.put("name", sandbox.getName());
        sandboxDetails.put("description", sandbox.getDescription());
        sandboxDetails.put("base", sandboxApiURL.substring(0, sandboxApiURL.length() - sandboxId.length() - 1));
        sandboxDetails.put(FHIR_SERVER_VERSION, fhirServerVersions.get(FHIR_SERVER_VERSION));
        sandboxDetails.put(HAPI_VERSION, fhirServerVersions.get(HAPI_VERSION));
        sandboxDetails.put(FHIR_VERSION, fhirServerVersions.get(FHIR_VERSION));
        try (var sandboxInputStream = new ByteArrayInputStream(new GsonBuilder().setPrettyPrinting()
                                                                                .create()
                                                                                .toJson(sandboxDetails)
                                                                                .getBytes())) {
            addZipFileEntry(sandboxInputStream, new ZipEntry("sandbox.json"), zipOutputStream);
        } catch (IOException e) {
            LOGGER.error("Exception while adding sandbox details for sandbox download", e);
        }
    }

    private void addSandboxUserRolesAndInviteesToZipFile(String sandboxId, ZipOutputStream zipOutputStream) {
        var sandbox = findBySandboxId(sandboxId);
        var sandboxUsers = sandbox.getUserRoles()
                                  .stream()
                                  .map(userRole -> new SandboxUser(userRole.getUser(), userRole.getRole().getSandboxDownloadRole()))
                                  .collect(Collectors.toSet());
        var sandboxInvites = sandboxInviteService.findInvitesBySandboxId(sandboxId);
        var pendingInviteeEmails = sandboxInvites.stream()
                                                 .filter(sandboxInvite -> sandboxInvite.getStatus() == InviteStatus.PENDING)
                                                 .map(sandboxInvite -> sandboxInvite.getInvitee().getEmail())
                                                 .collect(Collectors.toList());
        var sandboxUserRolesAndInvitees = new HashMap<String, Object>();
        sandboxUsers.add(new SandboxServiceImpl.SandboxUser(sandbox.getCreatedBy(), "owner"));
        sandboxUserRolesAndInvitees.put("users", sandboxUsers);
        sandboxUserRolesAndInvitees.put("invitees", pendingInviteeEmails);
        try (var sandboxInputStream = new ByteArrayInputStream(new GsonBuilder().setPrettyPrinting()
                                                                                .create()
                                                                                .toJson(sandboxUserRolesAndInvitees).getBytes())) {
            addZipFileEntry(sandboxInputStream, new ZipEntry("users.json"), zipOutputStream);
        } catch (IOException e) {
            LOGGER.error("Exception while adding sandbox user roles and invites for sandbox download", e);
        }
        addSandboxUsersToZipFile(sandboxUsers, zipOutputStream);
    }

    private void addSandboxUsersToZipFile(Set<SandboxServiceImpl.SandboxUser> sandboxUsers, ZipOutputStream zipOutputStream) {
        var users = sandboxUsers.stream()
                                .map(SandboxUser::getEmail)
                                .distinct()
                                .collect(Collectors.joining(","));
        try (var usersInputStream = new ByteArrayInputStream(users.getBytes())) {
            addZipFileEntry(usersInputStream, new ZipEntry("users.csv"), zipOutputStream);
        } catch (IOException e) {
            LOGGER.error("Exception while adding users for sandbox download", e);
        }
    }

    @Getter
    private static class SandboxUser {
        private final String name;
        private final String email;
        private final String role;

        public SandboxUser(User user, String role) {
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = role;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SandboxUser)) {
                return false;
            }
            var otherSandboxUser = (SandboxUser) other;
            return (this.name.equals(otherSandboxUser.name) && this.email.equals(otherSandboxUser.email) && this.role.equals(otherSandboxUser.role));
        }

        @Override
        public int hashCode() {
            return this.email.hashCode() + this.email.hashCode() + this.role.hashCode();
        }
    }

    private List<AppManifestTemplate> addAppsManifestToZipFile(String sandboxId, String sbmUserId, ZipOutputStream zipOutputStream) {
        var apps = appService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, sbmUserId, Visibility.PUBLIC);
        var appsList = parseAppsListJson(apps);
        addAppImagesToZipFile(appsList, zipOutputStream);
        try (var inputStream = new ByteArrayInputStream(new GsonBuilder().setPrettyPrinting()
                                                                         .create()
                                                                         .toJson(appsList)
                                                                         .getBytes())) {
            addZipFileEntry(inputStream, new ZipEntry("apps.json"), zipOutputStream);
            return appsList;
        } catch (IOException e) {
            LOGGER.error("Exception while adding apps manifest for sandbox download", e);
        }
        return null;
    }

    private List<AppManifestTemplate> parseAppsListJson(List<App> apps) {
        var appManifests = new ArrayList<AppManifestTemplate>();
        for (App app : apps) {
            var appManifestTemplate = new AppManifestTemplate(app.getSoftwareId(), app.getClientName(), app.getClientUri(), app.getLogoUri(), app.getLaunchUri(), app.getFhirVersions(), app.getBriefDescription(), app.getSamplePatients());
            try {
                var clientJsonNode = new ObjectMapper().readTree(app.getClientJSON());
                appManifestTemplate.setAppId(clientJsonNode.get("id").asText());
                appManifestTemplate.setClientId(clientJsonNode.get("clientId").asText());
                var redirectUrisIterator = clientJsonNode.get("redirectUris").elements();
                var redirectUris = new ArrayList<String>();
                while (redirectUrisIterator.hasNext()) {
                    redirectUris.add(redirectUrisIterator.next().asText());
                }
                appManifestTemplate.setRedirectURIs(redirectUris);
                var scopeIterator = clientJsonNode.get("scope").elements();
                appManifestTemplate.setScope(new ArrayList<>());
                while (scopeIterator.hasNext()) {
                    appManifestTemplate.getScope().add(scopeIterator.next().asText());
                }
                appManifestTemplate.setTokenEndpointAuthMethod(clientJsonNode.get("tokenEndpointAuthMethod").asText());
                appManifests.add(appManifestTemplate);
            } catch (JsonProcessingException e) {
                LOGGER.error("Exception while parsing application client json for sandbox download", e);
            }
        }
        return appManifests;
    }

    @NoArgsConstructor
    @Data
    private static class AppManifestTemplate {
        private transient String appId;
        private String softwareId;
        private String clientId;
        private String name;
        private String clientUri;
        private String logo;
        private String launchURI;
        private List<String> redirectURIs;
        private List<String> scope;
        private transient String tokenEndpointAuthMethod;
        private String fhirVersions;
        private String description;
        private List<String> samplePatients;

        public AppManifestTemplate(String softwareId, String name, String clientUri, String logo, String launchURI, String fhirVersions, String description, String samplePatients) {
            this.softwareId = softwareId;
            this.name = name;
            this.clientUri = clientUri;
            this.logo = logo;
            this.launchURI = launchURI;
            this.fhirVersions = fhirVersions;
            this.description = description;
            if (samplePatients != null) {
                var samplePatientsArray = samplePatients.split(",");
                this.samplePatients = new ArrayList<>(samplePatientsArray.length);
                for (String patient : samplePatientsArray) {
                    this.samplePatients.add(stripFhirQuery(patient));
                }
            }
        }

        private String stripFhirQuery(String patient) {
            return patient.substring(patient.contains(PATIENT_FHIR_QUERY) ? patient.indexOf(PATIENT_FHIR_QUERY) + PATIENT_FHIR_QUERY.length() : 0);
        }

        public String getLogoFileName() {
            return this.logo.substring(this.logo.lastIndexOf("/") + 1);
        }

        public InputStream getLogoInputStream() {
            try {
                return new URL(this.logo).openStream();
            } catch (IOException e) {
                LOGGER.error("Exception while accessing app logo image for sandbox download", e);
            }
            return null;
        }
    }

    private void addAppImagesToZipFile(List<AppManifestTemplate> appsList, ZipOutputStream zipOutputStream) {
        var fileToInputStreamMapping = appsList.stream()
                                               .collect(Collectors.toMap(AppManifestTemplate::getLogoFileName, AppManifestTemplate::getLogoInputStream));
        fileToInputStreamMapping.forEach((fileName, inputStream) -> addZipFileEntry(inputStream, new ZipEntry(IMAGE_FOLDER + fileName), zipOutputStream));
        appsList.forEach(app -> app.setLogo(IMAGE_FOLDER + app.getLogoFileName()));
    }

    private void addUserPersonasToZipFile(String sandboxId, String sbmUserId, ZipOutputStream zipOutputStream) {
        var userPersonas = userPersonaService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, sbmUserId, Visibility.PUBLIC);
        var sandboxUserPersona = userPersonas.stream()
                                             .map(SandboxUserPersona::new)
                                             .collect(Collectors.toList());
        try (var inputStream = new ByteArrayInputStream(new GsonBuilder().setPrettyPrinting()
                                                                         .create()
                                                                         .toJson(sandboxUserPersona)
                                                                         .getBytes())) {
            addZipFileEntry(inputStream, new ZipEntry("personas.json"), zipOutputStream);
        } catch (IOException e) {
            LOGGER.error("Exception while adding personas for sandbox download", e);
        }
    }

    @Getter
    private static class SandboxUserPersona {
        private final String personaUserId;
        private final String password;
        private final String resourceUrl;

        public SandboxUserPersona(UserPersona userPersona) {
            this.personaUserId = userPersona.getPersonaUserId();
            this.password = userPersona.getPassword();
            this.resourceUrl = userPersona.getResourceUrl();
        }
    }

    private void addCdsHooksToZipFile(String sandboxId, String sbmUserId, ZipOutputStream zipOutputStream) {
        var cdsServiceEndpoints = cdsServiceEndpointService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, sbmUserId, Visibility.PUBLIC);
        for (CdsServiceEndpoint cdsServiceEndpoint : cdsServiceEndpoints) {
            List<CdsHook> cdsHooks = cdsHookService.findByCdsServiceEndpointId(cdsServiceEndpoint.getId());
            cdsServiceEndpoint.setCdsHooks(cdsHooks);
        }
        var sandboxCdsServiceEndpoints = cdsServiceEndpoints.stream()
                                                            .map(SandboxCdsServiceEndpoint::new)
                                                            .collect(Collectors.toList());
        try (var inputStream = new ByteArrayInputStream(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                                                                          .writerWithDefaultPrettyPrinter()
                                                                          .writeValueAsBytes(sandboxCdsServiceEndpoints))) {
            addZipFileEntry(inputStream, new ZipEntry("cds-hooks.json"), zipOutputStream);
        } catch (IOException e) {
            LOGGER.error("Exception while adding cds hooks for sandbox download", e);
        }
    }

    @Getter
    private static class SandboxCdsServiceEndpoint {
        private final String url;
        private final String title;
        private final String description;
        private final List<SandboxCdsHook> cdsHooks;

        public SandboxCdsServiceEndpoint(CdsServiceEndpoint cdsServiceEndpoint) {
            this.url = cdsServiceEndpoint.getUrl();
            this.title = cdsServiceEndpoint.getTitle();
            this.description = cdsServiceEndpoint.getDescription();
            this.cdsHooks = cdsServiceEndpoint.getCdsHooks()
                                              .stream()
                                              .map(SandboxCdsHook::new)
                                              .collect(Collectors.toList());
        }
    }

    @Getter
    private static class SandboxCdsHook {
        private final String logoUri;
        private final String hook;
        private final String title;
        private final String description;
        private final String hookId;
        private final JsonNode prefetch;
        private final String hookUrl;
        private final String scope;
        private final JsonNode context;

        public SandboxCdsHook(CdsHook cdsHook) {
            this.logoUri = cdsHook.getLogoUri();
            this.hook = cdsHook.getHook();
            this.title = cdsHook.getTitle();
            this.description = cdsHook.getDescription();
            this.hookId = cdsHook.getHookId();
            this.prefetch = cdsHook.getPrefetch();
            this.hookUrl = cdsHook.getHookUrl();
            this.scope = cdsHook.getScope();
            this.context = cdsHook.getContext();
        }
    }

    private void addLaunchScenariosToZipFile(String sandboxId, String sbmUserId, ZipOutputStream zipOutputStream, List<AppManifestTemplate> appsManifests) {
        var launchScenarios = launchScenarioService.findBySandboxIdAndCreatedByOrVisibility(sandboxId, sbmUserId, Visibility.PUBLIC);
        var appIdToClientIdMapper = appsManifests.stream()
                                                 .collect(Collectors.toMap(AppManifestTemplate::getAppId, AppManifestTemplate::getClientId));
        var sandboxLaunchScenarios = launchScenarios.stream()
                                                    .map(SandboxLaunchScenario::new)
                                                    .peek(sandboxLaunchScenario -> sandboxLaunchScenario.setClientId(appIdToClientIdMapper.get(sandboxLaunchScenario.getAppId())))
                                                    .collect(Collectors.toList());
        try (var inputStream = new ByteArrayInputStream(new GsonBuilder().setPrettyPrinting()
                                                                         .create()
                                                                         .toJson(sandboxLaunchScenarios)
                                                                         .getBytes())) {
            addZipFileEntry(inputStream, new ZipEntry("launch-scenarios.json"), zipOutputStream);
        } catch (IOException e) {
            LOGGER.error("Exception while adding launch scenarios for sandbox download", e);
        }
    }

    @Data
    private static class SandboxLaunchScenario {
        private final String description;
        private final String personaUserId;
        private final transient String appId;
        private String clientId;
        private final Map<String, String> contextParams;
        private final String patient;
        private final String encounter;
        private final String location;
        private final String resource;
        private final String intent;
        private final String smartStyleUrl;
        private final String title;
        private final boolean needPatientBanner;
        private final String cdsHookUrl;
        private final JsonNode context;

        public SandboxLaunchScenario(LaunchScenario launchScenario) {
            this.description = launchScenario.getDescription();
            this.personaUserId = launchScenario.getUserPersona() == null ? null : launchScenario.getUserPersona().getPersonaUserId();
            this.appId = launchScenario.getApp() == null ? null : extractAppId(launchScenario.getApp().getClientJSON());
            this.contextParams = launchScenario.getContextParams()
                                               .stream()
                                               .collect(Collectors.toMap(ContextParams::getName, ContextParams::getValue));
            this.patient = launchScenario.getPatient();
            this.encounter = launchScenario.getEncounter();
            this.location = launchScenario.getLocation();
            this.resource = launchScenario.getResource();
            this.intent = launchScenario.getIntent();
            this.smartStyleUrl = launchScenario.getSmartStyleUrl();
            this.title = launchScenario.getTitle();
            this.needPatientBanner = "1".equals(launchScenario.getNeedPatientBanner());
            this.cdsHookUrl = launchScenario.getCdsHook() == null ? null : launchScenario.getCdsHook().getHookUrl();
            this.context = launchScenario.getContext();
        }

        public String extractAppId(String clientJson) {
            var appId = new GsonBuilder().setPrettyPrinting()
                                         .create()
                                         .fromJson(clientJson, AppId.class);
            return appId.getId();
        }

    }

    @Data
    @AllArgsConstructor
    private static class AppId {
        private String id;
    }

    public void addProfilesToZipFile(String sandboxId, ZipOutputStream zipOutputStream) {
        var profileDetails = fhirProfileDetailService.getAllProfilesForAGivenSandbox(sandboxId);
        var profiles = profileDetails.stream()
                                     .map(SandboxFhirProfileDetail::new)
                                     .peek(sandboxFhirProfileDetail -> {
                                         var sandboxFhirProfiles = fhirProfileService.getAllResourcesForGivenProfileId(sandboxFhirProfileDetail.getId());
                                         sandboxFhirProfileDetail.setSandboxFhirProfiles(sandboxFhirProfiles);
                                     })
                                     .collect(Collectors.toList());
        try (var inputStream = new ByteArrayInputStream(new GsonBuilder().setPrettyPrinting()
                                                                         .create()
                                                                         .toJson(profiles)
                                                                         .getBytes())) {
            addZipFileEntry(inputStream, new ZipEntry("profiles.json"), zipOutputStream);
        } catch (IOException e) {
            LOGGER.error("Exception while adding profiles for sandbox download", e);
        }
    }

    @Getter
    private static class SandboxFhirProfileDetail {
        private final transient Integer id;
        private final String profileName;
        private final String profileId;
        private List<SandboxFhirProfile> fhirProfiles;

        public SandboxFhirProfileDetail(FhirProfileDetail fhirProfileDetail) {
            this.id = fhirProfileDetail.getId();
            this.profileName = fhirProfileDetail.getProfileName();
            this.profileId = fhirProfileDetail.getProfileId();
        }

        public void setSandboxFhirProfiles(List<FhirProfile> fhirProfiles) {
            this.fhirProfiles = fhirProfiles.stream()
                                            .map(SandboxFhirProfile::new)
                                            .collect(Collectors.toList());
        }
    }

    @Getter
    private static class SandboxFhirProfile {
        private final String fullUrl;
        private final String relativeUrl;
        private final String profileType;

        public SandboxFhirProfile(FhirProfile fhirProfile) {
            this.fullUrl = fhirProfile.getFullUrl();
            this.relativeUrl = fhirProfile.getRelativeUrl();
            this.profileType = fhirProfile.getProfileType();
        }
    }

}
