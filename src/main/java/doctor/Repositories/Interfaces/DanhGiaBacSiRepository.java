package doctor.Repositories.Interfaces;

import doctor.Models.Entities.DanhGiaBacSi;
import java.util.List;
import java.util.Optional;

public interface DanhGiaBacSiRepository extends BaseRepository<DanhGiaBacSi, Integer> {
    List<DanhGiaBacSi> findByMaBacSi(Integer maBacSi);

    Optional<DanhGiaBacSi> findByMaNguoiDungAndMaBacSi(Integer maNguoiDung, Integer maBacSi);

    long countByMaBacSi(Integer maBacSi);

    Double getAvgSoSaoByMaBacSi(Integer maBacSi);
}
