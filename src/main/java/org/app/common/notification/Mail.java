package org.app.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class Mail {

    private final JavaMailSender mailSender;

    /**
     * Sends an email synchronously.
     *
     * @param from    Sender email address
     * @param to      Recipient email address
     * @param subject Email subject
     * @param content Email content (HTML supported)
     * @return true if sent successfully, false otherwise
     */
    public boolean sendMail(String from, String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, 
                StandardCharsets.UTF_8.name());
            
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true enables HTML content

            mailSender.send(message);
            return true;
        } catch (MessagingException ex) {
            log.error("Error sending email", ex);
            return false;
        }
    }

    /**
     * Sends an email asynchronously.
     *
     * @param from    Sender email address
     * @param to      Recipient email address
     * @param subject Email subject
     * @param content Email content (HTML supported)
     * @return CompletableFuture containing the send result
     */
    public CompletableFuture<Boolean> sendMailAsync(String from, String to, 
            String subject, String content) {
        return CompletableFuture.supplyAsync(() -> 
            sendMail(from, to, subject, content)
        );
    }

    /**
     * Sends an email with attachments synchronously.
     *
     * @param from        Sender email address
     * @param to          Recipient email address
     * @param subject     Email subject
     * @param content     Email content (HTML supported)
     * @param attachments Array of byte arrays representing attachments
     * @param fileNames   Array of attachment file names
     * @return true if sent successfully, false otherwise
     */
    public boolean sendMailWithAttachments(String from, String to, String subject, 
            String content, byte[][] attachments, String[] fileNames) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            // Add attachments
            for (int i = 0; i < attachments.length; i++) {
                fillMail(helper, attachments[i], fileNames[i]);
            }

            mailSender.send(message);
            return true;
        } catch (MessagingException ex) {
            log.error("Error sending email with attachments", ex);
            return false;
        }
    }

    private static void fillMail(MimeMessageHelper helper, byte[] attachment, String fileNames) throws MessagingException {
        helper.addAttachment(fileNames, () -> new ByteArrayInputStream(attachment));
    }

    /**
     * Sends an email with attachments asynchronously.
     *
     * @param from        Sender email address
     * @param to          Recipient email address
     * @param subject     Email subject
     * @param content     Email content (HTML supported)
     * @param attachments Array of byte arrays representing attachments
     * @param fileNames   Array of attachment file names
     * @return CompletableFuture containing the send result
     */
    public CompletableFuture<Boolean> sendMailWithAttachmentsAsync(String from, 
            String to, String subject, String content, byte[][] attachments, 
            String[] fileNames) {
        return CompletableFuture.supplyAsync(() -> 
            sendMailWithAttachments(from, to, subject, content, attachments, fileNames)
        );
    }
}