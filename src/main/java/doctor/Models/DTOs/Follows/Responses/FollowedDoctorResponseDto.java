package doctor.Models.DTOs.Follows.Responses;

import java.time.LocalDateTime;

public record FollowedDoctorResponseDto(
        Integer maNguoiDung,
        Integer maBacSi,
        String hoTenBacSi,
        String chuyenKhoa,
        String tenCoSoYTe,
        String diaChiLamViec,
        String anhDaiDienBacSi,
        LocalDateTime ngayTheoDoi) {}

