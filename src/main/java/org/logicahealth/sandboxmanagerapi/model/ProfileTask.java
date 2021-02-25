package org.logicahealth.sandboxmanagerapi.model;

import java.util.List;
import java.util.Map;

public class ProfileTask {
    private String id;
    private Boolean status;
    private String error;
    private Map<String, List<String>> resourceSaved;
    private Map<String, List<String>> resourceNotSaved;
    private int totalCount;
    private int resourceSavedCount;
    private int resourceNotSavedCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Map<String, List<String>> getResourceSaved() {
        return resourceSaved;
    }

    public void setResourceSaved(Map<String, List<String>> resourceSaved) {
        this.resourceSaved = resourceSaved;
    }

    public Map<String, List<String>> getResourceNotSaved() {
        return resourceNotSaved;
    }

    public void setResourceNotSaved(Map<String, List<String>> resourceNotSaved) {
        this.resourceNotSaved = resourceNotSaved;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getResourceSavedCount() {
        return resourceSavedCount;
    }

    public void setResourceSavedCount(int resourceSavedCount) {
        this.resourceSavedCount = resourceSavedCount;
    }

    public int getResourceNotSavedCount() {
        return resourceNotSavedCount;
    }

    public void setResourceNotSavedCount(int resourceNotSavedCount) {
        this.resourceNotSavedCount = resourceNotSavedCount;
    }

}
