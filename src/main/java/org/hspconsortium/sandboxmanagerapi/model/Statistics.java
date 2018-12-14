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
    private String totalSandboxesCount;
    @Column(name = "total_dstu2_sandboxes_count")
    private String totalDstu2SandboxesCount;
    @Column(name = "total_stu3_sandboxes_count")
    private String totalStu3SandboxesCount;
    @Column(name = "total_r4_sandboxes_count")
    private String totalR4SandboxesCount;
    private String totalUsersCount;
    private String activeSandboxesInInterval;
    @Column(name = "active_dstu2_sandboxes_in_interval")
    private String activeDstu2SandboxesInInterval;
    @Column(name = "active_stu3_sandboxes_in_interval")
    private String activeStu3SandboxesInInterval;
    @Column(name = "active_r4_sandboxes_in_interval")
    private String activeR4SandboxesInInterval;
    private String activeUsersInInterval;
    private String newSandboxesInInterval;
    private String newUsersInInterval;
    @Column(name = "new_dstu2_sandboxes_in_interval")
    private String newDstu2SandboxesInInterval;
    @Column(name = "new_stu3_sandboxes_in_interval")
    private String newStu3SandboxesInInterval;
    @Column(name = "new_r4_sandboxes_in_interval")
    private String newR4SandboxesInInterval;
    private String fhirTransactions;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getTotalSandboxesCount() {
        return totalSandboxesCount;
    }

    public void setTotalSandboxesCount(String totalSandboxesCount) {
        this.totalSandboxesCount = totalSandboxesCount;
    }

    public String getTotalDstu2SandboxesCount() {
        return totalDstu2SandboxesCount;
    }

    public void setTotalDstu2SandboxesCount(String totalDstu2SandboxesCount) {
        this.totalDstu2SandboxesCount = totalDstu2SandboxesCount;
    }

    public String getTotalStu3SandboxesCount() {
        return totalStu3SandboxesCount;
    }

    public void setTotalStu3SandboxesCount(String totalStu3SandboxesCount) {
        this.totalStu3SandboxesCount = totalStu3SandboxesCount;
    }

    public String getTotalR4SandboxesCount() {
        return totalR4SandboxesCount;
    }

    public void setTotalR4SandboxesCount(String totalR4SandboxesCount) {
        this.totalR4SandboxesCount = totalR4SandboxesCount;
    }

    public String getTotalUsersCount() {
        return totalUsersCount;
    }

    public void setTotalUsersCount(String totalUsersCount) {
        this.totalUsersCount = totalUsersCount;
    }

    public String getActiveSandboxesInInterval() {
        return activeSandboxesInInterval;
    }

    public void setActiveSandboxesInInterval(String activeSandboxesInInterval) {
        this.activeSandboxesInInterval = activeSandboxesInInterval;
    }

    public String getActiveDstu2SandboxesInInterval() {
        return activeDstu2SandboxesInInterval;
    }

    public void setActiveDstu2SandboxesInInterval(String activeDstu2SandboxesInInterval) {
        this.activeDstu2SandboxesInInterval = activeDstu2SandboxesInInterval;
    }

    public String getActiveStu3SandboxesInInterval() {
        return activeStu3SandboxesInInterval;
    }

    public void setActiveStu3SandboxesInInterval(String activeStu3SandboxesInInterval) {
        this.activeStu3SandboxesInInterval = activeStu3SandboxesInInterval;
    }

    public String getActiveR4SandboxesInInterval() {
        return activeR4SandboxesInInterval;
    }

    public void setActiveR4SandboxesInInterval(String activeR4SandboxesInInterval) {
        this.activeR4SandboxesInInterval = activeR4SandboxesInInterval;
    }

    public String getActiveUsersInInterval() {
        return activeUsersInInterval;
    }

    public void setActiveUsersInInterval(String activeUsersInInterval) {
        this.activeUsersInInterval = activeUsersInInterval;
    }

    public String getNewSandboxesInInterval() {
        return newSandboxesInInterval;
    }

    public void setNewSandboxesInInterval(String newSandboxesInInterval) {
        this.newSandboxesInInterval = newSandboxesInInterval;
    }

    public String getNewUsersInInterval() {
        return newUsersInInterval;
    }

    public void setNewUsersInInterval(String newUsersInInterval) {
        this.newUsersInInterval = newUsersInInterval;
    }

    public String getNewDstu2SandboxesInInterval() {
        return newDstu2SandboxesInInterval;
    }

    public void setNewDstu2SandboxesInInterval(String newDstu2SandboxesInInterval) {
        this.newDstu2SandboxesInInterval = newDstu2SandboxesInInterval;
    }

    public String getNewStu3SandboxesInInterval() {
        return newStu3SandboxesInInterval;
    }

    public void setNewStu3SandboxesInInterval(String newStu3SandboxesInInterval) {
        this.newStu3SandboxesInInterval = newStu3SandboxesInInterval;
    }

    public String getNewR4SandboxesInInterval() {
        return newR4SandboxesInInterval;
    }

    public void setNewR4SandboxesInInterval(String newR4SandboxesInInterval) {
        this.newR4SandboxesInInterval = newR4SandboxesInInterval;
    }

    public String getFhirTransactions() {
        return fhirTransactions;
    }

    public void setFhirTransactions(String fhirTransactions) {
        this.fhirTransactions = fhirTransactions;
    }

}
