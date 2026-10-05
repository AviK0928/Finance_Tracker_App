package com.example.Finance_Tracker.Core.exception;

/** Generic 404 for resources that don't exist (or don't exist for the current user). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
