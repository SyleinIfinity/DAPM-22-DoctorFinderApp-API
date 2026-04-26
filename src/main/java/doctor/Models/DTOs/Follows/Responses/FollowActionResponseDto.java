package doctor.Models.DTOs.Follows.Responses;

import java.time.LocalDateTime;

public record FollowActionResponseDto(
        boolean success,
        String message,
        Integer maNguoiDung,
        Integer maBacSi,
        boolean followed,
        LocalDateTime ngayTheoDoi) {}

