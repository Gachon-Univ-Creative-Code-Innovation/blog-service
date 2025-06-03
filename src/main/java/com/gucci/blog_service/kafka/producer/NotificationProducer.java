package com.gucci.blog_service.kafka.producer;

import com.gucci.blog_service.kafka.dto.NotificationKafkaRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationKafkaRequest> kafkaTemplate;
    private static final String TOPIC_NAME = "alarm-topic";

    public void sendNotification(NotificationKafkaRequest message) {
        log.info("Kafka 메시지 전송 (blog-service): {}", message);
        kafkaTemplate.send(TOPIC_NAME, message);
    }
}
