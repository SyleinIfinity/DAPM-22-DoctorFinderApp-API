package doctor.Repositories.Interfaces;

import doctor.Models.Entities.PhieuDatLich;
import java.util.List;

public interface PhieuDatLichRepository extends BaseRepository<PhieuDatLich, Integer> {
    List<PhieuDatLich> findByMaNguoiDung(Integer maNguoiDung);

    List<PhieuDatLich> findByMaChiTiet(Integer maChiTiet);

    List<PhieuDatLich> findByMaBacSi(Integer maBacSi);

    List<PhieuDatLich> findByMaBacSiAndTrangThaiPhieu(Integer maBacSi, String trangThaiPhieu);
}
