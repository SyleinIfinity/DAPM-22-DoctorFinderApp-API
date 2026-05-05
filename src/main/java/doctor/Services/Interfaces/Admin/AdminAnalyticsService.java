package doctor.Services.Interfaces.Admin;

import doctor.Models.DTOs.Admin.Responses.AdminDashboardEventDto;
import doctor.Models.DTOs.Admin.Responses.AdminDashboardOverviewDto;
import doctor.Models.DTOs.Admin.Responses.AdminDoctorProfileTrafficReportDto;
import doctor.Models.DTOs.Admin.Responses.AdminReportDoctorRankDto;
import doctor.Models.DTOs.Admin.Responses.AdminReportKeywordDto;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminAnalyticsService {
    AdminDashboardOverviewDto getOverview(Integer onlineWindowMinutes);

    List<AdminDashboardEventDto> getRecentEvents(int hours, int limit);

    AdminDoctorProfileTrafficReportDto getProfileTraffic(LocalDateTime from, LocalDateTime to, int topN);

    List<AdminReportDoctorRankDto> getTopDoctorsByViews(LocalDateTime from, LocalDateTime to, int limit);

    List<AdminReportDoctorRankDto> getTopDoctorsByFollows(LocalDateTime from, LocalDateTime to, int limit);

    List<AdminReportKeywordDto> getTopKeywords(LocalDateTime from, LocalDateTime to, int limit);
}
