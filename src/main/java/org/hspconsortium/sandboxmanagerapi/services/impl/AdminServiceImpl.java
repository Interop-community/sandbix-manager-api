package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.services.AdminService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxActivityLogService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    @Value("${spring.datasource.base_url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUserName;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${hspc.platform.dontDeleteInSync}")
    private String[] dontDeleteInSync;

    private UserService userService;
    private SandboxService sandboxService;
    private SandboxActivityLogService sandboxActivityLogService;

    private RestTemplate simpleRestTemplate;

    @Inject
    public AdminServiceImpl() { }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Inject
    public void setSandboxActivityLogService(SandboxActivityLogService sandboxActivityLogService) {
        this.sandboxActivityLogService = sandboxActivityLogService;
    }

    @Inject
    public void setRestTemplate(RestTemplate simpleRestTemplate) {
        this.simpleRestTemplate = simpleRestTemplate;
    }

    public HashMap<String, Object> syncSandboxManagerandReferenceApi(Boolean fix, String request) {
        List<String> sandboxesInSM = new ArrayList<>();
        Collection<LinkedHashMap> sandboxesInRAPI;
        Iterable<Sandbox> sandboxesIterable = sandboxService.findAll();
        HashMap<String, Object> returnedDict = new HashMap<>();
        List<Sandbox> missingInSandboxManager = new ArrayList<>();
        List<String> missingInSandboxManagerIds = new ArrayList<>();
        List<String> missingInReferenceApi = new ArrayList<>();
        List<String> sandboxesInRAPINames = new ArrayList<>();
        for (Sandbox sandbox: sandboxesIterable) {
            sandboxesInSM.add(sandbox.getSandboxId());
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer " + request);
        HttpEntity<String> httpEntity = new HttpEntity(requestHeaders);

        try {
            sandboxesInRAPI = simpleRestTemplate.exchange(sandboxService.getSystemSandboxApiURL() + "/sandboxObjects", HttpMethod.GET, httpEntity, Collection.class).getBody();

            for (LinkedHashMap sandboxJSON: sandboxesInRAPI) {
                String sandboxId = sandboxJSON.get("teamId").toString();
                sandboxesInRAPINames.add(sandboxId);

                if (!sandboxesInSM.contains(sandboxId) && !Arrays.stream(dontDeleteInSync).anyMatch(sandboxId::equals)) {
                    Sandbox sandbox = new Sandbox();
                    sandbox.setSandboxId(sandboxId);
                    sandbox.setApiEndpointIndex(sandboxJSON.get("schemaVersion").toString());
                    missingInSandboxManager.add(sandbox);
                    missingInSandboxManagerIds.add(sandbox.getSandboxId());
                }
            }
            for (String sandbox: sandboxesInSM) {
                if (!sandboxesInRAPINames.contains(sandbox)) {
                    missingInReferenceApi.add(sandbox);
                }
            }
            returnedDict.put("missing_in_sandbox_manager", missingInSandboxManagerIds);
            returnedDict.put("missing_in_reference_api", missingInReferenceApi);
            if (fix) {
                for (String sandboxString: missingInReferenceApi) {
                    sandboxService.delete(sandboxService.findBySandboxId(sandboxString), request, null, true);
                }
                for (Sandbox sandbox: missingInSandboxManager) {
                    callDeleteSandboxAPI(sandbox, request);
                }
            }

            return returnedDict;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callDeleteSandboxAPI(Sandbox sandbox, String request) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer " + request);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestHeaders);
        simpleRestTemplate.exchange(sandboxService.getSandboxApiURL(sandbox)  + "/sandbox?sync=true", HttpMethod.DELETE, httpEntity, String.class);
    }
}
