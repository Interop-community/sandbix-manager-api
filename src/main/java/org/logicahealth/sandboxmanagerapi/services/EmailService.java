package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;

import java.io.IOException;
import java.net.URL;

public interface EmailService {

    void sendEmail(final User inviter, final User invitee, Sandbox sandbox, int invitationId) throws IOException;
    void sendExportNotificationEmail(User user, URL sandboxExportFile, String sandboxName);
    void sendImportErrorNotificationEmail(User user, String sandboxName);
}
