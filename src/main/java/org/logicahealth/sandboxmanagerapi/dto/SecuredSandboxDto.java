package org.logicahealth.sandboxmanagerapi.dto;

public class SecuredSandboxDto {
    private String sandboxId;
    private String name;

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
