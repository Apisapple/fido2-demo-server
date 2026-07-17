package tf.demo.fido2.fido.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tf.demo.fido2.fido.application.CeremonyUnavailableException;

@RestControllerAdvice
class FidoApiExceptionHandler {
    @ExceptionHandler(CeremonyUnavailableException.class)
    ResponseEntity<ApiError> ceremonyUnavailable(
            CeremonyUnavailableException exception, HttpServletRequest request) {
        return response(
                HttpStatus.GONE, "CEREMONY_UNAVAILABLE", "The ceremony is unavailable.", request);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<ApiError> invalidRequest(Exception exception, HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "The request is invalid.", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadableRequest(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "The request body is invalid.", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "The request could not be completed.",
                request);
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(
                        new ApiError(
                                code,
                                message,
                                request.getRequestURI(),
                                UUID.randomUUID().toString()));
    }

    record ApiError(String code, String message, String path, String correlationId) {}
}
