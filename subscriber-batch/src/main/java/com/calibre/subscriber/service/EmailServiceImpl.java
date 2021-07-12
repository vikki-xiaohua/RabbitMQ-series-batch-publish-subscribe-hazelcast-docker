package com.calibre.subscriber.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

@Slf4j
@Service
public class EmailServiceImpl implements IEmailService {
    @Value("${mail.host}")
    private String host;

    @Value("${mail.smtp.port}")
    private String port;

    @Value("${mail.user}")
    private String userName;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.mail-to}")
    private String toAddress;

    @Value("${mail.smtp.auth}")
    private String smtpAuth;


    @Async("email-service-taskExecutor")
    @Override
    public void sendEmail(String subject, String message, String[] attachedFiles) throws MessagingException, IOException, InterruptedException {
        log.info("sendEmail subject:{},  message: {}, attachedFiles:{}, thread id: {}", subject, message, Arrays.toString(attachedFiles), Thread.currentThread().getId());
        if (StringUtils.isAnyBlank(subject, message)) return;

        Properties properties = buildProperties();
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };

        try {
            Thread.sleep(3000);
            Message msg = buildMessageContent(subject, message, attachedFiles, properties, auth);
            Transport.send(msg);
        } catch (MessagingException | IOException | InterruptedException e) {
            log.error("sendEmail MessagingException : {}", Arrays.toString(e.getStackTrace()));
            throw e;
        }

    }

    private Message buildMessageContent(String subject, String message, String[] attachedFiles,
                                        Properties properties, Authenticator auth) throws MessagingException, IOException {
        Session session = Session.getInstance(properties, auth);
        Message mimeMessage = new MimeMessage(session);

        mimeMessage.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
        mimeMessage.setRecipients(Message.RecipientType.TO, toAddresses);

        mimeMessage.setSubject(subject);
        mimeMessage.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, MediaType.TEXT_HTML_VALUE);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        if (ArrayUtils.isNotEmpty(attachedFiles)) {
            for (String filePath : attachedFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(filePath);
                multipart.addBodyPart(attachPart);
            }
        }

        mimeMessage.setContent(multipart);

        return mimeMessage;
    }

    private Properties buildProperties() {
        Properties properties = new Properties();
        properties.put("mail.host", host);
        properties.put("mail.smtp.port", port);
        properties.setProperty("mail.smtp.auth", smtpAuth);
        properties.put("mail.user", userName);
        properties.put("mail.password", password);

        return properties;
    }
}