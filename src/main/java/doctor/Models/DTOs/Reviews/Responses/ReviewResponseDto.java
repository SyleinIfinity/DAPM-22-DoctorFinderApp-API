package doctor.Models.DTOs.Reviews.Responses;

import java.time.LocalDateTime;

public record ReviewResponseDto(
        Integer maDanhGia,
        Integer maNguoiDung,
        String hoTenNguoiDung,
        String anhDaiDienNguoiDung,
        Integer maBacSi,
        Integer soSao,
        String noiDung,
        LocalDateTime thoiGian) {}

