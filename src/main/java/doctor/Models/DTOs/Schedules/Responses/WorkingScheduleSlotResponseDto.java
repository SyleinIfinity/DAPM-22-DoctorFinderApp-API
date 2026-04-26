package doctor.Models.DTOs.Schedules.Responses;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record WorkingScheduleSlotResponseDto(
        Integer maChiTiet,
        LocalTime gioBatDau,
        LocalTime gioKetThuc,
        String trangThai,
        LocalDateTime khoaDen,
        Integer maPhieuDatLichHienTai) {}

