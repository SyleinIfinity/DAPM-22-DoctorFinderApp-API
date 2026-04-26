package doctor.Models.DTOs.Schedules.Responses;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record WorkingSlotResponseDto(
        Integer maChiTiet,
        Integer maLichLamViec,
        Integer maBacSi,
        Integer thuTrongTuan,
        LocalDate ngayCuThe,
        LocalTime gioBatDau,
        LocalTime gioKetThuc,
        String trangThai,
        LocalDateTime khoaDen,
        Integer maPhieuDatLichHienTai,
        Integer maKhungGio,
        Integer thoiLuongPhut,
        String trangThaiLich) {}

