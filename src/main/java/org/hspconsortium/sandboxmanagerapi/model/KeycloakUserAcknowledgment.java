package org.hspconsortium.sandboxmanagerapi.model;

import javax.persistence.*;

@Entity
@NamedQuery(name="KeycloakUserAcknowledgment.findBySbmUserId", query="SELECT c.sbmUserId FROM KeycloakUserAcknowledgment c WHERE c.sbmUserId = :sbmUserId")
public class KeycloakUserAcknowledgment {
    private String sbmUserId;

    public String getSbmUserId() {
        return sbmUserId;
    }

    public void setSbmUserId(String sbmUserId) {
        this.sbmUserId = sbmUserId;
    }
}
