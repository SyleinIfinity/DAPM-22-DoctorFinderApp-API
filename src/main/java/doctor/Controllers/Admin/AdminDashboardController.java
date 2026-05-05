package doctor.Controllers.Admin;

import doctor.Models.DTOs.Admin.Responses.AdminDashboardEventDto;
import doctor.Models.DTOs.Admin.Responses.AdminDashboardOverviewDto;
import doctor.Models.DTOs.Admin.Responses.AdminDoctorProfileTrafficReportDto;
import doctor.Models.DTOs.Admin.Responses.AdminReportDoctorRankDto;
import doctor.Models.DTOs.Admin.Responses.AdminReportKeywordDto;
import doctor.Services.Interfaces.Admin.AdminAnalyticsService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/dashboard/overview")
    public ResponseEntity<AdminDashboardOverviewDto> overview(
            @RequestParam(required = false) Integer onlineWindowMinutes) {
        return ResponseEntity.ok(adminAnalyticsService.getOverview(onlineWindowMinutes));
    }

    @GetMapping("/dashboard/events")
    public ResponseEntity<List<AdminDashboardEventDto>> events(
            @RequestParam(defaultValue = "24") int hours, @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(adminAnalyticsService.getRecentEvents(hours, limit));
    }

    @GetMapping("/reports/doctor-profile-traffic")
    public ResponseEntity<AdminDoctorProfileTrafficReportDto> doctorProfileTraffic(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "7") int top) {
        return ResponseEntity.ok(adminAnalyticsService.getProfileTraffic(from, to, top));
    }

    @GetMapping("/reports/top-doctors")
    public ResponseEntity<List<AdminReportDoctorRankDto>> topDoctors(
            @RequestParam String metric,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "10") int limit) {
        String m = metric == null ? "" : metric.trim().toLowerCase();
        if ("follow".equals(m)) {
            return ResponseEntity.ok(adminAnalyticsService.getTopDoctorsByFollows(from, to, limit));
        }
        return ResponseEntity.ok(adminAnalyticsService.getTopDoctorsByViews(from, to, limit));
    }

    @GetMapping("/reports/top-search-keywords")
    public ResponseEntity<List<AdminReportKeywordDto>> topKeywords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminAnalyticsService.getTopKeywords(from, to, limit));
    }
}
