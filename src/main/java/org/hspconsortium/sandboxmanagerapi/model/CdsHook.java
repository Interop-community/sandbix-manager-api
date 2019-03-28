package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.json.JSONObject;

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

    private Integer id;
    private String logoUri;
    private Image logo;
    private String hook;
    private String title;
    private String description;
    private String hookId;
    private String prefetch;

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getHook() {
        return hook;
    }

    public void setHook(String hook) {
        this.hook = hook;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHookId() {
        return hookId;
    }

    public void setHookId(String hookId) {
        this.hookId = hookId;
    }

    public String getPrefetch() {
        return prefetch;
    }

    public void setPrefetch(String prefetch) {
        this.prefetch = prefetch;
    }
}
