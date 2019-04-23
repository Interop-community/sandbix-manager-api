package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name="FhirProfileDetail.findByFhirProfileId",
                query="SELECT c FROM FhirProfileDetail c WHERE c.id = :fhirProfileId"),
        @NamedQuery(name="FhirProfileDetail.findBySandboxId",
                query="SELECT c FROM FhirProfileDetail c WHERE c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="FhirProfileDetail.findByProfileIdAndSandboxId",
                query="SELECT c FROM FhirProfileDetail c WHERE c.profileId = :profileId AND c.sandbox.sandboxId = :sandboxId"),
})
public class FhirProfileDetail extends AbstractSandboxItem {

    private Timestamp lastUpdated;
    private List<FhirProfile> fhirProfiles;
    private String profileName;
    private String profileId;

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

//    @OneToMany(cascade={CascadeType.ALL})
    @Transient
    public List<FhirProfile> getFhirProfiles() {
        return fhirProfiles;
    }

    public void setFhirProfiles(List<FhirProfile> fhirProfiles) {
        this.fhirProfiles = fhirProfiles;
    }

    /******************* Inherited Property Getter/Setters ************************/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @ManyToOne(cascade={CascadeType.MERGE})
    @JoinColumn(name="created_by_id")
    @JsonIgnoreProperties(ignoreUnknown = true, allowSetters = true,
            value={"sandboxes", "termsOfUseAcceptances", "systemRoles"})
    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    @ManyToOne(cascade={CascadeType.MERGE})
    @JoinColumn(name="sandbox_id")
    @JsonIgnoreProperties(ignoreUnknown = true, allowSetters = true, value={"userRoles", "imports", "dataSet"})
    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    /**********************************************************************************/

}
