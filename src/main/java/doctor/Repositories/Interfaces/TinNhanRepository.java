package doctor.Repositories.Interfaces;

import doctor.Models.Entities.TinNhan;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface TinNhanRepository extends BaseRepository<TinNhan, Integer> {
    List<TinNhan> findByMaCuocHoiThoai(Integer maCuocHoiThoai);

    List<TinNhan> findByMaCuocHoiThoai(Integer maCuocHoiThoai, int limit, int offset);

    List<TinNhan> findLatestByMaCuocHoiThoai(Integer maCuocHoiThoai, int limit);

    List<TinNhan> findByMaCuocHoiThoaiBefore(Integer maCuocHoiThoai, LocalDateTime before, int limit);

    Optional<TinNhan> findLastByMaCuocHoiThoai(Integer maCuocHoiThoai);
}
