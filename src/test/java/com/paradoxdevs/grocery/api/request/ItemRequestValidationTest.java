package com.paradoxdevs.grocery.api.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRequestValidationTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsValidNameAndOptionalBarcode() {
        assertTrue(validator.validate(request("Whole milk", null)).isEmpty());
        assertTrue(validator.validate(request("Whole milk", "12345678")).isEmpty());
        assertTrue(validator.validate(request("Whole milk", "123456789012")).isEmpty());
        assertTrue(validator.validate(request("Whole milk", "1234567890123")).isEmpty());
    }

    @Test
    void rejectsBlankOrWhitespacePaddedNames() {
        assertFalse(validator.validate(request(" ", null)).isEmpty());
        assertFalse(validator.validate(request(" Whole milk", null)).isEmpty());
        assertFalse(validator.validate(request("Whole milk ", null)).isEmpty());
        assertFalse(validator.validate(request("Whole milk\n", null)).isEmpty());
    }

    @Test
    void rejectsBarcodesThatAreNotEightToThirteenDigits() {
        assertFalse(validator.validate(request("Whole milk", "1234567")).isEmpty());
        assertFalse(validator.validate(request("Whole milk", "12345678901234")).isEmpty());
        assertFalse(validator.validate(request("Whole milk", "1234 678")).isEmpty());
        assertFalse(validator.validate(request("Whole milk", "1234567 ")).isEmpty());
        assertFalse(validator.validate(request("Whole milk", "12345678\n")).isEmpty());
        assertFalse(validator.validate(request("Whole milk", "ABCDEFGH")).isEmpty());
    }

    private static ItemRequest request(String name, String barcode) {
        ItemRequest request = new ItemRequest();
        request.setName(name);
        request.setBarcode(barcode);
        return request;
    }
}
