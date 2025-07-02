package com.example.Finance_Tracker.User.service;

import com.example.Finance_Tracker.Core.mail.EmailService;
import com.example.Finance_Tracker.User.dto.AuthResponse;
import com.example.Finance_Tracker.User.dto.ForgotPasswordRequest;
import com.example.Finance_Tracker.User.dto.LoginRequest;
import com.example.Finance_Tracker.User.dto.RegisterRequest;
import com.example.Finance_Tracker.User.entity.PasswordResetToken;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.exception.EmailAlreadyExistsException;
import com.example.Finance_Tracker.User.exception.InvalidCredentialsException;
import com.example.Finance_Tracker.User.exception.UserNotFoundException;
import com.example.Finance_Tracker.User.repository.UserRepository;
import com.example.Finance_Tracker.Security.JWTService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private PasswordResetService passwordResetService;
    @Autowired
    private EmailService emailService;

    public AuthResponse register(RegisterRequest request){
        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException("User already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        // Optionally generate JWT on registration
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        System.out.println("Received login request:");
        System.out.println("Email = " + request.getEmail());
        System.out.println("Password = " + request.getPassword());
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and Password must not be null.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Invalid Email or Password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Email or Password");
        }
        System.out.println("Authenticated user: " + user.getUsername());
        System.out.println("Email for JWT: " + user.getEmail());
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail());
    }
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetService.validateToken(token);
        if (resetToken == null) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetService.markTokenAsUsed(resetToken);
    }


    public void initiateForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("No account with that email."));

        String token = passwordResetService.generateResetToken(user);
        String resetLink = "https://yourfrontend.com/reset-password?token=" + token;

        emailService.sendResetPasswordEmail(user.getEmail(), user.getUsername(), resetLink);
    }
}
