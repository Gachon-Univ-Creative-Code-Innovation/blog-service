package com.gucci.blog_service.kafka.producer;

import com.gucci.blog_service.kafka.dto.NewCommentCreatedEvent;
import com.gucci.blog_service.kafka.dto.NewPostCreatedEvent;
import com.gucci.blog_service.kafka.dto.NewReplyCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BlogEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishNewPostEvent(NewPostCreatedEvent message) {
        log.info("Kafka 새 글 생성 이벤트 발행: {}", message);
        kafkaTemplate.send("post.created", message);
    }

    public void publishNewCommentEvent(NewCommentCreatedEvent message) {
        log.info("Kafka 댓글 생성 이벤트 발행: {}", message);
        kafkaTemplate.send("comment.created", message);
    }

    public void publishNewReplyEvent(NewReplyCreatedEvent message) {
        log.info("Kafka 답글 생성 이벤트 발행: {}", message);
        kafkaTemplate.send("reply.created", message);
    }
}
