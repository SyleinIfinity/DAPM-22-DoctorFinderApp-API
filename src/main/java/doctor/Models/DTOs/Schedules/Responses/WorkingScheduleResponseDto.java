package doctor.Models.DTOs.Schedules.Responses;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record WorkingScheduleResponseDto(
        Integer maLichLamViec,
        Integer maBacSi,
        Integer thuTrongTuan,
        LocalDate ngayCuThe,
        LocalTime gioBatDau,
        LocalTime gioKetThuc,
        Integer maKhungGio,
        Integer thoiLuongPhut,
        Integer soLuongToiDa,
        String trangThaiLich,
        List<WorkingScheduleSlotResponseDto> chiTiet) {}

