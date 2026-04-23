package doctor.Repositories.Implements;

import doctor.Models.Entities.TaiLieuBacSi;
import doctor.Repositories.Interfaces.TaiLieuBacSiRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TaiLieuBacSiRepositoryImpl extends BaseRepositoryImpl<TaiLieuBacSi, Integer>
        implements TaiLieuBacSiRepository {
    public TaiLieuBacSiRepositoryImpl(EntityManager entityManager) {
        super(entityManager, TaiLieuBacSi.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaiLieuBacSi> findByMaBacSi(Integer maBacSi) {
        String jpql = "SELECT t FROM TaiLieuBacSi t WHERE t.maBacSi = :maBacSi ORDER BY t.maTaiLieu";
        return entityManager
                .createQuery(jpql, TaiLieuBacSi.class)
                .setParameter("maBacSi", maBacSi)
                .getResultList();
    }
}
