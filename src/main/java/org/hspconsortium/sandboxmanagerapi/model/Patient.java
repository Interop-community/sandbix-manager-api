package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@NamedQueries({
        // Used to retrieve a patient when a new launch scenario is being created with the patient
        @NamedQuery(name="Patient.findByFhirIdAndSandboxId",
                query="SELECT c FROM Patient c WHERE c.fhirId = :fhirId and c.sandbox.sandboxId = :sandboxId"),
        // Used to delete all patients when a sandbox is deleted
        @NamedQuery(name="Patient.findBySandboxId",
                query="SELECT c FROM Patient c WHERE c.sandbox.sandboxId = :sandboxId")
})
public class Patient {
    private Integer id;
    private String name;
    private String fhirId;
    private String resource;
    private Sandbox sandbox;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFhirId(String fhirId) {
        this.fhirId = fhirId;
    }

    public String getFhirId() {
        return fhirId;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
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

}
