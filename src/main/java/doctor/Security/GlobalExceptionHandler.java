package doctor.Security;

import doctor.Models.DTOs.Auth.Responses.ErrorResponseDto;
import doctor.Security.Exceptions.TooManyRequestsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ErrorResponseDto.badRequest(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("Cloudinary")) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(ErrorResponseDto.uploadFailed(message));
        }
        if (message != null && message.contains("luu avatar vao co so du lieu")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponseDto.databaseError(message));
        }
        if (message != null && message.contains("URL khong hop le")) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(ErrorResponseDto.uploadFailed(message));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto.internalError());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponseDto> handleTooManyRequests(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponseDto.tooManyRequests(ex.getMessage(), ex.getRetryAfterSeconds()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnhandledException(Exception ignored) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto.internalError());
    }
}
