package org.hspconsortium.sandboxmanagerapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hspconsortium.sandboxmanagerapi.model.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
