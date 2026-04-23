package doctor.Models.DTOs.Auth.Requests;

public record SendOtpRequestDto(String email, String purpose, Boolean forceResend) {}
