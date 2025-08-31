package io.github.tony8864.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String email) {
    super("User with email: " + email + " already exists");
  }
}
