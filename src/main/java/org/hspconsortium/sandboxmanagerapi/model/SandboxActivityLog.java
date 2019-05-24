package org.hspconsortium.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        // Not currently used, available for a future dashboard
        @NamedQuery(name="SandboxActivityLog.findByUserSbmUserId",
                query="SELECT c FROM SandboxActivityLog c WHERE c.user.sbmUserId = :sbmUserId"),
        // Used to find all activity for a user for deleting the user
        @NamedQuery(name="SandboxActivityLog.findByUserId",
                query="SELECT c FROM SandboxActivityLog c WHERE c.user.id = :userId"),
        // Used to delete all activity records when a sandbox is deleted, to remove foreign keys
        @NamedQuery(name="SandboxActivityLog.findBySandboxId",
                query="SELECT c FROM SandboxActivityLog c WHERE c.sandbox.sandboxId = :sandboxId"),
        // Not currently used, available for a future dashboard
        @NamedQuery(name="SandboxActivityLog.findBySandboxActivity",
                query="SELECT c FROM SandboxActivityLog c WHERE c.activity = :sandboxActivity"),
        // Used for statistics
        @NamedQuery(name="SandboxActivityLog.findAllForSpecificTimePeriod",
                    query="SELECT c FROM SandboxActivityLog c WHERE c.timestamp BETWEEN :beginDate AND :endDate"),
        // Used for statistics
        @NamedQuery(name="SandboxActivityLog.intervalActive",
                query="SELECT COUNT(DISTINCT c.user) FROM SandboxActivityLog c WHERE c.timestamp  >= :intervalTime")
})
public class SandboxActivityLog {
    private Integer id;
    private Timestamp timestamp;
    private User user;
    private SandboxActivity activity;
    private Sandbox sandbox;
    private String additionalInfo;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="user_id")
    @JsonIgnoreProperties(ignoreUnknown = true, allowSetters = true,
            value={"sandboxes", "termsOfUseAcceptances", "systemRoles"})
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setActivity(SandboxActivity activity) {

        this.activity = activity;
    }

    public SandboxActivity getActivity() {
        return activity;
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

    @Lob
    @Column( length = 1000 )
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
