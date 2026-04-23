package doctor.Repositories.Implements;

import doctor.Models.Entities.ChiTietLich;
import doctor.Repositories.Interfaces.ChiTietLichRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ChiTietLichRepositoryImpl extends BaseRepositoryImpl<ChiTietLich, Integer>
        implements ChiTietLichRepository {
    public ChiTietLichRepositoryImpl(EntityManager entityManager) {
        super(entityManager, ChiTietLich.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietLich> findByMaLichLamViec(Integer maLichLamViec) {
        String jpql =
                "SELECT c FROM ChiTietLich c WHERE c.maLichLamViec = :maLichLamViec ORDER BY c.gioBatDau";
        return entityManager
                .createQuery(jpql, ChiTietLich.class)
                .setParameter("maLichLamViec", maLichLamViec)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietLich> findAvailableByMaLichLamViec(Integer maLichLamViec) {
        LocalDateTime now = LocalDateTime.now();
        String jpql =
                "SELECT c FROM ChiTietLich c "
                        + "WHERE c.maLichLamViec = :maLichLamViec "
                        + "AND (c.trangThai = 'TRONG' OR (c.trangThai = 'DANG_GIU' AND (c.khoaDen IS NULL OR c.khoaDen <= :now))) "
                        + "ORDER BY c.gioBatDau";
        return entityManager
                .createQuery(jpql, ChiTietLich.class)
                .setParameter("maLichLamViec", maLichLamViec)
                .setParameter("now", now)
                .getResultList();
    }

    @Override
    @Transactional
    public Optional<ChiTietLich> selectByIdForUpdate(Integer maChiTiet) {
        return Optional.ofNullable(entityManager.find(ChiTietLich.class, maChiTiet, LockModeType.PESSIMISTIC_WRITE));
    }
}
