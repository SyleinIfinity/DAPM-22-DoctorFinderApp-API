package doctor.Models.DTOs.Auth.Responses;

public record ErrorResponseDto(String code, String message, Long retryAfterSeconds) {
    public static ErrorResponseDto badRequest(String message) {
        return new ErrorResponseDto("BAD_REQUEST", message, null);
    }

    public static ErrorResponseDto tooManyRequests(String message, long retryAfterSeconds) {
        return new ErrorResponseDto("TOO_MANY_REQUESTS", message, Math.max(1L, retryAfterSeconds));
    }

    public static ErrorResponseDto internalError() {
        return new ErrorResponseDto("INTERNAL_ERROR", "Da xay ra loi he thong", null);
    }
}
