package org.hspconsortium.sandboxmanagerapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "smart_app")
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class SmartApp {

    @Id
    protected String id;

    private String manifestUrl;

    private String manifest;

    private String name;

    private String clientId;

    protected Integer ownerId;

    protected Timestamp createdTimestamp;

    @Enumerated(EnumType.STRING)
    protected Visibility2 visibility;

    private String samplePatients;

    private String info;

    private String briefDescription;

    private String author;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManifestUrl() {
        return manifestUrl;
    }

    public void setManifestUrl(String manifestUrl) {
        this.manifestUrl = manifestUrl;
    }

    public String getManifest() {
        return manifest;
    }

    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Visibility2 getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility2 visibility) {
        this.visibility = visibility;
    }

    public String getSamplePatients() {
        return samplePatients;
    }

    public void setSamplePatients(String samplePatients) {
        this.samplePatients = samplePatients;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
