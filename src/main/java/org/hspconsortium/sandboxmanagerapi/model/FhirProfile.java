package org.hspconsortium.sandboxmanagerapi.model;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name="FhirProfile.findByFhirProfileId",
                query="SELECT c FROM FhirProfile c WHERE c.fhirProfileId = :fhirProfileId"),
        @NamedQuery(name="FhirProfileDetail.findByFullUrlAndFhirProfileId",
                query="SELECT c FROM FhirProfile c WHERE c.fullUrl = :fullUrl AND c.fhirProfileId = :fhirProfileId")

})
public class FhirProfile {

    private Integer id;
//    private FhirProfileDetail fhirProfileId;
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

//    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
//    @JoinColumn(name="fhir_profile_id")
//    public FhirProfileDetail getFhirProfileId() {
//        return fhirProfileId;
//    }
//
//    public void setFhirProfileId(FhirProfileDetail fhirProfileId) {
//        this.fhirProfileId = fhirProfileId;
//    }


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


//    private String profileName;
//    private String profileId;
//    private Sandbox sandbox;

//    public String getProfileName() {
//        return profileName;
//    }
//
//    public void setProfileName(String profileName) {
//        this.profileName = profileName;
//    }
//
//    public String getProfileId() {
//        return profileId;
//    }
//
//    public void setProfileId(String profileId) {
//        this.profileId = profileId;
//    }

//    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
//    @JoinColumn(name="sandbox_id")
//    public Sandbox getSandbox() {
//        return sandbox;
//    }
//
//    public void setSandbox(Sandbox sandbox) {
//        this.sandbox = sandbox;
//    }