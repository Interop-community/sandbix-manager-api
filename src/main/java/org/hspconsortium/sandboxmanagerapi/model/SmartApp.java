package org.hspconsortium.sandboxmanagerapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "smart_app")
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class SmartApp {

    @Id
    protected String id;

    private String sandboxId;

    private String manifestUrl;

    private String manifest;

    private String clientId;

    protected Integer ownerId;

    protected Timestamp createdTimestamp;

    @Enumerated(EnumType.STRING)
    protected Visibility2 visibility;

    private String samplePatients;

    private String info;

    private String briefDescription;

    private String author;

}