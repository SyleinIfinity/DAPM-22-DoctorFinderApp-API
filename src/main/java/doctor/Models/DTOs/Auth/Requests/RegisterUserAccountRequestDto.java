package doctor.Models.DTOs.Auth.Requests;

public record RegisterUserAccountRequestDto(
        RegisterUserInfoRequestDto thongTinNguoiDung, String otpProofToken) {}
