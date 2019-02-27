package org.hspconsortium.sandboxmanagerapi.dto;

import org.hspconsortium.sandboxmanagerapi.model.InviteStatus;

import java.sql.Timestamp;

public class SecuredSandboxInviteDto {
    private Integer id;
    private SecuredUserDto invitee;
    private SecuredUserDto invitedBy;
    private SecuredSandboxDto sandbox;
    private Timestamp inviteTimestamp;
    private InviteStatus status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SecuredUserDto getInvitee() {
        return invitee;
    }

    public void setInvitee(SecuredUserDto invitee) {
        this.invitee = invitee;
    }

    public SecuredUserDto getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(SecuredUserDto invitedBy) {
        this.invitedBy = invitedBy;
    }

    public SecuredSandboxDto getSandbox() {
        return sandbox;
    }

    public void setSandbox(SecuredSandboxDto sandbox) {
        this.sandbox = sandbox;
    }

    public Timestamp getInviteTimestamp() {
        return inviteTimestamp;
    }

    public void setInviteTimestamp(Timestamp inviteTimestamp) {
        this.inviteTimestamp = inviteTimestamp;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

}
