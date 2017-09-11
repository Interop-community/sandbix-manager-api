package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@NamedQueries({
        // Used to delete all launch scenarios when a sandbox is deleted
        @NamedQuery(name="LaunchScenario.findBySandboxId",
                query="SELECT c FROM LaunchScenario c WHERE c.sandbox.sandboxId = :sandboxId"),
        // Used to determine if a registered app is being used in a launch scenarios and cannot be deleted
        @NamedQuery(name="LaunchScenario.findByAppIdAndSandboxId",
                query="SELECT c FROM LaunchScenario c WHERE c.app.id = :appId and c.sandbox.sandboxId = :sandboxId"),
        // Used to determine if a persona is being used in a launch scenarios and cannot be deleted
        @NamedQuery(name="LaunchScenario.findByUserPersonaIdAndSandboxId",
                query="SELECT c FROM LaunchScenario c WHERE c.userPersona.id = :userPersonaId and c.sandbox.sandboxId = :sandboxId"),
        // Used to retrieve all launch scenarios visible to a user of this a sandbox
        @NamedQuery(name="LaunchScenario.findBySandboxIdAndCreatedByOrVisibility",
                query="SELECT c FROM LaunchScenario c WHERE c.sandbox.sandboxId = :sandboxId and " +
                "(c.createdBy.sbmUserId = :createdBy or c.visibility = :visibility)"),
        // Used to delete a user's PRIVATE launch scenarios when they are removed from a sandbox
        @NamedQuery(name="LaunchScenario.findBySandboxIdAndCreatedBy",
        query="SELECT c FROM LaunchScenario c WHERE c.sandbox.sandboxId = :sandboxId and " +
                "c.createdBy.sbmUserId = :createdBy")
})
public class LaunchScenario extends AbstractSandboxItem {

    private String description;
    private boolean launchEmbedded;
    private Patient patient;
    private UserPersona userPersona;
    private App app;
    private List<ContextParams> contextParams;
    private Timestamp lastLaunch;
    private Long lastLaunchSeconds;


    /******************* Launch Scenario Property Getter/Setters ************************/

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLaunchEmbedded() {
        return launchEmbedded;
    }

    public void setLaunchEmbedded(boolean launchEmbedded) {
        this.launchEmbedded = launchEmbedded;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="patient_id")
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="user_persona_id")
    public UserPersona getUserPersona() {
        return userPersona;
    }

    public void setUserPersona(UserPersona userPersona) {
        this.userPersona = userPersona;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="app_id")
    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @OneToMany(cascade={CascadeType.ALL})
    public List<ContextParams> getContextParams() {
        return contextParams;
    }

    public void setContextParams(List<ContextParams> contextParams) {
        this.contextParams = contextParams;
    }

    @JsonIgnore
    public Timestamp getLastLaunch() {
        return lastLaunch;
    }

    public void setLastLaunch(Timestamp lastLaunch) {
        this.lastLaunch = lastLaunch;
    }

    @Transient
    public Long getLastLaunchSeconds() {
        if (this.lastLaunch != null) {
            this.lastLaunchSeconds = this.lastLaunch.getTime();
        }
        return this.lastLaunchSeconds;
    }

    public void setLastLaunchSeconds(Long lastLaunchSeconds) {
        this.lastLaunchSeconds = lastLaunchSeconds;
        if (lastLaunchSeconds != null) {
            this.lastLaunch = new Timestamp(lastLaunchSeconds);
        }
    }

    /******************* Inherited Property Getter/Setters ************************/

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
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

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
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

}
