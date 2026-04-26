package doctor.Services.Interfaces.Schedules;

import doctor.Models.DTOs.Schedules.Requests.UpsertDoctorWorkingSlotsRequestDto;
import doctor.Models.DTOs.Schedules.Responses.WorkingScheduleResponseDto;
import doctor.Models.DTOs.Schedules.Responses.WorkingSlotResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface WorkingSlotService {
    List<WorkingSlotResponseDto> getWorkingSlotsByDoctorAndDate(Integer maBacSi, LocalDate date);

    List<WorkingScheduleResponseDto> upsertDoctorWorkingSlots(
            Integer maBacSi, UpsertDoctorWorkingSlotsRequestDto request);
}

