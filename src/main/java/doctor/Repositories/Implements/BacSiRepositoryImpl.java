package doctor.Repositories.Implements;

import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.NguoiDung;
import doctor.Repositories.Interfaces.BacSiRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class BacSiRepositoryImpl extends BaseRepositoryImpl<BacSi, Integer> implements BacSiRepository {
    public BacSiRepositoryImpl(EntityManager entityManager) {
        super(entityManager, BacSi.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BacSi> findByMaTaiKhoan(Integer maTaiKhoan) {
        String jpql = "SELECT b FROM BacSi b WHERE b.maTaiKhoan = :maTaiKhoan";
        return entityManager
                .createQuery(jpql, BacSi.class)
                .setParameter("maTaiKhoan", maTaiKhoan)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BacSi> findByMaChungChiHanhNghe(String maChungChiHanhNghe) {
        String jpql = "SELECT b FROM BacSi b WHERE b.maChungChiHanhNghe = :maChungChiHanhNghe";
        return entityManager
                .createQuery(jpql, BacSi.class)
                .setParameter("maChungChiHanhNghe", maChungChiHanhNghe)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BacSi> findByTrangThaiHoSo(String trangThaiHoSo) {
        String jpql = "SELECT b FROM BacSi b WHERE b.trangThaiHoSo = :trangThaiHoSo ORDER BY b.maBacSi";
        return entityManager
                .createQuery(jpql, BacSi.class)
                .setParameter("trangThaiHoSo", trangThaiHoSo)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BacSi> search(
            String keyword,
            String chuyenKhoa,
            String diaChiLamViec,
            String trangThaiHoSo,
            Integer limit,
            Integer offset) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasChuyenKhoa = chuyenKhoa != null && !chuyenKhoa.isBlank();
        boolean hasDiaChiLamViec = diaChiLamViec != null && !diaChiLamViec.isBlank();
        boolean hasTrangThaiHoSo = trangThaiHoSo != null && !trangThaiHoSo.isBlank();

        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT b FROM BacSi b, ").append(NguoiDung.class.getSimpleName()).append(" n ");
        jpql.append("WHERE b.maTaiKhoan = n.maTaiKhoan");

        if (hasTrangThaiHoSo) {
            jpql.append(" AND b.trangThaiHoSo = :trangThaiHoSo");
        }
        if (hasChuyenKhoa) {
            jpql.append(" AND LOWER(b.chuyenKhoa) LIKE :chuyenKhoa");
        }
        if (hasDiaChiLamViec) {
            jpql.append(" AND LOWER(COALESCE(b.diaChiLamViec, '')) LIKE :diaChiLamViec");
        }
        if (hasKeyword) {
            jpql.append(" AND (");
            jpql.append("LOWER(CONCAT(CONCAT(n.hoLot, ' '), n.ten)) LIKE :keyword");
            jpql.append(" OR LOWER(n.hoLot) LIKE :keyword");
            jpql.append(" OR LOWER(n.ten) LIKE :keyword");
            jpql.append(" OR LOWER(b.chuyenKhoa) LIKE :keyword");
            jpql.append(" OR LOWER(b.tenCoSoYTe) LIKE :keyword");
            jpql.append(" OR LOWER(COALESCE(b.diaChiLamViec, '')) LIKE :keyword");
            jpql.append(" OR LOWER(b.maChungChiHanhNghe) LIKE :keyword");
            jpql.append(")");
        }

        jpql.append(" ORDER BY n.hoLot, n.ten, b.maBacSi");

        TypedQuery<BacSi> query = entityManager.createQuery(jpql.toString(), BacSi.class);

        if (hasTrangThaiHoSo) {
            query.setParameter("trangThaiHoSo", trangThaiHoSo);
        }
        if (hasChuyenKhoa) {
            query.setParameter("chuyenKhoa", "%" + chuyenKhoa.toLowerCase() + "%");
        }
        if (hasDiaChiLamViec) {
            query.setParameter("diaChiLamViec", "%" + diaChiLamViec.toLowerCase() + "%");
        }
        if (hasKeyword) {
            query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }
        if (offset != null && offset >= 0) {
            query.setFirstResult(offset);
        }
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }
}
