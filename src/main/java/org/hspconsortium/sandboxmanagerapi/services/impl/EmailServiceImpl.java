package org.hspconsortium.sandboxmanagerapi.services.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.platform.messaging.model.mail.Message;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.User;
import org.hspconsortium.sandboxmanagerapi.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@Profile("email")
public class EmailServiceImpl implements EmailService {
    private static Logger LOGGER = LoggerFactory.getLogger(EmailService.class.getName());

    private static final String HSPC_EMAIL = "no-reply@hspconsortium.org";
    private static final String PNG_MIME = "image/png";
    private static final String EMAIL_SUBJECT = "HSPC Sandbox Invitation";
    private static final String HSPC_LOGO_IMAGE = "templates\\hspc-sndbx-logo.png";

    @Value("${hspc.platform.messaging.emailSenderEndpointURL}")
    private String emailSenderEndpointURL;

    @Value("${hspc.platform.messaging.sendEmail}")
    private boolean sendEmail;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Override
    public void sendEmail(User inviter, User invitee, Sandbox sandbox) throws IOException {
        if (sendEmail) {

            Message message = new Message(true, Message.ENCODING);

            message.setSubject(EMAIL_SUBJECT);
            message.setAcceptHtmlMessage(true);

            message.setSenderName(inviter.getName());
            message.setSenderEmail(HSPC_EMAIL);
            message.addRecipient(invitee.getName(), invitee.getEmail());

            message.setTemplateFormat(Message.TemplateFormat.HTML);

            if (inviter.getName() != null) {
                message.addVariable("inviter", inviter.getName());
            } else {
                message.addVariable("inviter", inviter.getEmail());
            }
            if (invitee.getName() != null) {
                message.addVariable("invitee", invitee.getName());
            } else {
                message.addVariable("invitee", invitee.getEmail());
            }
            message.addVariable("sandboxName", sandbox.getName());
            message.addVariable("inviteeEmail", invitee.getEmail());

            // Add the inline images, referenced from the HTML code as "cid:image-name"
            message.addResource("hspc-logo", PNG_MIME, getImageFile(HSPC_LOGO_IMAGE, "png"));
//            sendEmailToMessaging(message);
            try {
                sendEmailByJavaMail(message);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendEmailToMessaging(Message message) throws IOException {
        String url = this.emailSenderEndpointURL;

        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader("Content-Type", "application/json");
        postRequest.addHeader("Accept", "application/json");

        postRequest.setEntity(new StringEntity(toJson(message)));
//        postRequest.setHeader("Authorization", "BEARER " + oAuthUserService.getBearerToken(request));

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                String errorMsg = String.format("There was a problem sending the email.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

        } catch (IOException e) {
            LOGGER.error("Error posting to {}", url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient", e);
            }
        }
    }

    public void sendEmailByJavaMail(Message emailMessage)
            throws MessagingException {

        for (Message.Recipient recipient : emailMessage.getRecipients()) {
            final Context ctx = new Context(recipient.getLocale());
            ctx.setVariables(emailMessage.getVariable());

            // Prepare messageHelper using a Spring helper
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper messageHelper
                    = new MimeMessageHelper(mimeMessage, emailMessage.isMultipart(), emailMessage.getEncoding());

            messageHelper.setSubject(emailMessage.getSubject());
            messageHelper.setFrom(emailMessage.getSenderEmail());
            messageHelper.setTo(recipient.getEmail());
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
            final String htmlContent = this.templateEngine.process("email-sandbox-invite", ctx);
            messageHelper.setText(htmlContent, true /* isHtml */);

            // Send email
            try {
                this.mailSender.send(mimeMessage);
            } catch (Exception e) {
                LOGGER.error("Error sending email message", e);
            }

        }

    }

    private static String toJson(Message message) {
        Gson gson = new Gson();
        Type type = new TypeToken<Message>() {
        }.getType();
        return gson.toJson(message, type);
    }

    private byte[] getImageFile(String pathName, String imageType) throws IOException {
        BufferedImage img;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ClassPathResource cpr = new ClassPathResource(pathName);
        ImageIO.setUseCache(false);
        img = ImageIO.read(cpr.getInputStream());
        ImageIO.write(img, imageType, baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }

    private byte[] getFile(String pathName) throws IOException {
        ClassPathResource cpr = new ClassPathResource(pathName);
        return IOUtils.toByteArray(cpr.getInputStream());
    }

}


