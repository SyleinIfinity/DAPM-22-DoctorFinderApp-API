package doctor.Repositories.Interfaces;

import doctor.Models.Entities.NguoiDung;
import java.util.Optional;

public interface NguoiDungRepository extends BaseRepository<NguoiDung, Integer> {
    Optional<NguoiDung> findByMaTaiKhoan(Integer maTaiKhoan);

    Optional<NguoiDung> findByEmail(String email);

    Optional<NguoiDung> findBySoDienThoai(String soDienThoai);

    Optional<NguoiDung> findByCccd(String cccd);

    boolean existsByEmail(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByCccd(String cccd);
}
