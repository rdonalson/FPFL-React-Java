package com.financialplanner.moduleapi.dtos;

import com.financialplanner.moduleapi.dtos.auth.ChangePasswordRequest;
import com.financialplanner.moduleapi.dtos.auth.LoginRequest;
import com.financialplanner.moduleapi.dtos.auth.RegisterRequest;
import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountRequest;
import com.financialplanner.moduleapi.dtos.item.ItemRequest;
import com.financialplanner.moduleapi.dtos.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dtos.itemtype.UpdateItemTypeNameRequest;
import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodRequest;
import com.financialplanner.moduleapi.dtos.timeperiod.UpdateTimePeriodNameRequest;
import com.financialplanner.modulecommonbc.exception.DomainValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** The rules request bodies enforce, both in their constructors and through Bean Validation. */
class RequestValidationTest {

    private static final UUID USER = UUID.randomUUID();
    private static jakarta.validation.ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void start() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void stop() {
        factory.close();
    }

    private static Set<String> messages(Object request) {
        return validator.validate(request).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    private static ItemRequest item(UUID user, String name, Double amount, Integer type, Integer period) {
        return new ItemRequest(user, name, amount, type, period, null, null, null, null, null, null, 15, null, null, null,
                               null, null, null, null, null, null, null, null, null, null, null, null, null, false);
    }

    @Test
    void itemRequestRules() {
        assertDoesNotThrow(() -> item(USER, "Rent", 1600.0, 2, 6));
        assertDoesNotThrow(() -> item(USER, "IA", 0.01, 3, null)); // the initial amount needs no period
        assertEquals("Item UserId is required and cannot be blank", assertThrows(DomainValidationException.class, () -> item(null, "Rent", 1.0, 2, 6)).getMessage());
        assertThrows(DomainValidationException.class, () -> item(USER, " ", 1.0, 2, 6));
        assertThrows(DomainValidationException.class, () -> item(USER, "Rent", 0.0, 2, 6));
        assertThrows(DomainValidationException.class, () -> item(USER, "Rent", null, 2, 6));
        assertThrows(DomainValidationException.class, () -> item(USER, "Rent", 1.0, 0, 6));
        assertThrows(DomainValidationException.class, () -> item(USER, "Rent", 1.0, 1, null));
        assertThrows(DomainValidationException.class, () -> item(USER, "Rent", 1.0, 2, 11));
    }

    @Test
    void initialAmountNeedsAUserAndAnAmount() {
        assertDoesNotThrow(() -> new InitialAmountRequest(USER, 0.0));
        assertThrows(DomainValidationException.class, () -> new InitialAmountRequest(null, 1.0));
        assertThrows(DomainValidationException.class, () -> new InitialAmountRequest(USER, null));
    }

    @Test
    void catalogRequestsNeedAPositiveIdAndAName() {
        assertDoesNotThrow(() -> new ItemTypeRequest(4L, "Other"));
        assertThrows(DomainValidationException.class, () -> new ItemTypeRequest(0L, "Other"));
        assertThrows(DomainValidationException.class, () -> new ItemTypeRequest(4L, ""));
        assertThrows(DomainValidationException.class, () -> new TimePeriodRequest(null, "Hourly"));
        assertThrows(DomainValidationException.class, () -> new TimePeriodRequest(11L, " "));
        assertThrows(DomainValidationException.class, () -> new UpdateItemTypeNameRequest(""));
        assertThrows(DomainValidationException.class, () -> new UpdateTimePeriodNameRequest(null));
        assertEquals(Set.of("size must be between 0 and 75"), messages(new ItemTypeRequest(4L, "x".repeat(76))));
    }

    @Test
    void loginAndRegisterChecks() {
        assertTrue(messages(new LoginRequest("rick@example.com", "Password@2")).isEmpty());
        assertEquals(Set.of("Email must be valid", "Password must be at least 8 characters"), messages(new LoginRequest("nope", "short")));

        assertTrue(messages(new RegisterRequest("rick@example.com", "Password@2", "Rick", "D")).isEmpty());
        assertEquals(Set.of("First name is required", "Last name is required"), messages(new RegisterRequest("rick@example.com", "Password@2", "", "")));
        assertEquals(Set.of("First name must be at most 50 characters"), messages(new RegisterRequest("rick@example.com", "Password@2", "x".repeat(51), "D")));
    }

    @Test
    void changePasswordChecks() {
        assertTrue(messages(new ChangePasswordRequest(7L, "old", "NewPassword@3")).isEmpty());
        assertEquals(Set.of("Record ID is required", "Current Password is required", "New Password must be at least 8 characters"),
                     messages(new ChangePasswordRequest(null, "", "short")));
    }
}
