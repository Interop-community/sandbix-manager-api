package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity
@NamedQueries({
        // Used to retrieve a registered CdsHook when a new launch scenario is being created with the CdsHook
        @NamedQuery(name="CdsHook.findByHookIdAndCdsServiceEndpointId",
                query="SELECT c FROM CdsHook c WHERE c.hookId = :hookId and " +
                        "c.cdsServiceEndpointId = :cdsServiceEndpointId"),
        // Used to attach all the cds-hook to the cds-service-endpoint when get cds-service-endpoint is called
        @NamedQuery(name="CdsHook.findCdsServiceEndpointId",
                query="SELECT c FROM CdsHook c WHERE c.cdsServiceEndpointId = :cdsServiceEndpointId")
})
@TypeDef(name = "jsonb-node", typeClass = JsonNodeStringType.class)
public class CdsHook {

    private Integer id;
    private String logoUri;
    private Image logo;
    private String hook;
    private String title;
    private String description;
    private String hookId;
    private JsonNode prefetch;
    private Integer cdsServiceEndpointId;
    private String hookUrl;

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

    @Type(type = "jsonb-node")
    @Column(columnDefinition = "json")
    public JsonNode getPrefetch() {
        return prefetch;
    }

    public void setPrefetch(JsonNode prefetch) {
        this.prefetch = prefetch;
    }

    public Integer getCdsServiceEndpointId() {
        return cdsServiceEndpointId;
    }

    public void setCdsServiceEndpointId(Integer cdsServiceEndpointId) {
        this.cdsServiceEndpointId = cdsServiceEndpointId;
    }

    public String getHookUrl() {
        return hookUrl;
    }

    public void setHookUrl(String hookUrl) {
        this.hookUrl = hookUrl;
    }
}
