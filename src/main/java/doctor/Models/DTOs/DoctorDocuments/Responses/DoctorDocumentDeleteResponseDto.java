package doctor.Models.DTOs.DoctorDocuments.Responses;

public record DoctorDocumentDeleteResponseDto(
        boolean deleted, String message, Integer maTaiLieu, boolean deletedFromStorage) {}

