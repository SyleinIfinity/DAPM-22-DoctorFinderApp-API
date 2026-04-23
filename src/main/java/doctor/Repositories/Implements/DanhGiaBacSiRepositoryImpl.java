package doctor.Repositories.Implements;

import doctor.Models.Entities.DanhGiaBacSi;
import doctor.Repositories.Interfaces.DanhGiaBacSiRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DanhGiaBacSiRepositoryImpl extends BaseRepositoryImpl<DanhGiaBacSi, Integer>
        implements DanhGiaBacSiRepository {
    public DanhGiaBacSiRepositoryImpl(EntityManager entityManager) {
        super(entityManager, DanhGiaBacSi.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DanhGiaBacSi> findByMaBacSi(Integer maBacSi) {
        String jpql = "SELECT d FROM DanhGiaBacSi d WHERE d.maBacSi = :maBacSi ORDER BY d.thoiGian DESC";
        return entityManager
                .createQuery(jpql, DanhGiaBacSi.class)
                .setParameter("maBacSi", maBacSi)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DanhGiaBacSi> findByMaNguoiDungAndMaBacSi(Integer maNguoiDung, Integer maBacSi) {
        String jpql =
                "SELECT d FROM DanhGiaBacSi d WHERE d.maNguoiDung = :maNguoiDung AND d.maBacSi = :maBacSi";
        return entityManager
                .createQuery(jpql, DanhGiaBacSi.class)
                .setParameter("maNguoiDung", maNguoiDung)
                .setParameter("maBacSi", maBacSi)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByMaBacSi(Integer maBacSi) {
        String jpql = "SELECT COUNT(d) FROM DanhGiaBacSi d WHERE d.maBacSi = :maBacSi";
        Long count = entityManager.createQuery(jpql, Long.class).setParameter("maBacSi", maBacSi).getSingleResult();
        return count == null ? 0L : count;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAvgSoSaoByMaBacSi(Integer maBacSi) {
        String jpql = "SELECT AVG(d.soSao) FROM DanhGiaBacSi d WHERE d.maBacSi = :maBacSi";
        return entityManager.createQuery(jpql, Double.class).setParameter("maBacSi", maBacSi).getSingleResult();
    }
}
