package org.hspconsortium.sandboxmanagerapi.model;

import javax.persistence.*;

@Entity
public class AuthClient {
    private Integer id;
    private String clientName;
    private String clientId;
    private String logoUri;
    private Integer authDatabaseId;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    public Integer getAuthDatabaseId() {
        return authDatabaseId;
    }

    public void setAuthDatabaseId(Integer authDatabaseId) {
        this.authDatabaseId = authDatabaseId;
    }
}
