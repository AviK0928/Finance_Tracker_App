package com.example.Finance_Tracker.Budget.exception;

public class UnauthorizedBudgetAccessException extends RuntimeException {
    public UnauthorizedBudgetAccessException(String message) {
        super(message);
    }
}