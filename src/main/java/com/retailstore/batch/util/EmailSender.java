package com.retailstore.batch.util;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailSender {

    @Value("${report.email.to}")
    private String reportEmail;

    @Autowired
    private JavaMailSender mailSender;

    public void sendReport(String subject, String body, File attachment) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(reportEmail);
        helper.setSubject(subject);
        helper.setText(body);
        helper.addAttachment(attachment.getName(), attachment);

        mailSender.send(message);
    }
}
