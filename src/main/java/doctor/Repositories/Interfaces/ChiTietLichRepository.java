package doctor.Repositories.Interfaces;

import doctor.Models.Entities.ChiTietLich;
import java.util.List;
import java.util.Optional;

public interface ChiTietLichRepository extends BaseRepository<ChiTietLich, Integer> {
    List<ChiTietLich> findByMaLichLamViec(Integer maLichLamViec);

    List<ChiTietLich> findAvailableByMaLichLamViec(Integer maLichLamViec);

    Optional<ChiTietLich> selectByIdForUpdate(Integer maChiTiet);
}
