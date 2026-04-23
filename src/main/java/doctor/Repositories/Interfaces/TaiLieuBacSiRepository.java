package doctor.Repositories.Interfaces;

import doctor.Models.Entities.TaiLieuBacSi;
import java.util.List;

public interface TaiLieuBacSiRepository extends BaseRepository<TaiLieuBacSi, Integer> {
    List<TaiLieuBacSi> findByMaBacSi(Integer maBacSi);
}
