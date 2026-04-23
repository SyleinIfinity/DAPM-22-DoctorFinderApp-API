package doctor.Repositories.Interfaces;

import doctor.Models.Entities.CuocHoiThoai;
import java.util.List;
import java.util.Optional;

public interface CuocHoiThoaiRepository extends BaseRepository<CuocHoiThoai, Integer> {
    Optional<CuocHoiThoai> findByMaNguoiDungAndMaBacSi(Integer maNguoiDung, Integer maBacSi);

    List<CuocHoiThoai> findByMaNguoiDung(Integer maNguoiDung);

    List<CuocHoiThoai> findByMaBacSi(Integer maBacSi);
}
