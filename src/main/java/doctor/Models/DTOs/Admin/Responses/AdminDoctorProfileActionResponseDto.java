package doctor.Models.DTOs.Admin.Responses;

public record AdminDoctorProfileActionResponseDto(
        boolean success, String message, Integer maBacSi, String trangThaiHoSo, String lyDoTuChoi) {}

