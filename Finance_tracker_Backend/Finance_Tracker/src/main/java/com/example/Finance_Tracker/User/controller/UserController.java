package com.example.Finance_Tracker.User.controller;

import com.example.Finance_Tracker.User.dto.AuthResponse;
import com.example.Finance_Tracker.User.dto.ForgotPasswordRequest;
import com.example.Finance_Tracker.User.dto.MessageResponse;
import com.example.Finance_Tracker.User.dto.ResetPasswordRequest;
import com.example.Finance_Tracker.User.dto.LoginRequest;
import com.example.Finance_Tracker.User.dto.RegisterRequest;
import com.example.Finance_Tracker.User.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest request){
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request){
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.initiateForgotPassword(request);
        return ResponseEntity.ok(new MessageResponse("If the email exists, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Password has been successfully reset."));
    }

}