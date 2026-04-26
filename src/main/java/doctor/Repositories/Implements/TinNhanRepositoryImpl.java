package doctor.Repositories.Implements;

import doctor.Models.Entities.TinNhan;
import doctor.Repositories.Interfaces.TinNhanRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TinNhanRepositoryImpl extends BaseRepositoryImpl<TinNhan, Integer>
        implements TinNhanRepository {
    public TinNhanRepositoryImpl(EntityManager entityManager) {
        super(entityManager, TinNhan.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TinNhan> findByMaCuocHoiThoai(Integer maCuocHoiThoai) {
        String jpql =
                "SELECT t FROM TinNhan t WHERE t.maCuocHoiThoai = :maCuocHoiThoai ORDER BY t.thoiGianGui ASC";
        return entityManager
                .createQuery(jpql, TinNhan.class)
                .setParameter("maCuocHoiThoai", maCuocHoiThoai)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TinNhan> findByMaCuocHoiThoai(Integer maCuocHoiThoai, int limit, int offset) {
        String jpql =
                "SELECT t FROM TinNhan t WHERE t.maCuocHoiThoai = :maCuocHoiThoai ORDER BY t.thoiGianGui ASC";
        return entityManager
                .createQuery(jpql, TinNhan.class)
                .setParameter("maCuocHoiThoai", maCuocHoiThoai)
                .setFirstResult(Math.max(0, offset))
                .setMaxResults(Math.max(0, limit))
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TinNhan> findLatestByMaCuocHoiThoai(Integer maCuocHoiThoai, int limit) {
        String jpql =
                "SELECT t FROM TinNhan t WHERE t.maCuocHoiThoai = :maCuocHoiThoai ORDER BY t.thoiGianGui DESC";
        List<TinNhan> fetched =
                entityManager
                        .createQuery(jpql, TinNhan.class)
                        .setParameter("maCuocHoiThoai", maCuocHoiThoai)
                        .setMaxResults(Math.max(0, limit))
                        .getResultList();
        Collections.reverse(fetched);
        return fetched;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TinNhan> findByMaCuocHoiThoaiBefore(Integer maCuocHoiThoai, LocalDateTime before, int limit) {
        String jpql =
                "SELECT t FROM TinNhan t "
                        + "WHERE t.maCuocHoiThoai = :maCuocHoiThoai "
                        + "AND t.thoiGianGui < :before "
                        + "ORDER BY t.thoiGianGui DESC";
        List<TinNhan> fetched =
                entityManager
                        .createQuery(jpql, TinNhan.class)
                        .setParameter("maCuocHoiThoai", maCuocHoiThoai)
                        .setParameter("before", before)
                        .setMaxResults(Math.max(0, limit))
                        .getResultList();
        Collections.reverse(fetched);
        return fetched;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TinNhan> findLastByMaCuocHoiThoai(Integer maCuocHoiThoai) {
        String jpql =
                "SELECT t FROM TinNhan t WHERE t.maCuocHoiThoai = :maCuocHoiThoai ORDER BY t.thoiGianGui DESC";
        return entityManager
                .createQuery(jpql, TinNhan.class)
                .setParameter("maCuocHoiThoai", maCuocHoiThoai)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }
}
