package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NamedQueries({
        // Used to retrieve a user instance for multiple uses
        @NamedQuery(name="User.findBySbmUserId",
                query="SELECT c FROM User c WHERE c.sbmUserId = :sbmUserId"),
        // Used to retrieve a user instance for sandbox invites
        @NamedQuery(name="User.findByUserEmail",
                query="SELECT c FROM User c WHERE c.email = :email"),
        // Used for statistics
        @NamedQuery(name="User.fullCount",
                query="SELECT COUNT(*) FROM User c"),
        // Used for statistics
        @NamedQuery(name="User.intervalCount",
                query="SELECT COUNT(*) FROM User c WHERE c.createdTimestamp  >= :intervalTime")

})
public class User {
    private Integer id;
    private Timestamp createdTimestamp;
    private String email;
    private String sbmUserId;
    private String name;
    private Set<SystemRole> systemRoles = new HashSet<>();
    private List<Sandbox> sandboxes = new ArrayList<>();
    private Boolean hasAcceptedLatestTermsOfUse;
    private List<TermsOfUseAcceptance> termsOfUseAcceptances = new ArrayList<>();
    private Tier tierLevel;

    @Id // @Id indicates that this is a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSbmUserId() {
        return sbmUserId;
    }

    public void setSbmUserId(String sbmUserId) {
        this.sbmUserId = sbmUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tier getTierLevel() {
        return tierLevel;
    }

    public void setTierLevel(Tier tierLevel) {
        this.tierLevel = tierLevel;
    }

    @ElementCollection(targetClass = SystemRole.class)
    @CollectionTable(name = "system_role",joinColumns = @JoinColumn(name = "user_id"))
    @Column(name ="role", nullable = false)
    public Set<SystemRole> getSystemRoles() {
        return systemRoles;
    }

    public void setSystemRoles(Set<SystemRole> systemRoles) {
        this.systemRoles = systemRoles;
    }

    @ManyToMany(cascade={CascadeType.ALL})
    @JoinTable(name = "user_sandbox", joinColumns = {
            @JoinColumn(name = "user_id", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name = "sandbox_id",
                    nullable = false, updatable = false) })
    @JsonIgnoreProperties(ignoreUnknown = true, allowSetters = true, value={"userRoles", "imports", "dataSet"})
    public List<Sandbox> getSandboxes() {
        return sandboxes;
    }

    public void setSandboxes(List<Sandbox> sandboxes) {
        this.sandboxes = sandboxes;
    }

    @Transient
    public Boolean getHasAcceptedLatestTermsOfUse() {
        return hasAcceptedLatestTermsOfUse;
    }

    public void setHasAcceptedLatestTermsOfUse(Boolean hasAcceptedLatestTermsOfUse) {
        this.hasAcceptedLatestTermsOfUse = hasAcceptedLatestTermsOfUse;
    }

    @OneToMany(cascade={CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinTable(name = "user_terms_of_use_acceptance",
            joinColumns = {@JoinColumn(name = "user_id", nullable = false, updatable = false)})
    @JsonIgnoreProperties(ignoreUnknown = true, value={"termsOfUse"})
    public List<TermsOfUseAcceptance> getTermsOfUseAcceptances() {
        return termsOfUseAcceptances;
    }

    public void setTermsOfUseAcceptances(List<TermsOfUseAcceptance> termsOfUseAcceptances) {
        this.termsOfUseAcceptances = termsOfUseAcceptances;
    }
}
