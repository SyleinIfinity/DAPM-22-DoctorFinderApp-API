package doctor.Models.DTOs.Appointments.Responses;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentDetailResponseDto(
        Integer maPhieuDatLich,
        String trangThaiPhieu,
        String lyDoTuChoi,
        String loaiPhieu,
        String trieuChungGhiChu,
        Integer maNguoiDung,
        String hoTenBenhNhan,
        String soDienThoaiBenhNhan,
        String emailBenhNhan,
        Integer maBacSi,
        String hoTenBacSi,
        String chuyenKhoa,
        String tenCoSoYTe,
        String diaChiLamViec,
        LocalDate ngayCuThe,
        Integer thuTrongTuan,
        LocalTime gioBatDau,
        LocalTime gioKetThuc,
        Integer maChiTiet,
        Integer maLichLamViec,
        Integer maKhungGio,
        Integer thoiLuongPhut,
        String trangThaiLich) {}

