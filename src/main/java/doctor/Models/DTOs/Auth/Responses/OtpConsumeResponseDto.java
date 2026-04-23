package doctor.Models.DTOs.Auth.Responses;

public record OtpConsumeResponseDto(
        String email, String purpose, String status, boolean consumed, String message) {}
