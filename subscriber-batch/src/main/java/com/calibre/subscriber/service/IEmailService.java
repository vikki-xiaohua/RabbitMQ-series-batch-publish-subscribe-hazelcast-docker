package com.calibre.subscriber.service;

import javax.mail.MessagingException;
import java.io.IOException;

public interface IEmailService {
    void sendEmail(String subject, String message, String[] attachFiles) throws MessagingException, IOException, InterruptedException;
}
