package drift.controller;

import drift.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlingControllerAdvice {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(Exception e) {
        return formMessage("Internal server error: %s.", e);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(Exception e) {
        return formMessage("Resource is not found: %s.", e);
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleSecurityException(Exception e) {
        return formMessage("Unauthorized: %s.", e);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            DataIntegrityViolationException.class,
            MultipartException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(Exception e) {
        return formMessage("Validation exception: %s.", e);
    }

    private String formMessage(String messageFormat, Exception e) {
        var errorMessage = String.format(messageFormat, e.getMessage());
        log.error(errorMessage, e);
        return errorMessage;
    }
}
