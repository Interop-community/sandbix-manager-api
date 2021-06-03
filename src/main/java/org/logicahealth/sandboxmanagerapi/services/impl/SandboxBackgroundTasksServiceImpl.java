package org.logicahealth.sandboxmanagerapi.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.logicahealth.sandboxmanagerapi.model.*;
import org.logicahealth.sandboxmanagerapi.repositories.SandboxRepository;
import org.logicahealth.sandboxmanagerapi.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@AllArgsConstructor
public class SandboxBackgroundTasksServiceImpl implements SandboxBackgroundTasksService {

    private final CloseableHttpClient httpClient;
    private final SandboxRepository repository;
    private final UserAccessHistoryService userAccessHistoryService;
    private final SandboxExportService sandboxExportService;
    private final SandboxInviteService sandboxInviteService;
    private final AppService appService;
    private final UserService userService;
    private final UserPersonaService userPersonaService;
    private final CdsServiceEndpointService cdsServiceEndpointService;
    private final CdsHookService cdsHookService;
    private final LaunchScenarioService launchScenarioService;
    private final FhirProfileDetailService fhirProfileDetailService;
    private static Logger LOGGER = LoggerFactory.getLogger(SandboxBackgroundTasksServiceImpl.class.getName());

    private static final String SANDBOX_IMPORT_IMAGE_PREFIX = "img/";

    @Override
    @Transactional
    @Async("sandboxSingleThreadedTaskExecutor")
    public void cloneSandboxSchema(final Sandbox newSandbox, final Sandbox clonedSandbox, final User user, final String bearerToken, final String sandboxApiURL) throws UnsupportedEncodingException {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        String url = sandboxApiURL + "/sandbox/clone";

        // TODO: change to using 'simpleRestTemplate'
        HttpPut cloneRequest = new HttpPut(url);
        cloneRequest.addHeader("Content-Type", "application/json");
        StringEntity entity;

        String jsonString = "{\"newSandbox\": {" +
                "\"teamId\": \"" + newSandbox.getSandboxId() +
                "\",\"allowOpenAccess\": \"" + newSandbox.isAllowOpenAccess() + "\"" +
                "}," +
                "\"clonedSandbox\": {" +
                "\"teamId\": \"" + clonedSandbox.getSandboxId() +
                "\",\"allowOpenAccess\": \"" + clonedSandbox.isAllowOpenAccess() + "\"" +
                "}" +
                "}";
        entity = new StringEntity(jsonString);
        cloneRequest.setEntity(entity);
        cloneRequest.setHeader("Authorization", "BEARER " + bearerToken);

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(cloneRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                org.apache.http.HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                String errorMsg = String.format("There was a problem cloning the sandbox.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                updateSandboxCreationStatus(newSandbox, SandboxCreationStatus.ERRORED);
                throw new RuntimeException(errorMsg);
            }
            this.userAccessHistoryService.saveUserAccessInstance(newSandbox, user);
            updateSandboxCreationStatus(newSandbox, SandboxCreationStatus.CREATED);
        } catch (IOException e) {
            updateSandboxCreationStatus(newSandbox, SandboxCreationStatus.ERRORED);
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        }
    }

    @Async("sandboxSingleThreadedTaskExecutor")
    @Override
    public void exportSandbox(Sandbox sandbox, User user, String bearerToken, String apiUrl) {
        try (final var pipedOutputStream = new PipedOutputStream();
             final var pipedInputStream = new PipedInputStream(pipedOutputStream)) {
            var sandboxExportFileName = UUID.randomUUID() + ".zip";
            final var zipFileCreationRunner = new Thread(sandboxExportService.createZippedSandboxExport(sandbox, user.getSbmUserId(), bearerToken, apiUrl, pipedOutputStream));
            final var s3BucketOutfileRunner = new Thread(sandboxExportService.sendToS3Bucket(pipedInputStream, sandboxExportFileName, user, sandbox.getName()));
            zipFileCreationRunner.start();
            s3BucketOutfileRunner.start();
            zipFileCreationRunner.join();
            s3BucketOutfileRunner.join();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Exception while exporting sandbox to s3 bucket", e);
            throw new RuntimeException("Exception while exporting sandbox to s3 bucket");
        }
    }

    @Override
    @Async("sandboxSingleThreadedTaskExecutor")
    @Transactional
    public void importSandbox(ZipInputStream zipInputStream, Sandbox newSandbox, Map sandboxVersions, User requestingUser, String sandboxApiURL, String bearerToken) {
        newSandbox = repository.findBySandboxId(newSandbox.getSandboxId());
        requestingUser = userService.findBySbmUserId(requestingUser.getSbmUserId());
        try {
            ZipEntry zipEntry;
            Gson gson = new Gson();
            String zipEntryName;
            Map<String, App> clientIdToApp = null;
            Map<String, UserPersona> personaIdToPersona = null;
            Map<String, CdsHook> cdsHookUrlToCdsHook = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                zipEntryName = zipEntry.getName();
                System.out.println("Zip Entry: " + zipEntryName);
                var appImages = new HashMap<String, Image>();
                switch (zipEntryName) {
                    case "sandbox.sql":
                        importSandboxDatabaseSchema(zipInputStream, newSandbox, sandboxApiURL, bearerToken, (String) sandboxVersions.get("hapiVersion"));
                        break;
                    case "users.json":
                    case "signature":
                        break;
                    case "users.csv":
                        importSandboxUsers(zipInputStream, gson, requestingUser, newSandbox);
                        break;
                    case "apps.json":
                        clientIdToApp = importSandboxApps(zipInputStream, gson, appImages, newSandbox, requestingUser);
                        break;
                    case "personas.json":
                        personaIdToPersona = importUserPersonas(zipInputStream, gson, newSandbox, requestingUser);
                        break;
                    case "cds-hooks.json":
                        cdsHookUrlToCdsHook = importCdsHooks(zipInputStream, gson, newSandbox);
                        break;
                    case "launch-scenarios.json":
                        importLaunchScenarios(zipInputStream, gson, newSandbox, requestingUser, clientIdToApp, personaIdToPersona, cdsHookUrlToCdsHook);
                        break;
                    case "profiles.json":
                        importProfiles(zipInputStream, gson);
                        break;
                    default:
                        if (zipEntryName.startsWith(SANDBOX_IMPORT_IMAGE_PREFIX)) {
                            importAppImages(zipInputStream, zipEntryName, appImages);
                        }
                }
                updateSandboxCreationStatus(newSandbox, SandboxCreationStatus.CREATED);
            }
            zipInputStream.close();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to import zip file", e);
        }
    }

    private void importSandboxDatabaseSchema(ZipInputStream zipInputStream, Sandbox newSandbox, String sandboxApiURL, String bearerToken, String hapiVersion) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(HttpHeaders.AUTHORIZATION, "BEARER " + bearerToken);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        var fileName = newSandbox.getSandboxId() + UUID.randomUUID() + ".sql";
        try {
            var file = new File(fileName);
            var fileOutputStream = new FileOutputStream(file);
            IOUtils.copyLarge(zipInputStream, fileOutputStream);
            body.add("schema", new FileSystemResource(file));
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(sandboxApiURL + "/sandbox/import/" + newSandbox.getSandboxId() + "/" + hapiVersion, requestEntity, String.class);
            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create schema for sandbox " + newSandbox.getSandboxId());
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create schema for sandbox " + newSandbox.getSandboxId(), e);
        }
    }

    private void importSandboxUsers(ZipInputStream zipInputStream, Gson gson, User requestingUser, Sandbox newSandbox) {
        var bufferedReader = new BufferedReader(new InputStreamReader(zipInputStream));
        var inviteeEmails = bufferedReader.lines()
                                          .map(string -> string.split(","))
                                          .flatMap(Arrays::stream)
                                          .collect(Collectors.toList());
        inviteeEmails.forEach(inviteeEmail -> inviteUser(inviteeEmail, requestingUser, newSandbox));
    }

    private void inviteUser(String inviteeEmail, User invitedBy, Sandbox newSandbox) {
        var sandboxInvite = new SandboxInvite();
        var invitee = new User();
        invitee.setEmail(inviteeEmail);
        sandboxInvite.setInvitee(invitee);
        sandboxInvite.setInvitedBy(invitedBy);
        sandboxInvite.setSandbox(newSandbox);
        try {
            sandboxInviteService.create(sandboxInvite);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to invite users while importing sandbox", e);
        }
    }

    private Map<String, App> importSandboxApps(ZipInputStream zipInputStream, Gson gson, Map<String, Image> appImages, Sandbox newSandbox, User requestingUser) {
        SandboxExportServiceImpl.AppManifestTemplate[] sandboxApps = gson.fromJson(new JsonReader(new InputStreamReader(zipInputStream)), SandboxExportServiceImpl.AppManifestTemplate[].class);
        Map<String, App> clientIdToApp = new HashMap<>(sandboxApps.length);
        Arrays.stream(sandboxApps).forEach(app -> {
            var importedApp = new App();
            importedApp.setSandbox(newSandbox);
            importedApp.setVisibility(Visibility.PRIVATE);
            importedApp.setCreatedBy(requestingUser);
            importedApp.setClientId(app.getClientId());
            var savedClientId = app.getClientId();
            app.setClientId(null);
            importedApp.setClientJSON(new Gson().toJson(app));
            importedApp.setLaunchUri(app.getLaunchUri());
            importedApp.setSoftwareId(app.getSoftwareId());
            importedApp.setFhirVersions(app.getFhirVersions());
            importedApp.setLogoUri(null);
            importedApp.setLogo(appImages.get(app.getLogo()));
            importedApp.setSamplePatients(app.getSamplePatients() == null ? null : String.join(",", app.getSamplePatients()));
            importedApp.setClientName(app.getClientName());
            importedApp.setClientUri(app.getClientUri());
            importedApp.setManifestUrl(null);
            importedApp.setCopyType(CopyType.REPLICA);
            importedApp.setCustomApp(false);
            importedApp = appService.create(importedApp, newSandbox);
            clientIdToApp.put(savedClientId, importedApp);
        });
        return clientIdToApp;
    }

    private Map<String, UserPersona> importUserPersonas(ZipInputStream zipInputStream, Gson gson, Sandbox newSandbox, User requestingUser) {
        SandboxExportServiceImpl.SandboxUserPersona[] userPersonas = gson.fromJson(new JsonReader(new InputStreamReader(zipInputStream)), SandboxExportServiceImpl.SandboxUserPersona[].class);
        Map<String, UserPersona> personaIdToPersona = new HashMap<>(userPersonas.length);
        for (var userPersona : userPersonas) {
            UserPersona newUserPersona = new UserPersona();
            newUserPersona.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            var resourceUrlComponents = userPersona.getResourceUrl().split("/");
            newUserPersona.setFhirId(resourceUrlComponents.length > 1 ? resourceUrlComponents[1] : "");
            String[] personaSplit = userPersona.getPersonaUserId()
                                               .split("@");
            newUserPersona.setFhirName(personaSplit[0]);
            newUserPersona.setPassword(userPersona.getPassword());
            newUserPersona.setPersonaName(personaSplit[0]);
            newUserPersona.setPersonaUserId(personaSplit[0] + "@" + newSandbox.getSandboxId());
            newUserPersona.setResource(resourceUrlComponents[0]);
            newUserPersona.setResourceUrl(userPersona.getResourceUrl());
            newUserPersona.setVisibility(Visibility.PUBLIC);
            newUserPersona.setCreatedBy(requestingUser);
            newUserPersona.setSandbox(newSandbox);
            newUserPersona = userPersonaService.save(newUserPersona);
            personaIdToPersona.put(newUserPersona.getPersonaUserId(), newUserPersona);
        }
        return personaIdToPersona;
    }

    private Map<String, CdsHook> importCdsHooks(ZipInputStream zipInputStream, Gson gson, Sandbox newSandbox) {
        Map[] sandboxCdsHooks = gson.fromJson(new JsonReader(new InputStreamReader(zipInputStream)), Map[].class);
        Map<String, CdsHook> cdsHookUrlToCdsHook = new HashMap<>();
        for (Map sandboxCdsHook : sandboxCdsHooks) {
            var cdsHooks = new ArrayList<CdsHook>(((List) sandboxCdsHook.get("cdsHooks")).size());
            var cdsHookServiceEndpoint = new CdsServiceEndpoint();
            cdsHookServiceEndpoint.setCdsHooks(cdsHooks);
            var hooks = (List<Map>) sandboxCdsHook.get("cdsHooks");
            for (Map hook : hooks) {
                var cdsHook = new CdsHook();
                cdsHook.setHook((String) hook.get("hook"));
                cdsHook.setTitle((String) hook.get("title"));
                cdsHook.setDescription((String) hook.get("description"));
                cdsHook.setHookId((String) hook.get("hookId"));
                var prefetch = (Map<String, String>) hook.get("prefetch");
                if (prefetch != null) {
                    cdsHook.setPrefetch(new ObjectMapper().convertValue(prefetch, JsonNode.class));
                }
                cdsHook.setHookUrl((String) hook.get("hookUrl"));
                cdsHooks.add(cdsHook);
                cdsHookUrlToCdsHook.put(cdsHook.getHookUrl(), cdsHook);
            }
            cdsHookServiceEndpoint.setUrl((String) sandboxCdsHook.get("url"));
            cdsHookServiceEndpoint.setTitle((String) sandboxCdsHook.get("title"));
            cdsHookServiceEndpoint.setDescription((String) sandboxCdsHook.get("description"));
            cdsHookServiceEndpoint.setSandbox(newSandbox);
            cdsServiceEndpointService.create(cdsHookServiceEndpoint, newSandbox);
        }
        return cdsHookUrlToCdsHook;
    }

    private void importLaunchScenarios(ZipInputStream zipInputStream, Gson gson, Sandbox newSandbox, User requestingUser, Map<String, App> clientIdToApp, Map<String, UserPersona> personaIdToPersona, Map<String, CdsHook> cdsHookUrlToCdsHook) {
        SandboxExportServiceImpl.SandboxLaunchScenario[] sandboxLaunchScenarios = gson.fromJson(new JsonReader(new InputStreamReader(zipInputStream)), SandboxExportServiceImpl.SandboxLaunchScenario[].class);
        for (SandboxExportServiceImpl.SandboxLaunchScenario sandboxLaunchScenario : sandboxLaunchScenarios) {
            var launchScenario = new LaunchScenario();
            launchScenario.setCreatedTimestamp(new Timestamp(new Date().getTime()));
            launchScenario.setDescription(sandboxLaunchScenario.getDescription());
            launchScenario.setVisibility(Visibility.PUBLIC);
            launchScenario.setCreatedBy(requestingUser);
            launchScenario.setSandbox(newSandbox);
            String[] personaSplit = sandboxLaunchScenario.getPersonaUserId()
                                                         .split("@");
            var userPersonaId = personaSplit[0] + "@" + newSandbox.getSandboxId();
            launchScenario.setUserPersona(personaIdToPersona.get(userPersonaId));
            launchScenario.setPatient(sandboxLaunchScenario.getPatient());
            launchScenario.setTitle(sandboxLaunchScenario.getTitle());
            launchScenario.setNeedPatientBanner(sandboxLaunchScenario.isNeedPatientBanner() ? "1" : "0");
            launchScenario.setCdsHook(cdsHookUrlToCdsHook.get(sandboxLaunchScenario.getCdsHookUrl()));
            var contextParams = sandboxLaunchScenario.getContextParams();
            if (contextParams != null) {
                launchScenario.setContext(new ObjectMapper().convertValue(contextParams, JsonNode.class));
            }
            launchScenario.setApp(clientIdToApp.get(sandboxLaunchScenario.getClientId()));
            launchScenarioService.create(launchScenario);
        }
    }

    private void importProfiles(ZipInputStream zipInputStream, Gson gson) {
        SandboxExportServiceImpl.SandboxFhirProfileDetail[] sandboxProfiles = gson.fromJson(new JsonReader(new InputStreamReader(zipInputStream)), SandboxExportServiceImpl.SandboxFhirProfileDetail[].class);
        for (SandboxExportServiceImpl.SandboxFhirProfileDetail sandboxFhirProfileDetail : sandboxProfiles) {
            var fhirProfiles = new ArrayList<FhirProfile>(sandboxFhirProfileDetail.getFhirProfiles().size());
            var fhirProfileDetail = new FhirProfileDetail();
            fhirProfileDetail.setFhirProfiles(fhirProfiles);
            fhirProfileDetail.setProfileName(sandboxFhirProfileDetail.getProfileName());
            fhirProfileDetail.setProfileId(sandboxFhirProfileDetail.getProfileId());
            fhirProfileDetail.setStatus(FhirProfileStatus.CREATED);
            fhirProfileDetail.setLastUpdated(new Timestamp(new Date().getTime()));
            for (SandboxExportServiceImpl.SandboxFhirProfile sandboxFhirProfile : sandboxFhirProfileDetail.getFhirProfiles()) {
                var fhirProfile = new FhirProfile();
                fhirProfile.setFullUrl(sandboxFhirProfile.getFullUrl());
                fhirProfile.setRelativeUrl(sandboxFhirProfile.getRelativeUrl());
                fhirProfile.setProfileType(sandboxFhirProfile.getProfileType());
                fhirProfiles.add(fhirProfile);
            }
            fhirProfileDetailService.save(fhirProfileDetail);
        }

    }

    private void importAppImages(ZipInputStream zipInputStream, String zipEntryName, Map<String, Image> appImages) {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(zipInputStream, byteArrayOutputStream);
            var image = new Image();
            image.setBytes(byteArrayOutputStream.toByteArray());
            image.setContentType("image/" + zipEntryName.substring(zipEntryName.lastIndexOf(".") + 1));
            appImages.put(zipEntryName, image);
            byteArrayOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException while copying image zip input stream", e);
        }

    }

    private void updateSandboxCreationStatus(Sandbox newSandbox, SandboxCreationStatus status) {
        newSandbox = repository.findBySandboxId(newSandbox.getSandboxId());
        newSandbox.setCreationStatus(status);
        this.repository.save(newSandbox);
    }

}
