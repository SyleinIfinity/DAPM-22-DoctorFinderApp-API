package doctor.Models.DTOs.Appointments.Responses;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequestResponseDto(
        Integer maPhieuDatLich,
        Integer maNguoiDung,
        Integer maChiTiet,
        String hoTenBenhNhan,
        String soDienThoaiBenhNhan,
        String emailBenhNhan,
        LocalDate ngayCuThe,
        Integer thuTrongTuan,
        LocalTime gioBatDau,
        LocalTime gioKetThuc,
        String loaiPhieu,
        String trieuChungGhiChu,
        String trangThaiPhieu) {}

