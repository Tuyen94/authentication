package tuyenbd.authentication.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        return handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex, WebRequest request) {
        return handleException(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenException(Exception ex, WebRequest request) {
        return handleException(ex, HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<Map<String, Object>> handleException(Exception ex, HttpStatus httpStatus) {
        log.error("handle error ", ex);
        return new ResponseEntity<>(buildError(ex, httpStatus), httpStatus);
    }

    private Map<String, Object> buildError(Exception ex, HttpStatus status) {
        return Map.of(
                "code", status.value(),
                "message", ex.getMessage()
        );
    }
}
