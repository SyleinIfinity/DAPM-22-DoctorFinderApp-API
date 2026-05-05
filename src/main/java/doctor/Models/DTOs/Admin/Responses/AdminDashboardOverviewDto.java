package doctor.Models.DTOs.Admin.Responses;

public record AdminDashboardOverviewDto(
        long onlineAccounts, long totalMembers, long totalDoctors, int onlineWindowMinutes) {}
