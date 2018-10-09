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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Value("${spring.datasource.base_url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUserName;

    @Value("${spring.datasource.password}")
    private String databasePassword;

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
        List<String> sandboxesInRAPI;
        Iterable<Sandbox> sandboxesIterable = sandboxService.findAll();
        HashMap<String, Object> returnedDict = new HashMap<>();
        List<String> missingInSandboxManager = new ArrayList<>();
        List<String> missingInReferenceApi = new ArrayList<>();
        for (Sandbox sandbox: sandboxesIterable) {
            sandboxesInSM.add(sandbox.getSandboxId());
        }

        Sandbox sandboxExample = new Sandbox();
        sandboxExample.setApiEndpointIndex("5");
        sandboxExample.setSandboxId("sandbox");

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer " + request);
        HttpEntity<String> httpEntity = new HttpEntity(requestHeaders);

        try {
            sandboxesInRAPI = simpleRestTemplate.exchange(sandboxService.getSandboxApiURL(sandboxExample)  + "/sandbox/all", HttpMethod.GET, httpEntity, List.class).getBody();
            for (String sandbox: sandboxesInRAPI) {
                if (!sandboxesInSM.contains(sandbox)) {
                    missingInSandboxManager.add(sandbox);
                }
            }
            for (String sandbox: sandboxesInSM) {
                if (!sandboxesInRAPI.contains(sandbox)) {
                    missingInReferenceApi.add(sandbox);
                }
            }
            returnedDict.put("missing_in_sandbox_manager", missingInSandboxManager);
            returnedDict.put("missing_in_reference_api", missingInReferenceApi);
            if (fix) {
                for (String sandboxString: missingInReferenceApi) {
                    sandboxService.delete(sandboxService.findBySandboxId(sandboxString.substring(7)), request, null, true);
                }
                for (String sandboxString: missingInSandboxManager) {
                    Sandbox sandbox = new Sandbox();
                    sandbox.setSandboxId(sandboxString.substring(7));
                    sandbox.setApiEndpointIndex(String.valueOf(sandboxString.charAt(5)));
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
        try {
            simpleRestTemplate.exchange(sandboxService.getSandboxApiURL(sandbox)  + "/sandbox?sync=true", HttpMethod.DELETE, httpEntity, String.class);
        } catch (Exception e) {
            try {
                sandbox.setApiEndpointIndex("6");
                simpleRestTemplate.exchange(sandboxService.getSandboxApiURL(sandbox)  + "/sandbox?sync=true", HttpMethod.DELETE, httpEntity, String.class);
            } catch (Exception e2) {
                try {
                    sandbox.setApiEndpointIndex("7");
                    simpleRestTemplate.exchange(sandboxService.getSandboxApiURL(sandbox)  + "/sandbox?sync=true", HttpMethod.DELETE, httpEntity, String.class);
                } catch (Exception e3) {
                    throw new RuntimeException("Error deleting sandbox " + sandbox.getSandboxId(), e3);
                }
            }
        }
    }
}
