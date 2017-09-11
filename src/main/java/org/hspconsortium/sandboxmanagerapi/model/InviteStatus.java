package org.hspconsortium.sandboxmanagerapi.model;

public enum InviteStatus {
    PENDING(0), REJECTED(1), REVOKED(2), ACCEPTED(3);

    private int numVal;

    InviteStatus(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
