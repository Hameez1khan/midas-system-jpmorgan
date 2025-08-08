package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KafkaTransactionListener {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public KafkaTransactionListener(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "test-topic")
    public void listen(Transaction transaction) {
        System.out.println("🟢 Received transaction: " + transaction);

        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender == null || recipient == null) {
            System.out.println("Invalid sender or recipient. Skipping transaction.");
            return;
        }
        if (sender.getBalance() < transaction.getAmount()) {
            System.out.println("Insufficient balance for sender. Skipping transaction.");
            return;
        }

        // Call Incentive API
        String url = "http://localhost:8080/incentive";
        Incentive response = restTemplate.postForObject(url, transaction, Incentive.class);
        float incentiveAmount = response != null ? response.getAmount() : 0.0f;

        // Apply transaction and incentive
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        userRepository.save(sender);
        userRepository.save(recipient);

        System.out.println("Transaction processed with incentive: " + incentiveAmount);
    }
}
