package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.json.JSONObject;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@NamedQueries({
        // Used to delete all launch scenarios when a sandbox is deleted
        @NamedQuery(name="LaunchScenarioCdsServiceEndpoint.findBySandboxId",
                query="SELECT c FROM LaunchScenarioCdsServiceEndpoint c WHERE c.sandbox.sandboxId = :sandboxId"),
        // Used to determine if a registered cds is being used in a launch scenarios and cannot be deleted
        @NamedQuery(name="LaunchScenarioCdsServiceEndpoint.findByCdsIdAndSandboxId",
                query="SELECT c FROM LaunchScenarioCdsServiceEndpoint c WHERE c.cds.id = :cdsId and c.sandbox.sandboxId = :sandboxId"),
        // Used to determine if a persona is being used in a launch scenarios and cannot be deleted
        @NamedQuery(name="LaunchScenarioCdsServiceEndpoint.findByUserPersonaIdAndSandboxId",
                query="SELECT c FROM LaunchScenarioCdsServiceEndpoint c WHERE c.userPersona.id = :userPersonaId and c.sandbox.sandboxId = :sandboxId"),
        // Used to retrieve all launch scenarios visible to a user of this a sandbox
        @NamedQuery(name="LaunchScenarioCdsServiceEndpoint.findBySandboxIdAndCreatedByOrVisibility",
                query="SELECT c FROM LaunchScenarioCdsServiceEndpoint c WHERE c.sandbox.sandboxId = :sandboxId and " +
                        "(c.createdBy.sbmUserId = :createdBy or c.visibility = :visibility)"),
        // Used to delete a user's PRIVATE launch scenarios when they are removed from a sandbox
        @NamedQuery(name="LaunchScenarioCdsServiceEndpoint.findBySandboxIdAndCreatedBy",
                query="SELECT c FROM LaunchScenarioCdsServiceEndpoint c WHERE c.sandbox.sandboxId = :sandboxId and " +
                        "c.createdBy.sbmUserId = :createdBy")
})

public class LaunchScenarioCdsServiceEndpoint extends AbstractSandboxItem {

    private String description;
    private UserPersona userPersona;
    private CdsServiceEndpoint cdsServiceEndpoint; // TODO: get rid
    private CdsHook cdsHook; // TODO: need CDS-hook Id
    private String context;
    private Timestamp lastLaunch;

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

    /*******************************************************************************/

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
    @JoinColumn(name="cds_service_endpoint_id")
    public CdsServiceEndpoint getCdsServiceEndpoint() {
        return cdsServiceEndpoint;
    }

    public void setCdsServiceEndpoint(CdsServiceEndpoint cdsServiceEndpoint) {
        this.cdsServiceEndpoint = cdsServiceEndpoint;
    }
    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="cds_hook_id")
    public CdsHook getCdsHook() {
        return cdsHook;
    }

    public void setCdsHook(CdsHook cdsHook) {
        this.cdsHook = cdsHook;
    }

    public Timestamp getLastLaunch() {
        return lastLaunch;
    }

    public void setLastLaunch(Timestamp lastLaunch) {
        this.lastLaunch = lastLaunch;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
