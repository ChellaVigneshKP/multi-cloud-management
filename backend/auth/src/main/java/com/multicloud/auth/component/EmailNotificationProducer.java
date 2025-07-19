package com.multicloud.auth.component;


import com.multicloud.commonlib.email.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProducer {
    private final String outputBinding;
    private final StreamBridge streamBridge;
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationProducer.class);

    @Autowired
    public EmailNotificationProducer(
            StreamBridge streamBridge,
            @Value("${app.messaging.email-notification-binding}") String outputBinding) {
        this.streamBridge = streamBridge;
        this.outputBinding = outputBinding;
    }

    public void sendEmailNotification(EmailNotification notification) {
        boolean sent = streamBridge.send(outputBinding, notification);
        logger.info("Message sent to {}: {}", outputBinding, sent);
    }
}