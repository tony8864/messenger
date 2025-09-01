package io.github.tony8864.entities.user;

import io.github.tony8864.exceptions.user.InvalidEmailFormatException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Email {
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidEmailFormatException("Email cannot be null or blank");
        }
        if (!validate(value)) {
            throw new InvalidEmailFormatException("Invalid email format: " + value);
        }
        return new Email(value);
    }

    public String getValue() {
        return value;
    }

    private static boolean validate(String value) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(value);
        return matcher.matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                // same reference
        if (!(o instanceof Email)) return false;   // type check
        Email other = (Email) o;
        return value.equals(other.value);          // compare actual email string
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
