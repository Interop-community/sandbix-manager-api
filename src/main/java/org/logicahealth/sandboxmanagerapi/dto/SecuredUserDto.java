package org.logicahealth.sandboxmanagerapi.dto;

public class SecuredUserDto {
    private String email;
    private String sbmUserId;
    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSbmUserId() {
        return sbmUserId;
    }

    public void setSbmUserId(String sbmUserId) {
        this.sbmUserId = sbmUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
