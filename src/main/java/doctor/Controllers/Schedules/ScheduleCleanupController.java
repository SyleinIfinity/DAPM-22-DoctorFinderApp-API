package doctor.Controllers.Schedules;

import doctor.Services.Business.Schedules.ScheduleCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/schedules")
@RequiredArgsConstructor
public class ScheduleCleanupController {
    private final ScheduleCleanupService scheduleCleanupService;

    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetSchedules() {
        scheduleCleanupService.clearScheduleData();
        return ResponseEntity.noContent().build();
    }
}
