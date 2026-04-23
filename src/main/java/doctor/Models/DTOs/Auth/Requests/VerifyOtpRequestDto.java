package doctor.Models.DTOs.Auth.Requests;

public record VerifyOtpRequestDto(String email, String purpose, String otpCode) {}
