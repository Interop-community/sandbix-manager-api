package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class FhirTransaction {

    private Integer id;
    private Timestamp transactionTimestamp;
    private Integer sandboxId;
    private Integer performedById;
    private String url;
    private String fhirResource;
    private String domain;
    private String ipAddress;
    private Integer responseCode;
    private String method;
    private Boolean secured;
    private Integer payerUserId;

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public void setTransactionTimestamp(Timestamp transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

    public Integer getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(Integer sandboxId) {
        this.sandboxId = sandboxId;
    }

    public Integer getPerformedById() {
        return performedById;
    }

    public void setPerformedById(Integer performedById) {
        this.performedById = performedById;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFhirResource() {
        return fhirResource;
    }

    public void setFhirResource(String fhirResource) {
        this.fhirResource = fhirResource;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Boolean getSecured() {
        return secured;
    }

    public void setSecured(Boolean secured) {
        this.secured = secured;
    }

    public Integer getPayerUserId() {
        return payerUserId;
    }

    public void setPayerUserId(Integer payerUserId) {
        this.payerUserId = payerUserId;
    }
}
