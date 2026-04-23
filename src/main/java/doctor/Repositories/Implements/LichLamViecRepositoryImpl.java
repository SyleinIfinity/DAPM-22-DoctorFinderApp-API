package doctor.Repositories.Implements;

import doctor.Models.Entities.LichLamViec;
import doctor.Repositories.Interfaces.LichLamViecRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LichLamViecRepositoryImpl extends BaseRepositoryImpl<LichLamViec, Integer>
        implements LichLamViecRepository {
    public LichLamViecRepositoryImpl(EntityManager entityManager) {
        super(entityManager, LichLamViec.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichLamViec> findByMaBacSi(Integer maBacSi) {
        String jpql = "SELECT l FROM LichLamViec l WHERE l.maBacSi = :maBacSi ORDER BY l.maLichLamViec DESC";
        return entityManager
                .createQuery(jpql, LichLamViec.class)
                .setParameter("maBacSi", maBacSi)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichLamViec> findByMaBacSiAndNgayCuThe(Integer maBacSi, LocalDate ngayCuThe) {
        String jpql =
                "SELECT l FROM LichLamViec l WHERE l.maBacSi = :maBacSi AND l.ngayCuThe = :ngayCuThe ORDER BY l.gioBatDau";
        return entityManager
                .createQuery(jpql, LichLamViec.class)
                .setParameter("maBacSi", maBacSi)
                .setParameter("ngayCuThe", ngayCuThe)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichLamViec> findByMaBacSiAndThuTrongTuan(Integer maBacSi, Integer thuTrongTuan) {
        String jpql =
                "SELECT l FROM LichLamViec l WHERE l.maBacSi = :maBacSi AND l.thuTrongTuan = :thuTrongTuan ORDER BY l.gioBatDau";
        return entityManager
                .createQuery(jpql, LichLamViec.class)
                .setParameter("maBacSi", maBacSi)
                .setParameter("thuTrongTuan", thuTrongTuan)
                .getResultList();
    }
}
