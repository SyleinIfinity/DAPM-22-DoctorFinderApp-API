package doctor.Models.DTOs.Schedules.Requests;

import java.time.LocalDate;
import java.time.LocalTime;

public record WorkingSlotUpsertItemDto(
        Integer thuTrongTuan,
        LocalDate ngayCuThe,
        LocalTime gioBatDau,
        LocalTime gioKetThuc,
        Integer maKhungGio,
        Integer soLuongToiDa,
        String trangThaiLich) {}

