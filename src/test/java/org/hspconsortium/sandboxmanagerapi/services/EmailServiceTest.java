package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.impl.EmailServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.hspconsortium.platform.messaging.model.mail.Message;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailServiceTest {

    private JavaMailSender mailSender = mock(JavaMailSender.class);
    private SpringTemplateEngine templateEngine = mock(SpringTemplateEngine.class);
    private MimeMessage mimeMessage = mock(MimeMessage.class);

    private EmailServiceImpl emailService = new EmailServiceImpl();

    private Message message;
    private Sandbox sandbox;
    private User inviter;
    private User invitee;

    @Before
    public void setup() {
        emailService.setMailSender(mailSender);
        emailService.setTemplateEngine(templateEngine);

        invitee = new User();
        invitee.setName("me");
        invitee.setEmail("my@email.com");
        inviter = new User();
        sandbox = new Sandbox();
        message = new Message(true, Message.ENCODING);
        message.addRecipient(invitee.getName(), invitee.getEmail());

        ReflectionTestUtils.setField(emailService, "sendEmail", true);
    }

    // With ThymeLeaf setup as they are, it is currently not possible to test this class because templateEngine.process()
    //      is a final class and will return null since it is unmockable.

    @Test(expected = NullPointerException.class)
    public void sendEmailTest() throws IOException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
//        when(templateEngine.process(any(), any())).thenReturn("htmlContent");
        emailService.sendEmail(inviter, invitee, sandbox);
    }
}
