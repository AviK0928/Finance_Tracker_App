package com.example.Finance_Tracker.Budget;

import com.example.Finance_Tracker.Budget.dto.BudgetCreateDTO;
import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetCreateDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static Set<String> invalidFields(LocalDate start, LocalDate end) {
        BudgetCreateDTO dto = BudgetCreateDTO.builder()
                .name("October")
                .amount(new BigDecimal("1000.00"))
                .startDate(start)
                .endDate(end)
                .frequency(BudgetFrequency.MONTHLY)
                .build();
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void endDateBeforeStartDate_isRejected() {
        assertThat(invalidFields(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1)))
                .contains("dateRangeValid");
    }

    @Test
    void amountWithThreeDecimals_isRejected_likeTransactions() {
        BudgetCreateDTO dto = BudgetCreateDTO.builder()
                .name("October")
                .amount(new BigDecimal("1000.125"))
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2026, 10, 31))
                .frequency(BudgetFrequency.MONTHLY)
                .build();

        assertThat(validator.validate(dto))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Amount must have at most 2 decimal places");
    }

    @Test
    void singleDayBudget_isValid() {
        assertThat(invalidFields(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 1)))
                .isEmpty();
    }
}
