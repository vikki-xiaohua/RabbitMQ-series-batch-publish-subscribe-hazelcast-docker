package com.calibre.publisher.service;

public interface IEmailService {
    void sendEmail(String subject, String message, String[] attachFiles);
}
