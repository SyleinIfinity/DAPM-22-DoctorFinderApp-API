package doctor.Models.DTOs.Appointments.Responses;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentSummaryResponseDto(
        Integer maPhieuDatLich,
        Integer maNguoiDung,
        Integer maBacSi,
        Integer maChiTiet,
        String loaiPhieu,
        String trieuChungGhiChu,
        String trangThaiPhieu,
        String lyDoTuChoi,
        boolean coTheDanhGia,
        LocalDate ngayCuThe,
        Integer thuTrongTuan,
        LocalTime gioBatDau,
        LocalTime gioKetThuc,
        String hoTenBacSi,
        String chuyenKhoa,
        String tenCoSoYTe,
        String diaChiLamViec) {}

