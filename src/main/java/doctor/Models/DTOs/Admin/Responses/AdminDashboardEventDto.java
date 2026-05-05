package doctor.Models.DTOs.Admin.Responses;

import java.time.LocalDateTime;

public record AdminDashboardEventDto(String type, String message, LocalDateTime occurredAt) {}
