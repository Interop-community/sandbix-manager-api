package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class UserAccessHistory {

    private Integer id;
    private Timestamp accessTimestamp;
    private String sandboxId;
    private String sbmUserId;

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getAccessTimestamp() {
        return accessTimestamp;
    }

    public void setAccessTimestamp(Timestamp accessTimestamp) {
        this.accessTimestamp = accessTimestamp;
    }

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public String getSbmUserId() {
        return sbmUserId;
    }

    public void setSbmUserId(String sbmUserId) {
        this.sbmUserId = sbmUserId;
    }

}
