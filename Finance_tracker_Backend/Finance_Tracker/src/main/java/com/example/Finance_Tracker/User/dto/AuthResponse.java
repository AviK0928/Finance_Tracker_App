package com.example.Finance_Tracker.User.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    /** JWT access token; the Android client reads this field as {@code token}. */
    @NotBlank
    private String token;

    @NotBlank
    @Email
    private String email;
}
