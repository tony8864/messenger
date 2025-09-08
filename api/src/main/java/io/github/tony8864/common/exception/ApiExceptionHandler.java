package io.github.tony8864.common.exception;

import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.user.usecase.login.exception.InvalidCredentialsException;
import io.github.tony8864.user.usecase.register.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return new ErrorResponse("USER_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return new ErrorResponse("INVALID_CREDENTIALS", "Invalid email or password");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex) {
        return new ErrorResponse("USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(GroupChatNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGroupChatNotFound(GroupChatNotFoundException ex) {
        return new ErrorResponse("GROUP_CHAT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUnauthorizedOperation(UnauthorizedOperationException ex) {
        return new ErrorResponse("FORBIDDEN", ex.getMessage());
    }
}
