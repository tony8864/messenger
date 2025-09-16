package io.github.tony8864.common.exception;

import io.github.tony8864.chat.common.exception.GroupChatNotFoundException;
import io.github.tony8864.chat.usecase.createdirectchat.exception.InvalidChatException;
import io.github.tony8864.common.UserNotFoundException;
import io.github.tony8864.exceptions.chat.InvalidGroupException;
import io.github.tony8864.exceptions.common.UnauthorizedOperationException;
import io.github.tony8864.user.usecase.login.exception.InvalidCredentialsException;
import io.github.tony8864.user.usecase.register.exception.UserAlreadyExistsException;
import io.github.tony8864.user.usecase.register.exception.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    // --- User-related exceptions ---
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

    @ExceptionHandler(UnauthorizedOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUnauthorizedOperation(UnauthorizedOperationException ex) {
        return new ErrorResponse("FORBIDDEN", ex.getMessage());
    }

    // --- Chat-related exceptions ---
    @ExceptionHandler(InvalidGroupException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidGroup(InvalidGroupException ex) {
        return new ErrorResponse("INVALID_GROUP", ex.getMessage());
    }

    @ExceptionHandler(GroupChatNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGroupChatNotFound(GroupChatNotFoundException ex) {
        return new ErrorResponse("GROUP_CHAT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InvalidChatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidChat(InvalidChatException ex) {
        return new ErrorResponse("INVALID_CHAT", ex.getMessage());
    }

    // --- Generic fallback ---
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        return new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred");
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return new ErrorResponse("USERNAME_ALREADY_EXISTS", ex.getMessage());
    }
}
