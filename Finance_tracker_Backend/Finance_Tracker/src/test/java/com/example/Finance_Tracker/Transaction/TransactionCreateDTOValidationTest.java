package com.example.Finance_Tracker.Transaction;

import com.example.Finance_Tracker.Transaction.dto.TransactionCreateDTO;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionCreateDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static Set<String> invalidFields(String amount) {
        TransactionCreateDTO dto = new TransactionCreateDTO(
                "Rent", TransactionType.EXPENSE, new BigDecimal(amount),
                LocalDateTime.of(2026, 10, 1, 10, 0), "October rent", null);
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "10.5", "12000.00", "99999999999999999.99"})
    void validAmounts_passValidation(String amount) {
        assertThat(invalidFields(amount)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "10.999", "0.001"})
    void invalidAmounts_areRejected(String amount) {
        assertThat(invalidFields(amount)).contains("amount");
    }
}
