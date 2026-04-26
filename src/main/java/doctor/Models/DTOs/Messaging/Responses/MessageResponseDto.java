package doctor.Models.DTOs.Messaging.Responses;

import java.time.LocalDateTime;

public record MessageResponseDto(
        Integer maTinNhan,
        Integer maCuocHoiThoai,
        Integer maTaiKhoanGui,
        String loaiNoiDung,
        String noiDungTinNhan,
        LocalDateTime thoiGianGui) {}

