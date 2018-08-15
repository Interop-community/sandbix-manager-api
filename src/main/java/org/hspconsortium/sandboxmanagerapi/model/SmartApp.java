package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@IdClass(SmartAppCompositeId.class)
public class SmartApp {

    @Id
    private String smartAppId;

    @Id
    private String sandboxId;

    private String clientName;

    private String manifestUrl;

    private String clientId;

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="owner_id")
    @JsonIgnoreProperties(ignoreUnknown = true, allowSetters = true,
            value={"sandboxes", "termsOfUseAcceptances", "systemRoles"})
    private User owner;

    private Timestamp createdTimestamp;

    @Enumerated(EnumType.STRING)
    private Visibility2 visibility;

    private String samplePatients;

    private String info;

    private String briefDescription;

    private String author;

    @Enumerated(EnumType.STRING)
    private CopyType copyType;

    private String launchUrl;

    private String logoUri;

    private String clientUri;

    private String fhirVersions;

    @OneToOne(cascade={CascadeType.ALL})
    @JoinColumn(name="logo_id")
    @JsonIgnore
    private Image logo;

    @Transient
    private String clientJSON;

}
