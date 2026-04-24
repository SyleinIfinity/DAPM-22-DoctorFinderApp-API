package doctor.Models.DTOs.Auth.Responses;

import java.time.LocalDateTime;

public record AccountInfoResponseDto(
        Integer maTaiKhoan,
        String tenDangNhap,
        String vaiTro,
        String trangThaiHoatDong,
        LocalDateTime ngayTao,
        Integer maNguoiDung,
        String hoLot,
        String ten,
        String soDienThoai,
        String email,
        String cccd,
        String anhDaiDien) {}
