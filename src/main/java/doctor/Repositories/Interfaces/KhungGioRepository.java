package doctor.Repositories.Interfaces;

import doctor.Models.Entities.KhungGio;
import java.util.Optional;

public interface KhungGioRepository extends BaseRepository<KhungGio, Integer> {
    Optional<KhungGio> findByThoiLuongPhut(Integer thoiLuongPhut);
}
