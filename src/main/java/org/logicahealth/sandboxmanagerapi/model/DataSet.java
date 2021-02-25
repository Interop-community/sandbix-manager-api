package org.logicahealth.sandboxmanagerapi.model;

public enum DataSet {
    NONE("none"), DEFAULT("default"), NA("na");

    private String value;

    DataSet(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

};