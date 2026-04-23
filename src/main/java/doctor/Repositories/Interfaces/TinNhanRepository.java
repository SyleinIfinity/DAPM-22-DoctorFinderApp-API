package doctor.Repositories.Interfaces;

import doctor.Models.Entities.TinNhan;
import java.util.List;
import java.util.Optional;

public interface TinNhanRepository extends BaseRepository<TinNhan, Integer> {
    List<TinNhan> findByMaCuocHoiThoai(Integer maCuocHoiThoai);

    List<TinNhan> findByMaCuocHoiThoai(Integer maCuocHoiThoai, int limit, int offset);

    Optional<TinNhan> findLastByMaCuocHoiThoai(Integer maCuocHoiThoai);
}
