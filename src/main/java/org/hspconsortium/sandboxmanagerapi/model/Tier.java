package org.hspconsortium.sandboxmanagerapi.model;

public enum Tier {
    FREE(0), DEVELOPER(1), TEAM(2), ENTERPRISE(3);

    private int numVal;

    Tier(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
