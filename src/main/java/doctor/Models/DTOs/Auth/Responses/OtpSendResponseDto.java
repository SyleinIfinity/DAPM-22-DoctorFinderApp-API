package doctor.Models.DTOs.Auth.Responses;

public record OtpSendResponseDto(
        String email,
        String purpose,
        String status,
        boolean otpSent,
        long expiresInSeconds,
        long resendInSeconds,
        String message) {}
