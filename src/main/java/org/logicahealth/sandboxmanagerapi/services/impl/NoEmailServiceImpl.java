package org.logicahealth.sandboxmanagerapi.services.impl;

import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;

@Service
@Profile("!email")
public class NoEmailServiceImpl implements EmailService {
    private static Logger LOGGER = LoggerFactory.getLogger(NoEmailServiceImpl.class.getName());

    @Override
    public void sendEmail(User inviter, User invitee, Sandbox sandbox, int invitationId) throws IOException {
        LOGGER.info("Mail is not enabled, would have sent from: " + inviter.getName() + " to: " + invitee.getName()
                + " for sandbox: " + sandbox.getName() + " for invitation ID: " + invitationId);
    }

    @Override
    public void sendExportNotificationEmail(User user, URL sandboxExportFile, String sandboxName) {
        LOGGER.info("Mail is not enabled, would have sent export notification to " + user.getName());
    }

    @Override
    public void sendImportErrorNotificationEmail(User user, String sandboxName) {
        LOGGER.info("Mail is not enabled, would have sent export notification to " + user.getName());
    }
}
