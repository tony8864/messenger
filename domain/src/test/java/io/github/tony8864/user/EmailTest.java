package io.github.tony8864.user;

import io.github.tony8864.exceptions.user.InvalidEmailFormatException;
import io.github.tony8864.entities.user.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {
    @Test
    void ofShouldCreateInstanceWithValidEmail() {
        Email email = Email.of("user@example.com");
        assertNotNull(email);
        assertEquals("user@example.com", email.getValue());
    }

    @Test
    void ofShouldThrowExceptionForMissingAtSymbol() {
        assertThrows(InvalidEmailFormatException.class,
                () -> Email.of("userexample.com"));
    }

    @Test
    void ofShouldThrowExceptionForMissingDomain() {
        assertThrows(InvalidEmailFormatException.class,
                () -> Email.of("user@"));
    }

    @Test
    void ofShouldThrowExceptionForMissingLocalPart() {
        assertThrows(InvalidEmailFormatException.class,
                () -> Email.of("@example.com"));
    }

    @Test
    void ofShouldThrowExceptionForInvalidTld() {
        assertThrows(InvalidEmailFormatException.class,
                () -> Email.of("user@example.c"));
    }

    @Test
    void ofShouldBeCaseInsensitive() {
        Email email1 = Email.of("USER@EXAMPLE.COM");
        Email email2 = Email.of("user@example.com");

        // they won't be equal objects (no equals/hashCode override),
        // but both must be accepted and retain original casing
        assertEquals("USER@EXAMPLE.COM", email1.getValue());
        assertEquals("user@example.com", email2.getValue());
    }
}