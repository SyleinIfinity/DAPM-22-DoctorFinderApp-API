package doctor.Repositories.Interfaces;

import doctor.Models.Entities.DanhSachTheoDoi;
import doctor.Models.Entities.DanhSachTheoDoiId;
import java.util.List;

public interface DanhSachTheoDoiRepository extends BaseRepository<DanhSachTheoDoi, DanhSachTheoDoiId> {
    boolean existsByMaNguoiDungAndMaBacSi(Integer maNguoiDung, Integer maBacSi);

    List<DanhSachTheoDoi> findByMaNguoiDung(Integer maNguoiDung);

    long countByMaBacSi(Integer maBacSi);
}
