package org.hspconsortium.sandboxmanagerapi.model;

public enum Role {
    ADMIN(0), USER(1), READONLY(2), MANAGE_USERS(3), MANAGE_DATA(4);

    private int numVal;

    Role(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
