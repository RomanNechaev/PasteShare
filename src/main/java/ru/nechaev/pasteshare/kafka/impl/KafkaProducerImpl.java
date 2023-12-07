package ru.nechaev.pasteshare.kafka.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.nechaev.pasteshare.entitity.PasteInfo;
import ru.nechaev.pasteshare.kafka.KafkaProducer;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaProducerImpl implements KafkaProducer {
    private final KafkaTemplate<String, PasteInfo> kafkaSender;
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerImpl.class);
    private final String topic;

    public KafkaProducerImpl(KafkaTemplate<String, PasteInfo> kafkaSender, @Value("${spring.kafka.template.default-topic}") String topic) {
        this.kafkaSender = kafkaSender;
        this.topic = topic;
    }

    @Override
    public void produce(PasteInfo pasteInfo) {
        CompletableFuture<SendResult<String, PasteInfo>> future = kafkaSender.send(topic, pasteInfo);
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                LOGGER.info("Sent pasteInfo with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                LOGGER.warn("Unable to send pasteInfo due to : " + exception.getMessage());
            }
        });
    }
}
