package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionListener {

    private final UserRepository userRepository;

    public KafkaTransactionListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "test-topic")
    public void listen(Transaction transaction) {
        System.out.println("🟢 Received transaction: " + transaction);

        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender == null || recipient == null) {
            System.out.println("❌ Invalid sender or recipient. Skipping transaction.");
            return;
        }

        if (sender.getBalance() < transaction.getAmount()) {
            System.out.println("❌ Insufficient balance for sender. Skipping transaction.");
            return;
        }

        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());

        userRepository.save(sender);
        userRepository.save(recipient);

        System.out.println("✅ Transaction processed successfully.");
    }
}
