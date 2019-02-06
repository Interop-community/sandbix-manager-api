package org.hspconsortium.sandboxmanagerapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SystemRole;
import org.hspconsortium.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.hspconsortium.sandboxmanagerapi.model.Tier;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDto {
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
