package ru.nechaev.pasteshare.kafka;

import ru.nechaev.pasteshare.entitity.PasteInfo;

public interface KafkaProducer {
    void produce(PasteInfo pasteInfo);
}
