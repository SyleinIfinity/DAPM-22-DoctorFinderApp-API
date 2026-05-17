package doctor.Models.DTOs.Admin.Responses;

public record AdminReportDoctorRankDto(
        int rank,
        Integer maBacSi,
        String hoTenDayDu,
        String chuyenKhoa,
        String trangThaiHoSo,
        long count) {}
