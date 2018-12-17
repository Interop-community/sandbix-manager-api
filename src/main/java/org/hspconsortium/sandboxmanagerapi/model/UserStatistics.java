package org.hspconsortium.sandboxmanagerapi.model;

import java.util.HashMap;

public class UserStatistics {
    private Integer sandboxesCount;
    private Integer sandboxesMax;
    private HashMap<String,Integer> applicationsCount;
    private Integer applicationsMax;
    private HashMap<String,Integer> usersCount;
    private Integer usersMax;
    private Integer transactionsCount; // for all sandboxes
    private Integer transactionsMax;
    private Double MemoryCount;
    private Integer MemoryMax;

    public UserStatistics(Rule ruleList){
       this.applicationsMax = ruleList.getApps();
       this.sandboxesMax = ruleList.getSandboxes();
       this.usersMax = ruleList.getUsers();
       this.transactionsMax = ruleList.getTransactions();
       this.MemoryMax = ruleList.getStorage();
    }

    public Integer getSandboxesCount() { return sandboxesCount; }

    public void setSandboxesCount(Integer sandboxesCount) { this.sandboxesCount = sandboxesCount; }

    public Integer getSandboxesMax() { return sandboxesMax; }

    public void setSandboxesMax(Integer sandboxesMax) { this.sandboxesMax = sandboxesMax; }

    public HashMap<String, Integer> getApplicationsCount() { return applicationsCount; }

    public void setApplicationsCount(HashMap<String, Integer> applicationsCount) { this.applicationsCount = applicationsCount; }

    public Integer getApplicationsMax() { return applicationsMax; }

    public void setApplicationsMax(Integer applicationsMax) { this.applicationsMax = applicationsMax; }

    public HashMap<String, Integer> getUsersCount() { return usersCount; }

    public void setUsersCount(HashMap<String, Integer> usersCount) { this.usersCount = usersCount; }

    public Integer getUsersMax() { return usersMax; }

    public void setUsersMax(Integer usersMax) { this.usersMax = usersMax; }

    public Integer getTransactionsCount() { return transactionsCount; }

    public void setTransactionsCount(Integer transactionsCount) { this.transactionsCount = transactionsCount; }

    public Integer getTransactionsMax() { return transactionsMax; }

    public void setTransactionsMax(Integer transactionsMax) { this.transactionsMax = transactionsMax; }

    public Double getMemoryCount() { return MemoryCount; }

    public void setMemoryCount(Double memoryCount) { MemoryCount = memoryCount; }

    public Integer getMemoryMax() { return MemoryMax; }

    public void setMemoryMax(Integer memoryMax) { MemoryMax = memoryMax; }

}
