package com.gucci.blog_service.kafka.producer;

import com.gucci.blog_service.kafka.dto.NewCommentCreatedEvent;
import com.gucci.blog_service.kafka.dto.NewPostCreatedEvent;
import com.gucci.blog_service.kafka.dto.NewReplyCreatedEvent;
import com.gucci.blog_service.kafka.dto.NotificationKafkaRequest;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // 필요시 수정
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    @Bean
    public ProducerFactory<String, NewPostCreatedEvent> newPostProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, NewPostCreatedEvent> newPostKafkaTemplate() {
        return new KafkaTemplate<>(newPostProducerFactory());
    }

    @Bean
    public ProducerFactory<String, NewCommentCreatedEvent> newCommentProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, NewCommentCreatedEvent> newCommentKafkaTemplate() {
        return new KafkaTemplate<>(newCommentProducerFactory());
    }

    @Bean
    public ProducerFactory<String, NewReplyCreatedEvent> newReplyProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, NewReplyCreatedEvent> newReplyKafkaTemplate() {
        return new KafkaTemplate<>(newReplyProducerFactory());
    }

    @Bean
    public ProducerFactory<String, NotificationKafkaRequest> notificationProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, NotificationKafkaRequest> notificationKafkaTemplate() {
        return new KafkaTemplate<>(notificationProducerFactory());
    }
}
