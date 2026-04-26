package doctor.Models.DTOs.Auth.Responses;

public record RegisterResponseDto(
        boolean registered,
        String message,
        Integer maTaiKhoan,
        String tenDangNhap,
        String vaiTro,
        String trangThaiHoatDong,
        Integer maNguoiDung,
        Integer maBacSi) {}
