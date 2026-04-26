package doctor.Models.DTOs.Auth.Responses;

public record LoginResponseDto(
        boolean authenticated,
        String message,
        Integer maTaiKhoan,
        String tenDangNhap,
        String vaiTro,
        String trangThaiHoatDong,
        Integer maNguoiDung,
        Integer maBacSi) {}
