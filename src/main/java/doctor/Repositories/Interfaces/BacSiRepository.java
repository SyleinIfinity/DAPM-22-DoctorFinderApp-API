package doctor.Repositories.Interfaces;

import doctor.Models.Entities.BacSi;
import java.util.List;
import java.util.Optional;

public interface BacSiRepository extends BaseRepository<BacSi, Integer> {
    Optional<BacSi> findByMaTaiKhoan(Integer maTaiKhoan);

    Optional<BacSi> findByMaChungChiHanhNghe(String maChungChiHanhNghe);

    List<BacSi> findByTrangThaiHoSo(String trangThaiHoSo);

    List<BacSi> search(
            String keyword,
            String chuyenKhoa,
            String diaChiLamViec,
            String trangThaiHoSo,
            Integer limit,
            Integer offset);
}
