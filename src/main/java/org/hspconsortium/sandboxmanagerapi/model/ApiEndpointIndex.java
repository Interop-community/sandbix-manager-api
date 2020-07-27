package org.hspconsortium.sandboxmanagerapi.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("api-endpoint-index")
public class ApiEndpointIndex {

    private FhirVersion prev;
    private FhirVersion current;

    public FhirVersion getPrev() {
        return prev;
    }

    public void setPrev(FhirVersion prev) {
        this.prev = prev;
    }

    public FhirVersion getCurrent() {
        return current;
    }

    public void setCurrent(FhirVersion current) {
        this.current = current;
    }

    public ApiEndpointIndex() { }

    public ApiEndpointIndex(FhirVersion prev, FhirVersion current) {
        this.prev = prev;
        this.current = current;
    }

}


