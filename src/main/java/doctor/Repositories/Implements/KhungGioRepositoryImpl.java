package doctor.Repositories.Implements;

import doctor.Models.Entities.KhungGio;
import doctor.Repositories.Interfaces.KhungGioRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class KhungGioRepositoryImpl extends BaseRepositoryImpl<KhungGio, Integer>
        implements KhungGioRepository {
    public KhungGioRepositoryImpl(EntityManager entityManager) {
        super(entityManager, KhungGio.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KhungGio> findByThoiLuongPhut(Integer thoiLuongPhut) {
        String jpql = "SELECT k FROM KhungGio k WHERE k.thoiLuongPhut = :thoiLuongPhut";
        return entityManager
                .createQuery(jpql, KhungGio.class)
                .setParameter("thoiLuongPhut", thoiLuongPhut)
                .getResultList()
                .stream()
                .findFirst();
    }
}
