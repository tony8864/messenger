package io.github.tony8864.entities.user;

import io.github.tony8864.exceptions.user.InvalidEmailFormatException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Email {
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email newEmail(String value) {
        if (!validate(value)) throw new InvalidEmailFormatException();
        return new Email(value);
    }

    public String getValue() {
        return value;
    }

    private static boolean validate(String value) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(value);
        return matcher.matches();
    }
}
