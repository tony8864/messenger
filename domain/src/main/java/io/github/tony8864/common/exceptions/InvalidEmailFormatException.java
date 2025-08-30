package io.github.tony8864.common.exceptions;

public class InvalidEmailFormatException extends DomainException{
    public InvalidEmailFormatException() {
        super("Invalid email format");
    }
}
