package com.example.Finance_Tracker.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JSON body for endpoints whose only result is a human-readable message. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    /** Shown to the user as-is; the Android client reads this field as {@code message}. */
    private String message;
}
