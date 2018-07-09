package org.hspconsortium.sandboxmanagerapi.model;

public class Rule {

    private Integer sandboxes;
    private Integer apps;
    private Integer users;
    private Integer transactions;
    private Integer storage;

    public Rule(Integer sandboxes, Integer apps, Integer users, Integer transactions, Integer storage) {
        this.sandboxes = sandboxes;
        this.apps = apps;
        this.users = users;
        this.transactions = transactions;
        this.storage = storage;
    }

    public Rule() {
    }

    public Integer getSandboxes() {
        return sandboxes;
    }

    public void setSandboxes(Integer sandboxes) {
        this.sandboxes = sandboxes;
    }

    public Integer getApps() {
        return apps;
    }

    public void setApps(Integer apps) {
        this.apps = apps;
    }

    public Integer getUsers() {
        return users;
    }

    public void setUsers(Integer users) {
        this.users = users;
    }

    public Integer getTransactions() {
        return transactions;
    }

    public void setTransactions(Integer transactions) {
        this.transactions = transactions;
    }

    public Integer getStorage() {
        return storage;
    }

    public void setStorage(Integer storage) {
        this.storage = storage;
    }
}
