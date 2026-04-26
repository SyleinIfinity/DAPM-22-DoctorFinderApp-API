package doctor.Models.DTOs.Admin.Responses;

import java.time.LocalDateTime;

public record AdminAccountActionResponseDto(
        boolean success,
        String message,
        Integer maTaiKhoan,
        String tenDangNhap,
        String vaiTro,
        String trangThaiHoatDong,
        LocalDateTime ngayTao) {}

