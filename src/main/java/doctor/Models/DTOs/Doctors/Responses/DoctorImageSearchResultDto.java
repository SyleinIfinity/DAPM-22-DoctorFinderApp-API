package doctor.Models.DTOs.Doctors.Responses;

public record DoctorImageSearchResultDto(
        DoctorProfileResponseDto bacSi, double similarityScore, String matchedImageUrl) {}
