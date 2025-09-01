package io.github.tony8864.user;

import io.github.tony8864.exceptions.user.InvalidEmailFormatException;
import io.github.tony8864.entities.user.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {
    @Test
    void newEmailShouldCreateInstanceWithValidEmail() {
        Email email = Email.newEmail("user@example.com");
        assertNotNull(email);
        assertEquals("user@example.com", email.getValue());
    }

    @Test
    void newEmailShouldThrowExceptionForMissingAtSymbol() {
        assertThrows(InvalidEmailFormatException.class,
                () -> Email.newEmail("userexample.com"));
    }

    @Test
    void newEmailShouldThrowExceptionForMissingDomain() {
        assertThrows(InvalidEmailFormatException.class,
                () -> Email.newEmail("user@"));
    }

    @Test
    void newEmailShouldThrowExceptionForMissingLocalPart() {
        assertThrows(InvalidEmailFormatException.class,
                () -> Email.newEmail("@example.com"));
    }

    @Test
    void newEmailShouldThrowExceptionForInvalidTld() {
        assertThrows(InvalidEmailFormatException.class,
                () -> Email.newEmail("user@example.c"));
    }

    @Test
    void newEmailShouldBeCaseInsensitive() {
        Email email1 = Email.newEmail("USER@EXAMPLE.COM");
        Email email2 = Email.newEmail("user@example.com");

        // they won't be equal objects (no equals/hashCode override),
        // but both must be accepted and retain original casing
        assertEquals("USER@EXAMPLE.COM", email1.getValue());
        assertEquals("user@example.com", email2.getValue());
    }
}