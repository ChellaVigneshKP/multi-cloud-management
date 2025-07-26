package com.multicloud.notificationservice.service;

import com.multicloud.commonlib.email.EmailNotification;
import com.multicloud.commonlib.email.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {
    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender emailSender, SpringTemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(EmailNotification notification) throws MessagingException {
        EmailRequest request = notification.getEmailRequest();
        Context context = new Context();
        request.populateContext(context);
        String htmlContent = templateEngine.process(notification.getTemplateName(), context);
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }
}
