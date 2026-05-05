package doctor.Models.DTOs.Admin.Responses;

import java.util.List;

public record AdminDoctorProfileTrafficReportDto(List<AdminReportSliceDto> slices, long totalViews) {}
