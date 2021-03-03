package org.logicahealth.sandboxmanagerapi.model;

public enum SandboxActivity {
    CREATED(0), LOGGED_IN(1), DELETED(2), OPEN_ENDPOINT(3),
    USER_INVITED(4), USER_ACCEPTED_INVITE(5), USER_INVITATION_REVOKED(6),
    USER_INVITATION_REJECTED(7), USER_REMOVED(8), USER_ADDED(9),
    USER_SYSTEM_ROLE_CHANGE(10), USER_SANDBOX_ROLE_CHANGE(11),
    USER_CREATED(12), SANDBOX_DATA_IMPORT(13), SANDBOX_RESET(14), USER_DELETED(15);

    private int numVal;

    SandboxActivity(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
