package doctor.Controllers.Schedules;

import doctor.Models.DTOs.Schedules.Responses.TimeSlotResponseDto;
import doctor.Services.Interfaces.Schedules.TimeSlotService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {
    private final TimeSlotService timeSlotService;

    @GetMapping
    public ResponseEntity<List<TimeSlotResponseDto>> getTimeSlots() {
        return ResponseEntity.ok(timeSlotService.getAllTimeSlots());
    }
}

