package com.example.Finance_Tracker.User;

import com.example.Finance_Tracker.User.dto.PasswordRules;
import com.example.Finance_Tracker.User.dto.RegisterRequest;
import com.example.Finance_Tracker.User.dto.ResetPasswordRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordRulesValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /** Meets the pattern (lower, upper, digit, special); padded to [length] characters. */
    private static String password(int length) {
        return "Aa1@" + "a".repeat(length - 4);
    }

    private static RegisterRequest register(String password) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("someone");
        request.setEmail("someone@example.com");
        request.setPassword(password);
        return request;
    }

    @Test
    void register_72Characters_isAccepted() {
        assertThat(validator.validate(register(password(PasswordRules.MAX_LENGTH)))).isEmpty();
    }

    @Test
    void register_73Characters_isRejected_becauseBcryptIgnoresTheRest() {
        assertThat(validator.validate(register(password(PasswordRules.MAX_LENGTH + 1))))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly(PasswordRules.LENGTH_MESSAGE);
    }

    @Test
    void reset_73Characters_isRejected_too() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("token");
        request.setNewPassword(password(PasswordRules.MAX_LENGTH + 1));

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly(PasswordRules.LENGTH_MESSAGE);
    }
}
