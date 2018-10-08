package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.services.AdminService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxActivityLogService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.hspconsortium.sandboxmanagerapi.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public HashMap<String, Object> syncSandboxManagerandReferenceApi(Boolean fix, String request) {
        List<String> sandboxes = new ArrayList<>();
        List<String> fullSandboxIds = new ArrayList<>();
        Iterable<Sandbox> sandboxesIterable = sandboxService.findAll();
        HashMap<String, Object> returnedDict = new HashMap<>();
        List<String> missingInSandboxManager = new ArrayList<>();
        List<String> missingInReferenceApi = new ArrayList<>();
        for (Sandbox sandbox : sandboxesIterable) {
            if (sandbox.getApiEndpointIndex().equals("5") || sandbox.getApiEndpointIndex().equals("6") || sandbox.getApiEndpointIndex().equals("7")) {
                sandboxes.add("hspc_5_" + sandbox.getSandboxId());
            } else {
                sandboxes.add("hspc_" + sandbox.getApiEndpointIndex() + "_" + sandbox.getSandboxId());
            }
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(databaseUrl, databaseUserName, databasePassword);
            Statement stmt = conn.createStatement() ;
            String query = "select table_schema from information_schema.tables group by 1;" ;
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                if (rs.getString(1).startsWith("hspc_")) {
                    fullSandboxIds.add(rs.getString(1));
                }
            }
            conn.close();
            for (String sandbox: fullSandboxIds) {
                if (!sandboxes.contains(sandbox)) {
                    missingInSandboxManager.add(sandbox);
                }
            }
            for (String sandbox: sandboxes) {
                if (!fullSandboxIds.contains(sandbox)) {
                    missingInReferenceApi.add(sandbox);
                }
            }
            returnedDict.put("missing_in_sandbox_manager", missingInSandboxManager);
            returnedDict.put("missing_in_reference_api", missingInReferenceApi);
            for (String sandbox: missingInReferenceApi) {
                sandboxService.delete(sandboxService.findBySandboxId(sandbox), request);
            }
            return returnedDict;
        } catch (Exception e) {
            throw new RuntimeException("Error getting memory information for median");
        }
    }
}
