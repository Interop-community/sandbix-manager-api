package org.logicahealth.sandboxmanagerapi.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.TemporalType;
import javax.persistence.CascadeType;
import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Temporal;

// import java.sql.Timestamp;
import java.util.Date;


@Entity
public class SandboxExport {
    private Integer id;
    private Sandbox sandbox;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    private String userId;

    @Column(columnDefinition = "TEXT")
    @javax.persistence.Lob
    private String token;
    
    private String server;

    @Enumerated(EnumType.STRING)
    private SandboxExportEnum status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date completedTime;

    @Column(columnDefinition = "TEXT")
    @javax.persistence.Lob
    private String reason;  

    public SandboxExport(){

    }

    public SandboxExport(Sandbox sandbox, String userId, String token, String server, SandboxExportEnum status,  Date createdTime) {
        this.sandbox = sandbox;
        this.createdTime = createdTime;
        this.userId = userId;
        this.token = token;
        this.server = server;
        this.status = status;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }
    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="sandbox_id")
    @JsonIgnoreProperties(ignoreUnknown = true, allowSetters = true, value={"userRoles", "imports", "dataSet"})
    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    @Enumerated(EnumType.STRING)
    public SandboxExportEnum getStatus() {
        return status;
    }

    public void setStatus(SandboxExportEnum status) {
        this.status = status;
    }

    public Date getcreatedTime() {
        return createdTime;
    }

    public void setcreatedTime(Date datetime) {
        this.createdTime = datetime;
    }

    public Date getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }

    public String getuserId() {
        return userId;
    }

    public void setuserId(String userId) {
        this.userId = userId;
    }

    public String gettoken() {
        return token;
    }

    public void settoken(String token) {
        this.token = token;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getserver() {
        return server;
    }

    public void setserver(String server) {
        this.server = server;
    }
}
