package org.hspconsortium.sandboxmanagerapi.model;

public enum SystemRole {
    ADMIN(0), USER(1), CREATE_SANDBOX(2);

    private int numVal;

    SystemRole(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
