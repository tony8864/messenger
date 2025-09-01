package io.github.tony8864.user.usecase.register.exception;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String email) {
    super("User with email: " + email + " already exists");
  }
}
