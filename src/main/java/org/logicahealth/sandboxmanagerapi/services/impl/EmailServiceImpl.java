package org.logicahealth.sandboxmanagerapi.services.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.hspconsortium.platform.messaging.model.mail.Message;
import org.logicahealth.sandboxmanagerapi.metrics.PublishAtomicMetric;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.User;
import org.logicahealth.sandboxmanagerapi.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.UUID;

@Service
@Profile("email")
public class EmailServiceImpl implements EmailService {
    private static Logger LOGGER = LoggerFactory.getLogger(EmailService.class.getName());

    private static final String HSPC_EMAIL = "noreply@interop.community";
    private static final String PNG_MIME = "image/png";
    private static final String EMAIL_SUBJECT = "Interop Community Sandbox Invitation";
    private static final String HSPC_LOGO_IMAGE = "templates\\hspc-sndbx-logo.png";

    @Value("${hspc.platform.messaging.sendEmail}")
    private boolean sendEmail;

    @Value("${hspc.platform.frontend}")
    private String baseURL;

    @Value("${hspc.platform.content-server.logoUrl}")
    private String logoUrl;

    private JavaMailSender mailSender;
    private SpringTemplateEngine templateEngine;

    @Inject
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Inject
    public void setTemplateEngine(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    @PublishAtomicMetric
    public void sendEmail(User inviter, User invitee, Sandbox sandbox, int invitationId) throws IOException {
        
        LOGGER.info("sendEmails");

        if (sendEmail) {

            Message message = new Message(true, Message.ENCODING);

            message.setSubject(EMAIL_SUBJECT);
            message.setAcceptHtmlMessage(true);

            message.setSenderName(inviter.getName());
            message.setSenderEmail(HSPC_EMAIL);
            message.addRecipient(invitee.getName(), invitee.getEmail().trim());

            message.setTemplateFormat(Message.TemplateFormat.HTML);

            if (inviter.getName() != null) {
                message.addVariable("inviter", inviter.getName());
            } else {
                message.addVariable("inviter", inviter.getEmail().trim());
            }
            if (invitee.getName() != null) {
                message.addVariable("invitee", invitee.getName());
            } else {
                message.addVariable("invitee", invitee.getEmail().trim());
            }
            message.addVariable("logoUrl", logoUrl);
            message.addVariable("sandboxName", sandbox.getName());
            message.addVariable("inviteeEmail", invitee.getEmail().trim());
            message.addVariable("invitationId", invitationId);
            message.addVariable("baseURL", baseURL);

            // Add the inline images, referenced from the HTML code as "cid:image-name"
            message.addResource("hspc-logo", PNG_MIME, getImageFile(HSPC_LOGO_IMAGE, "png"));
//            sendEmailToMessaging(message);
            try {
                sendEmailByJavaMail(message, "email-sandbox-invite");
            } catch (MessagingException e) {
                e.printStackTrace();
                throw new RuntimeException(e + " Email was not sent");
            }
        }

        LOGGER.debug("sendEmails: "
        +"Parameters: inviter = "+inviter+", invitee = "+invitee+", sandbox = "+sandbox+", invitationId = "+invitationId
        +"; No return value");

    }

    @Override
    public void sendExportNotificationEmail(User user, URL sandboxExportFile, String sandboxName) {

        LOGGER.info("sendExportNotificationEmail");

        if (sendEmail) {

            Message message = new Message(true, Message.ENCODING);

            message.setSubject(sandboxName + " Sandbox export is now available for download!");
            message.setAcceptHtmlMessage(true);

            message.setSenderEmail(HSPC_EMAIL);
            message.addRecipient(user.getName(), user.getEmail().trim());

            message.setTemplateFormat(Message.TemplateFormat.HTML);
            message.addVariable("logoUrl", logoUrl);
            message.addVariable("sandboxName", sandboxName);
            message.addVariable("s3resource", sandboxExportFile.toString());

            try {
                sendEmailByJavaMail(message, "email-sandbox-export");
            } catch (MessagingException e) {
                e.printStackTrace();
                throw new RuntimeException(e + " Email was not sent");
            }
        }

        LOGGER.debug("sendExportNotificationEmail: "
        +"Parameters: user = "+user+", sandboxExportFile = "+sandboxExportFile+", sandboxName = "+sandboxName
        +"; No return value");

    }

    @Override
    public void sendImportErrorNotificationEmail(User user, String sandboxName) {
        
        LOGGER.info("sendImportErrorNotificationEmail");

        if (sendEmail) {

            Message message = new Message(true, Message.ENCODING);

            message.setSubject(sandboxName + " Sandbox import failed!");
            message.setAcceptHtmlMessage(true);

            message.setSenderEmail(HSPC_EMAIL);
            message.addRecipient(user.getName(), user.getEmail().trim());

            message.setTemplateFormat(Message.TemplateFormat.HTML);
            message.addVariable("logoUrl", logoUrl);
            message.addVariable("sandboxName", sandboxName);

            try {
                sendEmailByJavaMail(message, "email-sandbox-import-error");
            } catch (MessagingException e) {
                e.printStackTrace();
                throw new RuntimeException(e + " Email was not sent");
            }
        }

        LOGGER.debug("sendImportErrorNotificationEmail: "
        +"Parameters: user = "+user+", sandboxName = "+sandboxName+"; No return value");

    }

    public void sendEmailByJavaMail(Message emailMessage, String templateName)
            throws MessagingException {

        LOGGER.info("sendEmailByJavaMail");

        for (Message.Recipient recipient : emailMessage.getRecipients()) {
            final Context ctx = new Context(recipient.getLocale());
            ctx.setVariables(emailMessage.getVariable());

            // Prepare messageHelper using a Spring helper
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper messageHelper
                    = new MimeMessageHelper(mimeMessage, emailMessage.isMultipart(), emailMessage.getEncoding());

            messageHelper.setSubject(emailMessage.getSubject());
            messageHelper.setFrom(emailMessage.getSenderEmail().trim());
            messageHelper.setTo(recipient.getEmail().trim());
            if (emailMessage.isAuditEnabled() && (recipient.getReplyTo() == null)) {
                messageHelper.setReplyTo(UUID.randomUUID().toString().toLowerCase() + "@" + emailMessage.getSenderEmail().split("@")[1]);
            } else if (recipient.getReplyTo() != null) {
                messageHelper.setReplyTo(recipient.getReplyTo());
            }
            if (emailMessage.getAttachments() != null) {
                for (Message.Resource attachment : emailMessage.getAttachments()) {
                    // Add the attachment
                    final InputStreamSource attachmentSource = new ByteArrayResource(attachment.getContent());
                    messageHelper.addAttachment(
                            attachment.getContentName(), attachmentSource, attachment.getContentType());
                }
            }

            if (emailMessage.getResources() != null) {
                for (final Message.Resource resource : emailMessage.getResources()) {
                    final InputStreamSource imageSource = new ByteArrayResource(resource.getContent());
                    messageHelper.addInline(resource.getContentName(), imageSource, resource.getContentType());
                }
            }

            // Create the HTML body using Thymeleaf
            final String htmlContent = this.templateEngine.process(templateName, ctx);
            messageHelper.setText(htmlContent, true /* isHtml */);

            // Send email
            try {
                this.mailSender.send(mimeMessage);
            } catch (Exception e) {
                LOGGER.error("Error sending email message", e);
            }

        }

        LOGGER.debug("sendEmailByJavaMail: "
        +"Parameters: emailMessage = "+emailMessage+", templateName = "+templateName
        +"No return value");

    }

    private static String toJson(Message message) {
        
        LOGGER.info("toJson");

        Gson gson = new Gson();
        Type type = new TypeToken<Message>() {
        }.getType();

        LOGGER.debug("toJson: "
        +"Parameters: message = "+message+"; Return value = "+gson.toJson(message, type));

        return gson.toJson(message, type);
    }

    private byte[] getImageFile(String pathName, String imageType) throws IOException {
        
        LOGGER.info("getImageFile");

        BufferedImage img;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ClassPathResource cpr = new ClassPathResource(pathName);
        ImageIO.setUseCache(false);
        img = ImageIO.read(cpr.getInputStream());
        ImageIO.write(img, imageType, baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        LOGGER.debug("getImageFile: "
        +"Parameters: pathName = "+pathName+", imageType = "+imageType
        +"Return value = "+imageInByte);

        return imageInByte;
    }

    private byte[] getFile(String pathName) throws IOException {
        
        LOGGER.info("getFile");

        ClassPathResource cpr = new ClassPathResource(pathName);

        LOGGER.debug("getFile: "
        +"Parameters: pathName = "+pathName+"; Return value = "+IOUtils.toByteArray(cpr.getInputStream()));

        return IOUtils.toByteArray(cpr.getInputStream());
    }

}


