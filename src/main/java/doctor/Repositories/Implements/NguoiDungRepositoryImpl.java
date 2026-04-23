package doctor.Repositories.Implements;

import doctor.Models.Entities.NguoiDung;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NguoiDungRepositoryImpl extends BaseRepositoryImpl<NguoiDung, Integer>
        implements NguoiDungRepository {
    public NguoiDungRepositoryImpl(EntityManager entityManager) {
        super(entityManager, NguoiDung.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NguoiDung> findByMaTaiKhoan(Integer maTaiKhoan) {
        String jpql = "SELECT n FROM NguoiDung n WHERE n.maTaiKhoan = :maTaiKhoan";
        return entityManager
                .createQuery(jpql, NguoiDung.class)
                .setParameter("maTaiKhoan", maTaiKhoan)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NguoiDung> findByEmail(String email) {
        String jpql = "SELECT n FROM NguoiDung n WHERE n.email = :email";
        return entityManager
                .createQuery(jpql, NguoiDung.class)
                .setParameter("email", email)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NguoiDung> findBySoDienThoai(String soDienThoai) {
        String jpql = "SELECT n FROM NguoiDung n WHERE n.soDienThoai = :soDienThoai";
        return entityManager
                .createQuery(jpql, NguoiDung.class)
                .setParameter("soDienThoai", soDienThoai)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        String jpql = "SELECT COUNT(n) FROM NguoiDung n WHERE n.email = :email";
        Long count =
                entityManager.createQuery(jpql, Long.class).setParameter("email", email).getSingleResult();
        return count != null && count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySoDienThoai(String soDienThoai) {
        String jpql = "SELECT COUNT(n) FROM NguoiDung n WHERE n.soDienThoai = :soDienThoai";
        Long count =
                entityManager
                        .createQuery(jpql, Long.class)
                        .setParameter("soDienThoai", soDienThoai)
                        .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCccd(String cccd) {
        String jpql = "SELECT COUNT(n) FROM NguoiDung n WHERE n.cccd = :cccd";
        Long count = entityManager.createQuery(jpql, Long.class).setParameter("cccd", cccd).getSingleResult();
        return count != null && count > 0;
    }
}
