package org.logicahealth.sandboxmanagerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        // Used to retrieve all sandbox invites for an invitee to show a user their sandbox invites
        @NamedQuery(name="SandboxInvite.findInvitesByInviteeId",
                query="SELECT c FROM SandboxInvite c WHERE c.invitee.sbmUserId = :inviteeId"),
        // Used to delete all sandbox invites when a sandbox is deleted
        // Used to retrieve all sandbox invites for a sandbox
        @NamedQuery(name="SandboxInvite.findInvitesBySandboxId",
                query="SELECT c FROM SandboxInvite c WHERE c.sandbox.sandboxId = :sandboxId"),
        // Used to retrieve an existing sandbox invite to change it's status ex. PENDING to REVOKED
        @NamedQuery(name="SandboxInvite.findInvitesByInviteeIdAndSandboxId",
                query="SELECT c FROM SandboxInvite c WHERE c.invitee.sbmUserId = :inviteeId and c.sandbox.sandboxId = :sandboxId"),
        // Used to retrieve an existing sandbox invite to change it's status ex. PENDING to REVOKED
        @NamedQuery(name="SandboxInvite.findInvitesByInviteeEmailAndSandboxId",
                query="SELECT c FROM SandboxInvite c WHERE c.invitee.email = :inviteeEmail and c.sandbox.sandboxId = :sandboxId"),
        // Used to retrieve existing sandbox invites for an email to merge user accounts
        @NamedQuery(name="SandboxInvite.findInvitesByInviteeEmail",
                query="SELECT c FROM SandboxInvite c WHERE c.invitee.email = :inviteeEmail"),
        // Used to retrieve all sandbox invites for an invitee to show a user their PENDING (or other status) sandbox invites
        @NamedQuery(name="SandboxInvite.findInvitesByInviteeIdAndStatus",
                query="SELECT c FROM SandboxInvite c WHERE c.invitee.sbmUserId = :inviteeId and c.status = :status"),
        // Used to retrieve all sandbox invites for a sandbox to show a user manager PENDING, REJECTED, etc sandbox invites
        @NamedQuery(name="SandboxInvite.findInvitesBySandboxIdAndStatus",
                query="SELECT c FROM SandboxInvite c WHERE c.sandbox.sandboxId = :sandboxId and c.status = :status")
})
public class SandboxInvite {
    private Integer id;
    private User invitee;
    private User invitedBy;
    private Sandbox sandbox;
    private Timestamp inviteTimestamp;
    private InviteStatus status;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="invitee_id")
    public User getInvitee() {
        return invitee;
    }

    public void setInvitee(User invitee) {
        this.invitee = invitee;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="invited_by_id")
    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
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

    public Timestamp getInviteTimestamp() {
        return inviteTimestamp;
    }

    public void setInviteTimestamp(Timestamp inviteTimestamp) {
        this.inviteTimestamp = inviteTimestamp;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public InviteStatus getStatus() {
        return status;
    }
}
