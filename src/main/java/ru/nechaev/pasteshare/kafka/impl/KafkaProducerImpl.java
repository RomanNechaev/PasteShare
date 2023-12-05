package ru.nechaev.pasteshare.kafka.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.nechaev.pasteshare.entitity.PasteInfo;
import ru.nechaev.pasteshare.kafka.KafkaProducer;

@Component
public class KafkaProducerImpl implements KafkaProducer {
    private final KafkaTemplate<String, PasteInfo> kafkaSender;
    private final String topic;

    public KafkaProducerImpl(KafkaTemplate<String, PasteInfo> kafkaSender, @Value("${spring.kafka.template.default-topic}") String topic) {
        this.kafkaSender = kafkaSender;
        this.topic = topic;
    }

    @Override
    public void produce(PasteInfo pasteInfo) {
        kafkaSender.send(topic, pasteInfo);
    }
}
