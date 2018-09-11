//package org.hspconsorotium.sandboxmanagerapi.services;
//
//import org.hspconsortium.sandboxmanagerapi.services.SandboxActivityLogService;
//import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
//import org.hspconsortium.sandboxmanagerapi.services.UserService;
//import org.hspconsortium.sandboxmanagerapi.services.impl.AdminServiceImpl;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import static org.junit.Assert.*;
//
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.when;
//
//public class AdminServiceTest {
//
//    private AdminServiceImpl adminService = new AdminServiceImpl();
//
//    @MockBean
//    private UserService userService;
//
//    @MockBean
//    private SandboxService sandboxService;
//
//    @MockBean
//    private SandboxActivityLogService sandboxActivityLogService;
//
//    private Statistics statistics;
//    @Before
//    public void setup() {
//        statistics = new Statistics();
//        statistics.setFullSandboxCount("1");
//        statistics.setSchema1Sandboxes("1");
//        statistics.setSchema2Sandboxes("1");
//        statistics.setSchema3Sandboxes("1");
//        statistics.setSchema4Sandboxes("1");
//    }
//
//    @Test
//    public void getSandboxStatisticsTest() {
//
//        when(sandboxService.fullCount()).thenReturn("1");
//        when(sandboxService.schemaCount("1")).thenReturn("1");
//        when(sandboxService.schemaCount("2")).thenReturn("1");
//        when(sandboxService.schemaCount("5")).thenReturn("1");
//        when(sandboxService.schemaCount("3")).thenReturn("1");
//        when(sandboxService.schemaCount("4")).thenReturn("1");
//        when(sandboxService.schemaCount("6")).thenReturn("1");
//        when(sandboxService.intervalCount(any())).thenReturn("Timestamp");
//        when(userService.fullCount()).thenReturn("1");
//        when(userService.intervalCount(any())).thenReturn("Timestamp");
//        when(sandboxActivityLogService.intervalActive(any())).thenReturn("Timestamp");
//        String stats = adminService.getSandboxStatistics("10");
//        assertEquals(stats, )
//    }
//
//    private class Statistics {
//        private String fullSandboxCount;
//        private String schema1Sandboxes;
//        private String schema2Sandboxes;
//        private String schema3Sandboxes;
//        private String schema4Sandboxes;
//        private String sandboxesInInterval;
//
//        private String fullUserCount;
//        private String newUsersInInterval;
//        private String activeUserInInterval;
//
//
//        public String getFullSandboxCount() {
//            return fullSandboxCount;
//        }
//
//        void setFullSandboxCount(String fullSandboxCount) {
//            this.fullSandboxCount = fullSandboxCount;
//        }
//
//        public String getSchema1Sandboxes() {
//            return schema1Sandboxes;
//        }
//
//        void setSchema1Sandboxes(String schema1Sandboxes) {
//            this.schema1Sandboxes = schema1Sandboxes;
//        }
//
//        public String getSchema2Sandboxes() {
//            return schema2Sandboxes;
//        }
//
//        void setSchema2Sandboxes(String schema2Sandboxes) {
//            this.schema2Sandboxes = schema2Sandboxes;
//        }
//
//        public String getSchema3Sandboxes() {
//            return schema3Sandboxes;
//        }
//
//        void setSchema3Sandboxes(String schema3Sandboxes) {
//            this.schema3Sandboxes = schema3Sandboxes;
//        }
//
//        public String getSchema4Sandboxes() {
//            return schema4Sandboxes;
//        }
//
//        void setSchema4Sandboxes(String schema4Sandboxes) {
//            this.schema4Sandboxes = schema4Sandboxes;
//        }
//
//        public String getSandboxesInInterval() {
//            return sandboxesInInterval;
//        }
//
//        void setSandboxesInInterval(String sandboxesInInterval) {
//            this.sandboxesInInterval = sandboxesInInterval;
//        }
//
//        public String getFullUserCount() {
//            return fullUserCount;
//        }
//
//        void setFullUserCount(String fullUserCount) {
//            this.fullUserCount = fullUserCount;
//        }
//
//        public String getNewUsersInInterval() {
//            return newUsersInInterval;
//        }
//
//        void setNewUsersInInterval(String newUsersInInterval) {
//            this.newUsersInInterval = newUsersInInterval;
//        }
//
//        public String getActiveUserInInterval() {
//            return activeUserInInterval;
//        }
//
//        void setActiveUserInInterval(String activeUserInInterval) {
//            this.activeUserInInterval = activeUserInInterval;
//        }
//    }
//
//}
