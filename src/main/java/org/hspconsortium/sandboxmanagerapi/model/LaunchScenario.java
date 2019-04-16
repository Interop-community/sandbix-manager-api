package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

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
                "c.createdBy.sbmUserId = :createdBy"),
        // Used to determine if a registered cds-hook is being used in a launch scenarios and cannot be deleted
        @NamedQuery(name="LaunchScenario.findByCdsHookIdAndSandboxId",
                query="SELECT c FROM LaunchScenario c WHERE c.cdsHook.id = :cdsHookId AND c.sandbox.sandboxId = :sandboxId")
})
@TypeDef(name = "jsonb-node", typeClass = JsonNodeStringType.class)
public class LaunchScenario extends AbstractSandboxItem {

    private String description;
    private UserPersona userPersona;
    private App app;
    private List<ContextParams> contextParams;
    private Timestamp lastLaunch;
    private Long lastLaunchSeconds;
    private String patient;
    private String patientName;
    private String encounter;
    private String location;
    private String resource;
    private String intent;
    private String smartStyleUrl;
    private String title;
    private String needPatientBanner;
    private CdsHook cdsHook;
    private JsonNode context;

    /******************* Launch Scenario Property Getter/Setters ************************/

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="cds_hook_id")
    public CdsHook getCdsHook() {
        return cdsHook;
    }

    public void setCdsHook(CdsHook cdsHook) {
        this.cdsHook = cdsHook;
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

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getEncounter() {
        return encounter;
    }

    public void setEncounter(String encounter) {
        this.encounter = encounter;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getSmartStyleUrl() {
        return smartStyleUrl;
    }

    public void setSmartStyleUrl(String smartStyleUrl) {
        this.smartStyleUrl = smartStyleUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNeedPatientBanner() {
        return needPatientBanner;
    }

    public void setNeedPatientBanner(String needPatientBanner) {
        this.needPatientBanner = needPatientBanner;
    }

    @Type(type = "jsonb-node")
    @Column(columnDefinition = "json")
    public JsonNode getContext() {
        return context;
    }

    public void setContext(JsonNode context) {
        this.context = context;
    }
}
