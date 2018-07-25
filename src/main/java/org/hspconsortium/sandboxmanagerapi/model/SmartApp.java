package org.hspconsortium.sandboxmanagerapi.model;

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

    private String manifestUrl;

    private String manifest;

    private String clientId;

    private Integer ownerId;

    private Timestamp createdTimestamp;

    @Enumerated(EnumType.STRING)
    private Visibility2 visibility;

    private String samplePatients;

    private String info;

    private String briefDescription;

    private String author;

    @Enumerated(EnumType.STRING)
    private CopyType copyType;

}
