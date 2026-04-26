package doctor.Models.DTOs.Admin.Responses;

import java.time.LocalDateTime;

public record AdminAccountResponseDto(
        Integer maTaiKhoan,
        String tenDangNhap,
        String vaiTro,
        String trangThaiHoatDong,
        LocalDateTime ngayTao,
        Integer maNguoiDung,
        String hoTenNguoiDung,
        String soDienThoai,
        String email,
        Integer maBacSi,
        String trangThaiHoSoBacSi) {}

