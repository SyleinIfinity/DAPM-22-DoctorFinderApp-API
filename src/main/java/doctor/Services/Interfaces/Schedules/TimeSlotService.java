package doctor.Services.Interfaces.Schedules;

import doctor.Models.DTOs.Schedules.Requests.CreateTimeSlotRequestDto;
import doctor.Models.DTOs.Schedules.Responses.TimeSlotResponseDto;
import java.util.List;

public interface TimeSlotService {
    List<TimeSlotResponseDto> getAllTimeSlots();

    TimeSlotResponseDto createTimeSlot(CreateTimeSlotRequestDto request);
}

