package doctor.Models.DTOs.Auth.Requests;

public record RegisterDoctorAccountRequestDto(
        RegisterUserInfoRequestDto thongTinNguoiDung,
        RegisterDoctorProfileRequestDto thongTinBacSi,
        String otpProofToken) {}
