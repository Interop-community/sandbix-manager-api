package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.hspconsortium.sandboxmanagerapi.model.Role;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.model.UserRole;
import org.hspconsortium.sandboxmanagerapi.services.AnalyticsService;
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
import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Value("${spring.datasource.base_url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUserName;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    private final UserService userService;
    private final SandboxService sandboxService;

    @Inject
    AnalyticsServiceImpl(final UserService userService, final SandboxService sandboxService) {
        this.userService = userService;
        this.sandboxService = sandboxService;
    }

    public Integer countSandboxesByUser(String userId) {
        User user = userService.findBySbmUserId(userId);
        return 1;
    }

    public List<Sandbox> sandboxesCreatedByUser(User user) {
        List<Sandbox> userSandboxes = sandboxService.getAllowedSandboxes(user);
        List<Sandbox> userCreatedSandboxes = new ArrayList<>();
        for (Sandbox sandbox: userSandboxes) {
            for (UserRole userRole: sandbox.getUserRoles()) {
                if (userRole.getUser().getSbmUserId().equals(user.getSbmUserId())) {
                    if (userRole.getRole().equals(Role.CREATE_SANDBOX)) {
                        userCreatedSandboxes.add(sandbox);
                    }
                }
            }
        }
        return userCreatedSandboxes;
    }

    public Double retrieveMemoryInSchema(String schemaName) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(databaseUrl, databaseUserName, databasePassword);
            Statement stmt = conn.createStatement() ;
            String query = "SELECT table_name AS 'Table', ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'\n" +
                    "FROM information_schema.TABLES WHERE table_schema REGEXP 'hspc_[0-9]_" + schemaName + "';" ;
            ResultSet rs = stmt.executeQuery(query);
            Double count = 0.0;
            while (rs.next()) {
                count += Double.parseDouble(rs.getString(2));
            }
            conn.close();
            return count;

        } catch (Exception e) {
            throw new RuntimeException("Error getting memory information for " + schemaName, e);
        }
    }
}
