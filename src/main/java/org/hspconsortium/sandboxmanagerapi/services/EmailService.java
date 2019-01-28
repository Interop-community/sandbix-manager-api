package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxInvite;
import org.hspconsortium.sandboxmanagerapi.model.User;

import java.io.IOException;

public interface EmailService {

    void sendEmail(final User inviter, final User invitee, Sandbox sandbox, int invitationId) throws IOException;

}
