package doctor.Repositories.Implements;

import doctor.Models.Entities.TaiKhoan;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TaiKhoanRepositoryImpl extends BaseRepositoryImpl<TaiKhoan, Integer>
        implements TaiKhoanRepository {
    public TaiKhoanRepositoryImpl(EntityManager entityManager) {
        super(entityManager, TaiKhoan.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap) {
        String jpql = "SELECT t FROM TaiKhoan t WHERE t.tenDangNhap = :tenDangNhap";
        return entityManager
                .createQuery(jpql, TaiKhoan.class)
                .setParameter("tenDangNhap", tenDangNhap)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTenDangNhap(String tenDangNhap) {
        String jpql = "SELECT COUNT(t) FROM TaiKhoan t WHERE t.tenDangNhap = :tenDangNhap";
        Long count =
                entityManager
                        .createQuery(jpql, Long.class)
                        .setParameter("tenDangNhap", tenDangNhap)
                        .getSingleResult();
        return count != null && count > 0;
    }
}
