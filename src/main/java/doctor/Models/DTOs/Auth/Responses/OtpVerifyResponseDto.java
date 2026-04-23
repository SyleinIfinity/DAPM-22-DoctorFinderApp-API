package doctor.Models.DTOs.Auth.Responses;

public record OtpVerifyResponseDto(
        String email,
        String purpose,
        String status,
        boolean verified,
        int attemptsRemaining,
        long expiresInSeconds,
        long resendInSeconds,
        String message) {}
