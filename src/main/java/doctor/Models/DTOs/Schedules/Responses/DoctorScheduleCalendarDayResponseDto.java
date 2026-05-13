package doctor.Models.DTOs.Schedules.Responses;

import java.time.LocalDate;

public record DoctorScheduleCalendarDayResponseDto(
        LocalDate ngay,
        String trangThai,
        Integer soLichLamViec,
        Integer soChiTietLich,
        boolean coNguoiDat) {}
