package doctor.Models.DTOs.Auth.Responses;

public record UpgradeToDoctorResponseDto(
        boolean upgraded,
        String message,
        Integer maTaiKhoan,
        Integer maBacSi,
        String trangThaiHoSo,
        Integer soTaiLieuDaTaiLen) {}

