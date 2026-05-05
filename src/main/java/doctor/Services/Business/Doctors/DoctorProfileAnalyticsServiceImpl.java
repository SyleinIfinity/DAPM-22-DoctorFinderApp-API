package doctor.Services.Business.Doctors;

import doctor.Models.Entities.LichSuTimKiemBacSi;
import doctor.Models.Entities.LuotXemHoSoBacSi;
import doctor.Repositories.Interfaces.LichSuTimKiemBacSiRepository;
import doctor.Repositories.Interfaces.LuotXemHoSoBacSiRepository;
import doctor.Services.Interfaces.Doctors.DoctorProfileAnalyticsService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorProfileAnalyticsServiceImpl implements DoctorProfileAnalyticsService {
    private final LuotXemHoSoBacSiRepository luotXemHoSoBacSiRepository;
    private final LichSuTimKiemBacSiRepository lichSuTimKiemBacSiRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logProfileView(Integer maBacSi, Integer viewerMaTaiKhoan) {
        if (maBacSi == null || maBacSi <= 0) {
            return;
        }
        LuotXemHoSoBacSi row = new LuotXemHoSoBacSi();
        row.setMaBacSi(maBacSi);
        row.setMaTaiKhoan(viewerMaTaiKhoan != null && viewerMaTaiKhoan > 0 ? viewerMaTaiKhoan : null);
        row.setThoiGian(LocalDateTime.now());
        luotXemHoSoBacSiRepository.insert(row);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSearch(String keyword, String chuyenKhoa, Integer viewerMaTaiKhoan) {
        String kw = keyword == null ? null : keyword.trim();
        String ck = chuyenKhoa == null ? null : chuyenKhoa.trim();
        if ((kw == null || kw.isEmpty()) && (ck == null || ck.isEmpty())) {
            return;
        }
        LichSuTimKiemBacSi row = new LichSuTimKiemBacSi();
        row.setTuKhoa(kw == null || kw.isEmpty() ? null : kw);
        row.setChuyenKhoa(ck == null || ck.isEmpty() ? null : ck);
        row.setMaTaiKhoan(viewerMaTaiKhoan != null && viewerMaTaiKhoan > 0 ? viewerMaTaiKhoan : null);
        row.setThoiGian(LocalDateTime.now());
        lichSuTimKiemBacSiRepository.insert(row);
    }
}
