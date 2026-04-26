package doctor.Models.DTOs.Appointments.Requests;

public record CreateAppointmentRequestDto(
        Integer maNguoiDung,
        Integer maChiTiet,
        String loaiPhieu,
        String trieuChungGhiChu) {}

