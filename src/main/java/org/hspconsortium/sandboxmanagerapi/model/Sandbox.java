package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.xml.crypto.Data;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
        // Used to retrieve a sandbox instance for multiple uses
        @NamedQuery(name="Sandbox.findBySandboxId",
                query="SELECT c FROM Sandbox c WHERE c.sandboxId = :sandboxId"),
        // Used to retrieve all sandboxes visible to a user
        @NamedQuery(name="Sandbox.findByVisibility",
                query="SELECT c FROM Sandbox c WHERE c.visibility = :visibility"),
        // Used for statistics
        @NamedQuery(name="Sandbox.fullCount",
                query="SELECT COUNT(*) FROM Sandbox"),
        // Used for statistics
        @NamedQuery(name="Sandbox.schemaCount",
                query="SELECT COUNT(*) FROM Sandbox c WHERE c.apiEndpointIndex = :apiEndpointIndex"),
        // Used for statistics
        @NamedQuery(name="Sandbox.intervalCount",
                query="SELECT COUNT(*) FROM Sandbox c WHERE c.createdTimestamp  >= :intervalTime")

})
public class Sandbox extends AbstractItem {

    private String sandboxId;
    private String name;
    private String description;
    private String apiEndpointIndex;
    private String fhirServerEndPoint;
    private DataSet dataSet = DataSet.NA;
    private DataSet apps = DataSet.NA;
    private boolean allowOpenAccess;
    private List<UserRole> userRoles = new ArrayList<>();
    private List<SmartApp> smartApps = new ArrayList<>();
    private List<SandboxImport> imports = new ArrayList<>();
    private String expirationMessage;
    private Date expirationDate;
    private Integer payerUserId;

    /******************* Sandbox Property Getter/Setters ************************/

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApiEndpointIndex() {
        return apiEndpointIndex;
    }

    public void setApiEndpointIndex(String apiEndpointIndex) {
        this.apiEndpointIndex = apiEndpointIndex;
    }

    public String getFhirServerEndPoint() {
        return fhirServerEndPoint;
    }

    public void setFhirServerEndPoint(String fhirServerEndPoint) {
        this.fhirServerEndPoint = fhirServerEndPoint;
    }

    @Transient
    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Transient
    public DataSet getApps() {
        return apps;
    }

    public void setApps(DataSet apps) {
        this.apps = apps;
    }

    public boolean isAllowOpenAccess() {
        return allowOpenAccess;
    }

    public void setAllowOpenAccess(boolean allowOpenAccess) {
        this.allowOpenAccess = allowOpenAccess;
    }

    @OneToMany(cascade={CascadeType.ALL})
    @JoinTable(name = "sandbox_user_roles", joinColumns = {
            @JoinColumn(name = "sandbox", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name = "user_roles",
                    nullable = false, updatable = false) })
    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    @OneToMany(cascade={CascadeType.ALL})
    @JoinTable(name = "sandbox_smart_apps", joinColumns = {
            @JoinColumn(name = "sandbox", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name = "smart_app",
                    nullable = false, updatable = false) })
    public List<SmartApp> getSmartApps() {
        return smartApps;
    }

    public void setSmartApps(List<SmartApp> smartApps) {
        this.smartApps = smartApps;
    }

    @OneToMany(cascade={CascadeType.ALL})
    public List<SandboxImport> getImports() {
        return imports;
    }

    public void setImports(List<SandboxImport> imports) {
        this.imports = imports;
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

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getExpirationMessage() { return expirationMessage; }

    public void setExpirationMessage(String expirationMessage) { this.expirationMessage = expirationMessage; }

    public Date getExpirationDate() { return expirationDate; }

    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }

    public Integer getPayerUserId() {
        return payerUserId;
    }

    public void setPayerUserId(Integer payerUserId) {
        this.payerUserId = payerUserId;
    }
}
