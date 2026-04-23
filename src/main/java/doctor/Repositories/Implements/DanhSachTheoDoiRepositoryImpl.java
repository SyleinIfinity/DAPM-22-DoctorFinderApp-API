package doctor.Repositories.Implements;

import doctor.Models.Entities.DanhSachTheoDoi;
import doctor.Models.Entities.DanhSachTheoDoiId;
import doctor.Repositories.Interfaces.DanhSachTheoDoiRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DanhSachTheoDoiRepositoryImpl extends BaseRepositoryImpl<DanhSachTheoDoi, DanhSachTheoDoiId>
        implements DanhSachTheoDoiRepository {
    public DanhSachTheoDoiRepositoryImpl(EntityManager entityManager) {
        super(entityManager, DanhSachTheoDoi.class);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMaNguoiDungAndMaBacSi(Integer maNguoiDung, Integer maBacSi) {
        String jpql =
                "SELECT COUNT(d) FROM DanhSachTheoDoi d WHERE d.maNguoiDung = :maNguoiDung AND d.maBacSi = :maBacSi";
        Long count =
                entityManager
                        .createQuery(jpql, Long.class)
                        .setParameter("maNguoiDung", maNguoiDung)
                        .setParameter("maBacSi", maBacSi)
                        .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DanhSachTheoDoi> findByMaNguoiDung(Integer maNguoiDung) {
        String jpql =
                "SELECT d FROM DanhSachTheoDoi d WHERE d.maNguoiDung = :maNguoiDung ORDER BY d.ngayTheoDoi DESC";
        return entityManager
                .createQuery(jpql, DanhSachTheoDoi.class)
                .setParameter("maNguoiDung", maNguoiDung)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByMaBacSi(Integer maBacSi) {
        String jpql = "SELECT COUNT(d) FROM DanhSachTheoDoi d WHERE d.maBacSi = :maBacSi";
        Long count = entityManager.createQuery(jpql, Long.class).setParameter("maBacSi", maBacSi).getSingleResult();
        return count == null ? 0L : count;
    }
}
