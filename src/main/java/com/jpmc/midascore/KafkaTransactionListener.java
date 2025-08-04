package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionListener {
    @KafkaListener(topics = "test-topic")
    public void listen(Transaction transaction) {
        System.out.println("🟢 Received transaction: " + transaction);
    }

}
