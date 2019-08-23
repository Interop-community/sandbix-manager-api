package org.hspconsortium.sandboxmanagerapi.model;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name="FhirProfile.findAllSDsforAProfileByFhirProfileId",
                query="SELECT c FROM FhirProfile c WHERE c.fhirProfileId = :fhirProfileId AND c.relativeUrl LIKE 'StructureDefinition/%' "),
        @NamedQuery(name="FhirProfile.findByFhirProfileId",
                query="SELECT c FROM FhirProfile c WHERE c.fhirProfileId = :fhirProfileId"),
        @NamedQuery(name="FhirProfile.findFhirProfileWithASpecificTypeForAGivenSandbox",
                query="SELECT c FROM FhirProfile c WHERE c.fhirProfileId = :fhirProfileId AND c.profileType = :profileType"),
        @NamedQuery(name="FhirProfile.findAllProfileTypeForAGivenProfileId",
                query="SELECT c.profileType FROM FhirProfile c WHERE c.fhirProfileId = :fhirProfileId")
})
public class FhirProfile {

    private Integer id;
    private Integer fhirProfileId;
    private String fullUrl;
    private String relativeUrl;
    private String profileType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFhirProfileId() {
        return fhirProfileId;
    }

    public void setFhirProfileId(Integer fhirProfileId) {
        this.fhirProfileId = fhirProfileId;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public void setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

}
