package org.hspconsorotium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.impl.EmailServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.hspconsortium.platform.messaging.model.mail.Message;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailServiceTest {

    private JavaMailSender mailSender = mock(JavaMailSender.class);

    private SpringTemplateEngine templateEngine = mock(SpringTemplateEngine.class);

    private EmailServiceImpl emailService = new EmailServiceImpl();

//    private Properties props = mock(Properties.class);
//    private Authenticator authenticator = mock(Authenticator.class);
//    private Session session = mock(Session.class);
    private MimeMessage mimeMessage = mock(MimeMessage.class);

    private Message message;
    private Sandbox sandbox;
    private User inviter;
    private User invitee;

    @Before
    public void setup() {
        invitee = new User();
        invitee.setName("me");
        invitee.setEmail("my@email.com");
        message = new Message(true, Message.ENCODING);
        message.addRecipient(invitee.getName(), invitee.getEmail());
    }

//    @Test
//    public void sendEmailByJavaMailTest() throws MessagingException {
//        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
//        when(templateEngine.process(any(), any())).thenReturn("htmlContent");
////        doNothing().when(mailSender.send(any()));
//        emailService.sendEmailByJavaMail(message);
//    }
}
