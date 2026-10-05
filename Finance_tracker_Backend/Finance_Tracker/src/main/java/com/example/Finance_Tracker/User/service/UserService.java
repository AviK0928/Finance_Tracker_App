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
import com.example.Finance_Tracker.User.repository.UserRepository;
import com.example.Finance_Tracker.Security.JWTService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

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

    /** BCrypt hash of a random password, compared against when the email is unknown (see login). */
    private String dummyHash;

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
        // Same exception and message for "unknown email" and "wrong password", so the response doesn't
        // reveal which emails have accounts. An unknown email is still checked against a dummy hash:
        // BCrypt dominates the response time, so skipping it would reveal the same thing through timing.
        Optional<User> maybeUser = userRepository.findByEmail(request.getEmail());
        String hash = maybeUser.map(User::getPassword).orElseGet(this::dummyHash);
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), hash);
        User user = maybeUser.filter(u -> passwordMatches)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail());
    }

    /** Created on first use with the configured encoder, so its cost always matches real hashes. */
    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
        }
        return dummyHash;
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
        // Unknown email: return silently. The controller sends the same
        // "If the email exists..." response either way (no account enumeration).
        Optional<User> maybeUser = userRepository.findByEmail(request.getEmail());
        if (maybeUser.isEmpty()) {
            return;
        }
        User user = maybeUser.get();

        String token = passwordResetService.generateResetToken(user);

        // Sent asynchronously (EmailService is @Async): a known email must answer exactly like an
        // unknown one, without waiting for SMTP and without a mail failure turning into a 500.
        emailService.sendResetPasswordEmail(user.getEmail(), user.getUsername(), token);
    }
}
