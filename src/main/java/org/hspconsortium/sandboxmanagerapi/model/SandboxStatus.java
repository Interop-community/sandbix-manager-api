package org.hspconsortium.sandboxmanagerapi.model;

public enum SandboxStatus {
    DEFAULT("default"), IN_PROGRESS("in-progress"), CREATED("created"), NOT_CREATED("not-created"), UPDATED("updated");

    private String value;

    SandboxStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

};