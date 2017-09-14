package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        // Used to:
        // 1) make sure that a user persona cannot be used to log in to sandbox manager
        // 2) make sure that a user persona with a given id does not exist when a user is creating one
        // 3) make sure that a user persona cannot be used to create a sandbox
        @NamedQuery(name="UserPersona.findByPersonaUserId",
                query="SELECT c FROM UserPersona c WHERE c.personaUserId = :personaUserId"),
        // Used to retrieve a user persona instance to be used in the creation of a launch scenario
        @NamedQuery(name="UserPersona.findByPersonaUserIdAndSandboxId",
                query="SELECT c FROM UserPersona c WHERE c.personaUserId = :personaUserId and c.sandbox.sandboxId = :sandboxId"),
        // Used to delete all user personas when a sandbox is deleted
        @NamedQuery(name="UserPersona.findBySandboxId",
                query="SELECT c FROM UserPersona c WHERE c.sandbox.sandboxId = :sandboxId"),
        // Used to retrieve all user personas visible to a user of this a sandbox
        @NamedQuery(name="UserPersona.findBySandboxIdAndCreatedByOrVisibility",
                query="SELECT c FROM UserPersona c WHERE c.sandbox.sandboxId = :sandboxId and " +
                "(c.createdBy.sbmUserId = :createdBy or c.visibility = :visibility)"),
        // Used to retrieve a default user persona visible to a user of this a sandbox
        @NamedQuery(name="UserPersona.findDefaultBySandboxId",
                query="SELECT c FROM UserPersona c WHERE c.sandbox.sandboxId = :sandboxId and " +
                        "(c.createdBy.sbmUserId = :createdBy or c.visibility = :visibility)  order by c.visibility"),
        // Used to delete a user's PRIVATE user personas when they are removed from a sandbox
        @NamedQuery(name="UserPersona.findBySandboxIdAndCreatedBy",
                query="SELECT c FROM UserPersona c WHERE c.sandbox.sandboxId = :sandboxId and " +
                        "c.createdBy.sbmUserId = :createdBy")
})
public class UserPersona extends AbstractSandboxItem {
    private String personaUserId;
    private String personaName;
    private String password;
    private String fhirId;
    private String fhirName;
    private String resource;
    private String resourceUrl;

    /******************* User Persona Property Getter/Setters ************************/

    public String getPersonaUserId() {
        return personaUserId;
    }

    public void setPersonaUserId(String personaUserId) {
        this.personaUserId = personaUserId;
    }

    public String getPersonaName() {
        return personaName;
    }

    public void setPersonaName(String personaName) {

        this.personaName = personaName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFhirId() {
        return fhirId;
    }

    public void setFhirId(String fhirId) {
        this.fhirId = fhirId;
    }

    public String getFhirName() {
        return fhirName;
    }

    public void setFhirName(String fhirName) {
        this.fhirName = fhirName;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
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
