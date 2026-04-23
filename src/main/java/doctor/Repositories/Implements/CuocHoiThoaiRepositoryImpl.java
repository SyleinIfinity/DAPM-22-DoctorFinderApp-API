package doctor.Repositories.Implements;

import doctor.Models.Entities.CuocHoiThoai;
import doctor.Repositories.Interfaces.CuocHoiThoaiRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CuocHoiThoaiRepositoryImpl extends BaseRepositoryImpl<CuocHoiThoai, Integer>
        implements CuocHoiThoaiRepository {
    public CuocHoiThoaiRepositoryImpl(EntityManager entityManager) {
        super(entityManager, CuocHoiThoai.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CuocHoiThoai> findByMaNguoiDungAndMaBacSi(Integer maNguoiDung, Integer maBacSi) {
        String jpql =
                "SELECT c FROM CuocHoiThoai c WHERE c.maNguoiDung = :maNguoiDung AND c.maBacSi = :maBacSi";
        return entityManager
                .createQuery(jpql, CuocHoiThoai.class)
                .setParameter("maNguoiDung", maNguoiDung)
                .setParameter("maBacSi", maBacSi)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuocHoiThoai> findByMaNguoiDung(Integer maNguoiDung) {
        String jpql = "SELECT c FROM CuocHoiThoai c WHERE c.maNguoiDung = :maNguoiDung ORDER BY c.ngayTao DESC";
        return entityManager
                .createQuery(jpql, CuocHoiThoai.class)
                .setParameter("maNguoiDung", maNguoiDung)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuocHoiThoai> findByMaBacSi(Integer maBacSi) {
        String jpql = "SELECT c FROM CuocHoiThoai c WHERE c.maBacSi = :maBacSi ORDER BY c.ngayTao DESC";
        return entityManager
                .createQuery(jpql, CuocHoiThoai.class)
                .setParameter("maBacSi", maBacSi)
                .getResultList();
    }
}
