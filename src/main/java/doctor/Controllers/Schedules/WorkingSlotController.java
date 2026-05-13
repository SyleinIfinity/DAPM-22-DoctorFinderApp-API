package doctor.Controllers.Schedules;

import doctor.Models.DTOs.Schedules.Requests.DoctorScheduleCalendarQueryDto;
import doctor.Models.DTOs.Schedules.Requests.WorkingSlotUpsertItemDto;
import doctor.Models.DTOs.Schedules.Responses.DoctorScheduleCalendarDayResponseDto;
import doctor.Models.DTOs.Schedules.Responses.WorkingScheduleResponseDto;
import doctor.Models.DTOs.Schedules.Responses.WorkingSlotResponseDto;
import doctor.Services.Interfaces.Schedules.WorkingSlotService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors/{maBacSi}/working-slots")
@RequiredArgsConstructor
public class WorkingSlotController {
    private final WorkingSlotService workingSlotService;

    @GetMapping
    public ResponseEntity<List<WorkingSlotResponseDto>> getWorkingSlots(
            @PathVariable Integer maBacSi, @RequestParam LocalDate date) {
        return ResponseEntity.ok(workingSlotService.getWorkingSlotsByDoctorAndDate(maBacSi, date));
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<DoctorScheduleCalendarDayResponseDto>> getCalendarDays(
            @PathVariable Integer maBacSi,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {
        return ResponseEntity.ok(
                workingSlotService.getCalendarDays(
                        maBacSi, new DoctorScheduleCalendarQueryDto(fromDate, toDate)));
    }

    @PostMapping
    public ResponseEntity<List<WorkingScheduleResponseDto>> createWorkingSlots(
            @PathVariable Integer maBacSi, @RequestBody List<WorkingSlotUpsertItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Request body is required");
        }
        return ResponseEntity.ok(workingSlotService.createWorkingSlots(maBacSi, items));
    }

    @PutMapping
    public ResponseEntity<List<WorkingScheduleResponseDto>> updateWorkingSlots(
            @PathVariable Integer maBacSi, @RequestBody List<WorkingSlotUpsertItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Request body is required");
        }
        return ResponseEntity.ok(workingSlotService.updateWorkingSlots(maBacSi, items));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteWorkingSlots(
            @PathVariable Integer maBacSi, @RequestBody List<WorkingSlotUpsertItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Request body is required");
        }
        workingSlotService.deleteWorkingSlots(maBacSi, items);
        return ResponseEntity.noContent().build();
    }
}
