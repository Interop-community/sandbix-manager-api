package org.hspconsortium.sandboxmanagerapi.dto;

import org.hspconsortium.sandboxmanagerapi.model.InviteStatus;

import java.sql.Timestamp;

public class SandboxInviteDto {
    private Integer id;
    private UserDto invitee;
    private UserDto invitedBy;
    private SandboxDto sandbox;
    private Timestamp inviteTimestamp;
    private InviteStatus status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserDto getInvitee() {
        return invitee;
    }

    public void setInvitee(UserDto invitee) {
        this.invitee = invitee;
    }

    public UserDto getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(UserDto invitedBy) {
        this.invitedBy = invitedBy;
    }

    public SandboxDto getSandbox() {
        return sandbox;
    }

    public void setSandbox(SandboxDto sandbox) {
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
