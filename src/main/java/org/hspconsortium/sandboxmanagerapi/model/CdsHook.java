package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
@NamedQueries({
        // Used to retrieve a registered CdsHook when a new launch scenario is being created with the CdsHook
        @NamedQuery(name="CdsHook.findByLogoUriAndHookId",
                query="SELECT c FROM CdsHook c WHERE c.logoUri = :logoUri and c.hookId = :hookId"),
        // Used to delete all registered CDS-Services when a sandbox is deleted
        @NamedQuery(name="CdsHook.findByHookId",
                query="SELECT c FROM CdsHook c WHERE c.hookId = :hookId"),
        // Used to retrieve all registered CDS-Services visible to a user of this a sandbox
        @NamedQuery(name="CdsHook.findByHookIdAndCreatedByOrVisibility",
                query="SELECT c FROM CdsHook c WHERE c.hookId = :hookId and " +
                        "(c.createdBy.sbmUserId = :createdBy or c.visibility = :visibility) "),
        // Used to delete a user's PRIVATE registered CDS-Services when they are removed from a sandbox
        @NamedQuery(name="CdsHook.findByHookIdAndCreatedBy",
                query="SELECT c FROM CdsHook c WHERE c.hookId = :hookId and " +
                        "c.createdBy.sbmUserId = :createdBy ")
})

public class CdsHook {

    private Integer id; // get rid
    private Integer hookId;
    private String logoUri;
    private Image logo;
    private String Id; // Add all the stuff
    private CdsServiceEndpoint cs; // many hook will have one cdsserviceendpoint

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHookId() {
        return hookId;
    }

    public void setHookId(Integer hookId) {
        this.hookId = hookId;
    }

    @OneToOne(cascade={CascadeType.ALL})
    @JoinColumn(name="logo_id")
    @JsonIgnore
    public Image getLogo() {
        return logo;
    }

    public void setLogo(Image logo) {
        this.logo = logo;
    }

    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

}
