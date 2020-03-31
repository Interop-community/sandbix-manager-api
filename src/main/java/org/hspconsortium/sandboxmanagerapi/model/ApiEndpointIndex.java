package org.hspconsortium.sandboxmanagerapi.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("api_endpoint_index")
@Configuration
public class ApiEndpointIndex {

    private FhirVersion prev;
    private FhirVersion current;
    private String manager;

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

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }
}


