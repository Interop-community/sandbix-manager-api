package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;

import java.io.IOException;

public interface EmailService {

    void sendEmail(final User inviter, final User invitee, Sandbox sandbox, int invitationId) throws IOException;

}
