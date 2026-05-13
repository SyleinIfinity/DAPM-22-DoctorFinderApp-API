package doctor.Services.Business.Schedules;

import doctor.Repositories.Interfaces.ChiTietLichRepository;
import doctor.Repositories.Interfaces.LichLamViecRepository;
import doctor.Repositories.Interfaces.PhieuDatLichRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleCleanupService {
    private final PhieuDatLichRepository phieuDatLichRepository;
    private final ChiTietLichRepository chiTietLichRepository;
    private final LichLamViecRepository lichLamViecRepository;
    private final EntityManager entityManager;

    @Transactional
    public void clearScheduleData() {
        entityManager.createNativeQuery("DELETE FROM phieu_dat_lich").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM chi_tiet_lich").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM lich_lam_viec").executeUpdate();
        entityManager.flush();
    }
}
