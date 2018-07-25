package org.hspconsortium.sandboxmanagerapi.model;

/**
 * @deprecated Use Visibility2 instead
 */
@Deprecated
public enum Visibility {
    PUBLIC(0), PRIVATE(1), SHARED(2); // SHARED is not yet used

    private int numVal;

    Visibility(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
