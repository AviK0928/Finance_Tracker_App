package com.example.Finance_Tracker.Sync.util;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Transaction.entity.Transaction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static String computeBudgetHash(Budget budget) {
        String raw = budget.getAmount().toPlainString()
                + budget.getStartDate().toString()
                + budget.getEndDate().toString()
                + budget.getFrequency().name()
                + budget.getStatus().name()
                + (budget.getNotes() != null ? budget.getNotes() : "")
                + budget.getUpdatedAt().toString();

        return sha256(raw);
    }

    public static String computeTransactionHash(Transaction tx) {
        String raw = tx.getAmount()
                + tx.getType().name()
                + tx.getCategory()
                + (tx.getDescription() != null ? tx.getDescription() : "")
                + tx.getTransactionDate().toString()
                + tx.getUpdatedAt().toString();

        return sha256(raw);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error computing hash", e);
        }
    }
}