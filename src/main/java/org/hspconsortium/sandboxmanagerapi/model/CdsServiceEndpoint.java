package org.hspconsortium.sandboxmanagerapi.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
        // Used to retrieve a registered CdsServiceEndpoint when a new launch scenario is being created with the CdsServiceEndpoint
        @NamedQuery(name="CdsServiceEndpoint.findByCdsServiceEndpointUrlAndSandboxId",
                query="SELECT c FROM CdsServiceEndpoint c WHERE c.url = :url and " +
                        "c.sandbox.sandboxId = :sandboxId"),
        // Used to delete all registered CDS-Services when a sandbox is deleted
        @NamedQuery(name="CdsServiceEndpoint.findBySandboxId",
                query="SELECT c FROM CdsServiceEndpoint c WHERE c.sandbox.sandboxId = :sandboxId"),
        // Used to retrieve all registered CDS-Services visible to a user of this a sandbox
        @NamedQuery(name="CdsServiceEndpoint.findBySandboxIdAndCreatedByOrVisibility",
                query="SELECT c FROM CdsServiceEndpoint c WHERE c.sandbox.sandboxId = :sandboxId and " +
                        "(c.createdBy.sbmUserId = :createdBy or c.visibility = :visibility)"),
        // Used to delete a user's PRIVATE registered CDS-Services when they are removed from a sandbox
        @NamedQuery(name="CdsServiceEndpoint.findBySandboxIdAndCreatedBy",
                query="SELECT c FROM CdsServiceEndpoint c WHERE c.sandbox.sandboxId = :sandboxId and " +
                        "c.createdBy.sbmUserId = :createdBy")
})
public class CdsServiceEndpoint extends AbstractSandboxItem {

    private String url;
    private String title;
    private String description;
    private List<CdsHook> cdsHooks = new ArrayList<>();
    private Timestamp lastUpdated;

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

    /**********************************************************************************/

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(columnDefinition="LONGTEXT")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    public List<CdsHook> getCdsHooks() {
        return cdsHooks;
    }

    public void setCdsHooks(List<CdsHook> cdsHooks) {
        this.cdsHooks = cdsHooks;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
