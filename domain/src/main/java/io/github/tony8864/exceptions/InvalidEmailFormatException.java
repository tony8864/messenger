package io.github.tony8864.exceptions;

public class InvalidEmailFormatException extends DomainException{
    public InvalidEmailFormatException() {
        super("Invalid email format");
    }
}
