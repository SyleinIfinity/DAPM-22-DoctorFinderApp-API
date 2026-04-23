package doctor.Repositories.Implements;

import doctor.Models.Entities.ChiTietLich;
import doctor.Models.Entities.LichLamViec;
import doctor.Models.Entities.PhieuDatLich;
import doctor.Repositories.Interfaces.PhieuDatLichRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PhieuDatLichRepositoryImpl extends BaseRepositoryImpl<PhieuDatLich, Integer>
        implements PhieuDatLichRepository {
    public PhieuDatLichRepositoryImpl(EntityManager entityManager) {
        super(entityManager, PhieuDatLich.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhieuDatLich> findByMaNguoiDung(Integer maNguoiDung) {
        String jpql =
                "SELECT p FROM PhieuDatLich p WHERE p.maNguoiDung = :maNguoiDung ORDER BY p.maPhieuDatLich DESC";
        return entityManager
                .createQuery(jpql, PhieuDatLich.class)
                .setParameter("maNguoiDung", maNguoiDung)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhieuDatLich> findByMaChiTiet(Integer maChiTiet) {
        String jpql = "SELECT p FROM PhieuDatLich p WHERE p.maChiTiet = :maChiTiet ORDER BY p.maPhieuDatLich DESC";
        return entityManager
                .createQuery(jpql, PhieuDatLich.class)
                .setParameter("maChiTiet", maChiTiet)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhieuDatLich> findByMaBacSi(Integer maBacSi) {
        String jpql =
                "SELECT p FROM PhieuDatLich p, "
                        + ChiTietLich.class.getSimpleName()
                        + " c, "
                        + LichLamViec.class.getSimpleName()
                        + " l "
                        + "WHERE p.maChiTiet = c.maChiTiet "
                        + "AND c.maLichLamViec = l.maLichLamViec "
                        + "AND l.maBacSi = :maBacSi "
                        + "ORDER BY p.maPhieuDatLich DESC";
        return entityManager
                .createQuery(jpql, PhieuDatLich.class)
                .setParameter("maBacSi", maBacSi)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhieuDatLich> findByMaBacSiAndTrangThaiPhieu(Integer maBacSi, String trangThaiPhieu) {
        String jpql =
                "SELECT p FROM PhieuDatLich p, "
                        + ChiTietLich.class.getSimpleName()
                        + " c, "
                        + LichLamViec.class.getSimpleName()
                        + " l "
                        + "WHERE p.maChiTiet = c.maChiTiet "
                        + "AND c.maLichLamViec = l.maLichLamViec "
                        + "AND l.maBacSi = :maBacSi "
                        + "AND p.trangThaiPhieu = :trangThaiPhieu "
                        + "ORDER BY p.maPhieuDatLich DESC";
        return entityManager
                .createQuery(jpql, PhieuDatLich.class)
                .setParameter("maBacSi", maBacSi)
                .setParameter("trangThaiPhieu", trangThaiPhieu)
                .getResultList();
    }
}
