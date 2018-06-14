package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.sandboxmanagerapi.model.*;
import org.hspconsortium.sandboxmanagerapi.repositories.SandboxRepository;
import org.hspconsortium.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Value("${hspc.platform.api.version1.baseUrl:}")
    private String apiBaseURL_1;

    @Value("${hspc.platform.api.version2.baseUrl:}")
    private String apiBaseURL_2;

    @Value("${hspc.platform.api.version3.baseUrl:}")
    private String apiBaseURL_3;

    @Value("${hspc.platform.api.version4.baseUrl:}")
    private String apiBaseURL_4;

    @Value("${hspc.platform.api.version5.baseUrl:}")
    private String apiBaseURL_5;

    @Value("${hspc.platform.api.version6.baseUrl:}")
    private String apiBaseURL_6;

    @Value("${hspc.platform.api.version7.baseUrl:}")
    private String apiBaseURL_7;

    @Value("${hspc.platform.api.oauthUserInfoEndpointURL}")
    private String oauthUserInfoEndpointURL;

    @Value("${expiration-message}")
    private String expirationMessage;

    @Value("${expiration-date}")
    private String expirationDate;

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final UserPersonaService userPersonaService;
    private final UserLaunchService userLaunchService;
    private final AppService appService;
    private final LaunchScenarioService launchScenarioService;
    private final PatientService patientService;
    private final SandboxImportService sandboxImportService;
    private final SandboxActivityLogService sandboxActivityLogService;

    @Inject
    public SandboxServiceImpl(final SandboxRepository repository, final UserService userService,
                              final UserRoleService userRoleService, final AppService appService,
                              final UserPersonaService userPersonaService,
                              final UserLaunchService userLaunchService,
                              final LaunchScenarioService launchScenarioService,
                              final PatientService patientService,
                              final SandboxImportService sandboxImportService,
                              final SandboxActivityLogService sandboxActivityLogService) {
        this.repository = repository;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.userPersonaService = userPersonaService;
        this.userLaunchService = userLaunchService;
        this.appService = appService;
        this.launchScenarioService = launchScenarioService;
        this.patientService = patientService;
        this.sandboxImportService = sandboxImportService;
        this.sandboxActivityLogService = sandboxActivityLogService;
    }

    @Override
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final Sandbox sandbox, final String bearerToken, final User admin) {

        deleteAllSandboxItems(sandbox, bearerToken);

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
        delete(sandbox.getId());

        try {
            callDeleteSandboxAPI(sandbox, bearerToken);
        } catch (Exception ex) {
            throw new SandboxDeleteFailedException(String.format("Failed to delete sandbox: %s %n %s", sandbox.getSandboxId(), ex.getMessage()), ex);
        }
    }

    @Override
    @Transactional
    public void delete(final Sandbox sandbox, final String bearerToken) {
        delete(sandbox, bearerToken, null);
    }

    private void deleteAllSandboxItems(final Sandbox sandbox, final String bearerToken) {

        deleteSandboxItemsExceptApps(sandbox, bearerToken);

        //delete all registered app, authClients, images
        List<App> apps = appService.findBySandboxId(sandbox.getSandboxId());
        for (App app : apps) {
            appService.delete(app);
        }
    }

    private void deleteSandboxItemsExceptApps(final Sandbox sandbox, final String bearerToken) {

        //delete launch scenarios, context params
        List<LaunchScenario> launchScenarios = launchScenarioService.findBySandboxId(sandbox.getSandboxId());
        for (LaunchScenario launchScenario : launchScenarios) {
            launchScenarioService.delete(launchScenario);
        }

        //delete patient/personas for sandbox
        List<Patient> patients = patientService.findBySandboxId(sandbox.getSandboxId());
        for (Patient patient : patients) {
            patientService.delete(patient);
        }


        List<UserPersona> userPersonas = userPersonaService.findBySandboxId(sandbox.getSandboxId());
        for (UserPersona userPersona : userPersonas) {
            userPersonaService.delete(userPersona);
        }


        //remove sample patients from all apps
        List<App> apps = appService.findBySandboxId(sandbox.getSandboxId());
        for (App app : apps) {
            app.setSamplePatients(null);
            appService.save(app);
        }
    }

    @Override
    @Transactional
    public Sandbox create(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException {

        UserPersona userPersona = userPersonaService.findByPersonaUserId(user.getSbmUserId());

        if (userPersona == null && callCreateOrUpdateSandboxAPI(sandbox, bearerToken)) {
            sandbox.setCreatedBy(user);
            sandbox.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            sandbox.setVisibility(Visibility.valueOf(defaultSandboxVisibility));
            // Set expiration date and message for R4 sandboxes
            if(sandbox.getApiEndpointIndex().equals("7")) {
                sandbox.setExpirationMessage(expirationMessage);
                sandbox.setExpirationDate(formatDate());
            }
            Sandbox savedSandbox = save(sandbox);
            addMember(savedSandbox, user, Role.ADMIN);
            addMember(savedSandbox, user, Role.CREATE_SANDBOX);
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
    public Sandbox update(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException  {
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
            launchScenarios.stream().filter(launchScenario -> launchScenario.getVisibility() == Visibility.PRIVATE).forEach(launchScenarioService::delete);

            //delete user launches for public launch scenarios in this sandbox
            List<UserLaunch> userLaunches = userLaunchService.findByUserId(user.getSbmUserId());
            userLaunches.stream().filter(userLaunch -> userLaunch.getLaunchScenario().getSandbox().getSandboxId().equalsIgnoreCase(sandbox.getSandboxId())).forEach(userLaunchService::delete);

            //delete all registered app, authClients, images
            List<App> apps = appService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId());
            apps.stream().filter(app -> app.getVisibility() == Visibility.PRIVATE).forEach(appService::delete);

            List<UserPersona> userPersonas = userPersonaService.findBySandboxIdAndCreatedBy(sandbox.getSandboxId(), user.getSbmUserId());
            userPersonas.stream().filter(userPersona -> userPersona.getVisibility() == Visibility.PRIVATE).forEach(userPersonaService::delete);

            List<UserRole> allUserRoles = sandbox.getUserRoles();
            List<UserRole> currentUserRoles = new ArrayList<>();
            Iterator<UserRole> iterator = allUserRoles.iterator();
            while (iterator.hasNext()) {
                UserRole userRole = iterator.next();
                if (userRole.getUser().getId().equals(user.getId())) {
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
                if (userRole.getUser().getId().equals(user.getId()) &&
                        userRole.getRole().equals(role)) {
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
    public boolean hasMemberRole(final Sandbox sandbox, final User user, final Role role) {
        List<UserRole> userRoles = sandbox.getUserRoles();
        for(UserRole userRole : userRoles) {
            if (userRole.getUser().getSbmUserId().equalsIgnoreCase(user.getSbmUserId()) && userRole.getRole() == role) {
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
        deleteSandboxItemsExceptApps(sandbox, bearerToken);
    }

    @Override
    public boolean isSandboxMember(final Sandbox sandbox, final User user) {
        for(UserRole userRole : sandbox.getUserRoles()) {
            if (userRole.getUser().getSbmUserId().equalsIgnoreCase(user.getSbmUserId())) {
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

        for (Sandbox sandbox : findByVisibility(Visibility.PUBLIC)){
            if (!sandboxes.contains(sandbox)){
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
    public String schemaCount(String apiEndpointIndex) {
        return repository.schemaCount(apiEndpointIndex);
    }

    @Override
    public String intervalCount(Timestamp intervalTime) {
        return repository.intervalCount(intervalTime);
    }

    private void removeAllMembers(final Sandbox sandbox) {

        List<UserRole> userRoles = sandbox.getUserRoles();
        sandbox.setUserRoles(Collections.<UserRole>emptyList());
        save(sandbox);

        for(UserRole userRole : userRoles) {
            userService.removeSandbox(sandbox, userRole.getUser());
            userRoleService.delete(userRole);
        }
    }

    public String getSandboxApiURL(final Sandbox sandbox) {
        return getApiSchemaURL(sandbox.getApiEndpointIndex()) + "/" + sandbox.getSandboxId();
    }

    private String getApiSchemaURL(final String apiEndpointIndex) {
        String url;
        switch (apiEndpointIndex){
            case "1":
                url = apiBaseURL_1;
                break;
            case "2":
                url = apiBaseURL_2;
                break;
            case "3":
                url = apiBaseURL_3;
                break;
            case "4":
                url = apiBaseURL_4;
                break;
            case "5":
                url = apiBaseURL_5;
                break;
            case "6":
                url = apiBaseURL_6;
                break;
            default:
                url = apiBaseURL_7;
        }
        return url;
    }

    private boolean callCreateOrUpdateSandboxAPI(final Sandbox sandbox, final String bearerToken ) throws UnsupportedEncodingException{
        String url = getSandboxApiURL(sandbox) + "/sandbox";
        if (!sandbox.getDataSet().equals(DataSet.NA)){
            url = getSandboxApiURL(sandbox) + "/sandbox?dataSet=" + sandbox.getDataSet();
        }

        HttpPut putRequest = new HttpPut(url);
        putRequest.addHeader("Content-Type", "application/json");
        StringEntity entity;

        String jsonString = "{\"teamId\": \"" + sandbox.getSandboxId() + "\",\"allowOpenAccess\": \"" + sandbox.isAllowOpenAccess() + "\"}";
        entity = new StringEntity(jsonString);
        putRequest.setEntity(entity);
        putRequest.setHeader("Authorization", "BEARER " + bearerToken);

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(putRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
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
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    private boolean callDeleteSandboxAPI(final Sandbox sandbox, final String bearerToken ) {
        String url = getSandboxApiURL(sandbox) + "/sandbox";

        HttpDelete deleteRequest = new HttpDelete(url);
        deleteRequest.addHeader("Authorization", "BEARER " + bearerToken);

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(deleteRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
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
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
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
}


