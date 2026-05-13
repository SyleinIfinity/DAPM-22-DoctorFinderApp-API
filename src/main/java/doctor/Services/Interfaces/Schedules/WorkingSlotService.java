package doctor.Services.Interfaces.Schedules;

import doctor.Models.DTOs.Schedules.Requests.DoctorScheduleCalendarQueryDto;
import doctor.Models.DTOs.Schedules.Requests.WorkingSlotUpsertItemDto;
import doctor.Models.DTOs.Schedules.Responses.DoctorScheduleCalendarDayResponseDto;
import doctor.Models.DTOs.Schedules.Responses.WorkingScheduleResponseDto;
import doctor.Models.DTOs.Schedules.Responses.WorkingSlotResponseDto;
import java.time.LocalDate;
import java.util.List;

public interface WorkingSlotService {
    List<WorkingSlotResponseDto> getWorkingSlotsByDoctorAndDate(Integer maBacSi, LocalDate date);

    List<WorkingScheduleResponseDto> createWorkingSlots(
            Integer maBacSi, List<WorkingSlotUpsertItemDto> items);

    List<WorkingScheduleResponseDto> updateWorkingSlots(
            Integer maBacSi, List<WorkingSlotUpsertItemDto> items);

    void deleteWorkingSlots(Integer maBacSi, List<WorkingSlotUpsertItemDto> items);

    List<DoctorScheduleCalendarDayResponseDto> getCalendarDays(
            Integer maBacSi, DoctorScheduleCalendarQueryDto query);
}

