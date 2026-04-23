package doctor.Repositories.Interfaces;

import doctor.Models.Entities.LichLamViec;
import java.time.LocalDate;
import java.util.List;

public interface LichLamViecRepository extends BaseRepository<LichLamViec, Integer> {
    List<LichLamViec> findByMaBacSi(Integer maBacSi);

    List<LichLamViec> findByMaBacSiAndNgayCuThe(Integer maBacSi, LocalDate ngayCuThe);

    List<LichLamViec> findByMaBacSiAndThuTrongTuan(Integer maBacSi, Integer thuTrongTuan);
}
