package com.gucci.blog_service.kafka.producer;

import com.gucci.blog_service.kafka.dto.NewPostCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BlogEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "post.created";

    public void publishNewPostEvent(NewPostCreatedEvent message) {
        log.info("Kafka 새 글 생성 이벤트 발행: {}", message);
        kafkaTemplate.send(TOPIC, message);
    }
}
