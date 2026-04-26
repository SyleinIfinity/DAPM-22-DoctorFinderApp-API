package doctor.Models.DTOs.Schedules.Requests;

import java.util.List;

public record UpsertDoctorWorkingSlotsRequestDto(List<WorkingSlotUpsertItemDto> items) {}

