package org.logicahealth.sandboxmanagerapi.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class SandboxImport {
    private Integer id;
    private String importFhirUrl;
    private Timestamp timestamp;
    private String durationSeconds;
    private String successCount;
    private String failureCount;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public String getImportFhirUrl() {
        return importFhirUrl;
    }

    public void setImportFhirUrl(String importFhirUrl) {
        this.importFhirUrl = importFhirUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(String durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(String successCount) {
        this.successCount = successCount;
    }

    public String getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(String failureCount) {
        this.failureCount = failureCount;
    }
}
