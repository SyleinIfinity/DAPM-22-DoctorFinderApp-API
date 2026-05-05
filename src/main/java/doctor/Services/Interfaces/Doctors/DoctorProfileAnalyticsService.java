package doctor.Services.Interfaces.Doctors;

public interface DoctorProfileAnalyticsService {
    void logProfileView(Integer maBacSi, Integer viewerMaTaiKhoan);

    void logSearch(String keyword, String chuyenKhoa, Integer viewerMaTaiKhoan);
}
