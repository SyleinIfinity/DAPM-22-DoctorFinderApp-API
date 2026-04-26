package doctor.Models.DTOs.Auth.Responses;

import java.time.LocalDateTime;

public record AccountDoctorInfoResponseDto(
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
        String anhDaiDien,
        boolean coTaiKhoanBacSi,
        Integer maBacSi,
        String chuyenKhoa,
        String trinhDoChuyenMon,
        String loaiHinhBacSi,
        String tenCoSoYTe,
        String diaChiLamViec,
        String maChungChiHanhNghe,
        String moTaBanThan,
        String trangThaiHoSo) {}
