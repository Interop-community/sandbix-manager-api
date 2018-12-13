package org.hspconsortium.sandboxmanagerapi.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        // Used to retrieve statistics for last 12 months
        @NamedQuery(name="Statistics.get12MonthStatistics",
                query="SELECT c FROM Statistics c WHERE c.createdTimestamp BETWEEN :yearAgoTimestamp AND :currentTimestamp"),
        @NamedQuery(name="Statistics.getFhirTransaction",
                query="SELECT COUNT(*) FROM FhirTransaction c WHERE c.transactionTimestamp BETWEEN :fromTimestamp AND :toTimestamp")
})
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Timestamp createdTimestamp;
    private String fullSandboxCount;
    @Column(name = "full_dstu2_count")
    private String fullDstu2Count;
    @Column(name = "full_stu3_count")
    private String fullStu3Count;
    @Column(name = "full_r4_count")
    private String fullR4Count;
    private String activeSandboxesInInterval;
    private String newSandboxesInInterval;
    @Column(name = "dstu2_sandboxes_in_interval")
    private String dstu2SandboxesInInterval;
    @Column(name = "stu3_sandboxes_in_interval")
    private String stu3SandboxesInInterval;
    @Column(name = "r4_sandboxes_in_interval")
    private String r4SandboxesInInterval;
    private String fullUserCount;
    private String activeUserInInterval;
    private String newUsersInInterval;
    private String fhirTransactions;

    public int getId() { return id; }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getFullSandboxCount() {
        return fullSandboxCount;
    }

    public void setFullSandboxCount(String fullSandboxCount) {
        this.fullSandboxCount = fullSandboxCount;
    }

    public String getFullDstu2Count() {
        return fullDstu2Count;
    }

    public void setFullDstu2Count(String fullDstu2Count) {
        this.fullDstu2Count = fullDstu2Count;
    }

    public String getFullStu3Count() {
        return fullStu3Count;
    }

    public void setFullStu3Count(String fullStu3Count) {
        this.fullStu3Count = fullStu3Count;
    }

    public String getFullR4Count() {
        return fullR4Count;
    }

    public void setFullR4Count(String fullR4Count) {
        this.fullR4Count = fullR4Count;
    }

    public String getActiveSandboxesInInterval() {
        return activeSandboxesInInterval;
    }

    public void setActiveSandboxesInInterval(String activeSandboxesInInterval) {this.activeSandboxesInInterval = activeSandboxesInInterval; }

    public String getNewSandboxesInInterval() {
        return newSandboxesInInterval;
    }

    public void setNewSandboxesInInterval(String newSandboxesInInterval) {this.newSandboxesInInterval = newSandboxesInInterval; }

    public String getDstu2SandboxesInInterval() {
        return dstu2SandboxesInInterval;
    }

    public void setDstu2SandboxesInInterval(String dstu2SandboxesInInterval) {this.dstu2SandboxesInInterval = dstu2SandboxesInInterval; }

    public String getStu3SandboxesInInterval() {
        return stu3SandboxesInInterval;
    }

    public void setStu3SandboxesInInterval(String stu3SandboxesInInterval) {this.stu3SandboxesInInterval = stu3SandboxesInInterval; }

    public String getR4SandboxesInInterval() {
        return r4SandboxesInInterval;
    }

    public void setR4SandboxesInInterval(String r4SandboxesInInterval) {this.r4SandboxesInInterval = r4SandboxesInInterval; }

    public String getFullUserCount() {
        return fullUserCount;
    }

    public void setFullUserCount(String fullUserCount) {
        this.fullUserCount = fullUserCount;
    }

    public String getActiveUserInInterval() {
        return activeUserInInterval;
    }

    public void setActiveUserInInterval(String activeUserInInterval) { this.activeUserInInterval = activeUserInInterval; }

    public String getNewUsersInInterval() {
        return newUsersInInterval;
    }

    public void setNewUsersInInterval(String newUsersInInterval) {
        this.newUsersInInterval = newUsersInInterval;
    }

    public String getFhirTransactions() { return fhirTransactions; }

    public void setFhirTransactions(String fhirTransactions) { this.fhirTransactions = fhirTransactions; }


}
