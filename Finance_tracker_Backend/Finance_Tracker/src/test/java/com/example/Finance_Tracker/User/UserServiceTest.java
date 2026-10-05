package com.example.Finance_Tracker.User;

import com.example.Finance_Tracker.Core.mail.EmailService;
import com.example.Finance_Tracker.Security.JWTService;
import com.example.Finance_Tracker.User.dto.AuthResponse;
import com.example.Finance_Tracker.User.dto.ForgotPasswordRequest;
import com.example.Finance_Tracker.User.dto.LoginRequest;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.exception.InvalidCredentialsException;
import com.example.Finance_Tracker.User.repository.UserRepository;
import com.example.Finance_Tracker.User.service.PasswordResetService;
import com.example.Finance_Tracker.User.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "user@example.com";

    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JWTService jwtService;
    @Mock private PasswordResetService passwordResetService;
    @Mock private EmailService emailService;

    @InjectMocks private UserService userService;

    private static LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private static User existingUser() {
        return User.builder().id(1L).email(EMAIL).username("user").password("stored-hash").build();
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginRequest("nobody@example.com", "Secret1!")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_unknownEmail_stillRunsAPasswordCheck() {
        // Without it an unknown email answers much faster than a wrong password (timing side channel)
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(loginRequest("nobody@example.com", "Secret1!")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(passwordEncoder).matches(eq("Secret1!"), any());
    }

    @Test
    void login_wrongPassword_throwsSameExceptionAsUnknownEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches("wrong", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(loginRequest(EMAIL, "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_correctPassword_returnsToken() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches("Secret1!", "stored-hash")).thenReturn(true);
        when(jwtService.generateToken(EMAIL)).thenReturn("jwt-token");

        AuthResponse response = userService.login(loginRequest(EMAIL, "Secret1!"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void forgotPassword_unknownEmail_returnsSilentlyWithoutSendingEmail() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nobody@example.com");

        assertThatCode(() -> userService.initiateForgotPassword(request)).doesNotThrowAnyException();
        verifyNoInteractions(passwordResetService, emailService);
    }

    @Test
    void forgotPassword_knownEmail_emailsTheResetCode() {
        User user = existingUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordResetService.generateResetToken(user)).thenReturn("tok-123");
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(EMAIL);

        userService.initiateForgotPassword(request);

        verify(emailService).sendResetPasswordEmail(EMAIL, "user", "tok-123");
    }
}
