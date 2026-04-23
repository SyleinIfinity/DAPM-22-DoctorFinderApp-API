package doctor.Repositories.Interfaces;

import doctor.Models.Entities.TaiKhoan;
import java.util.Optional;

public interface TaiKhoanRepository extends BaseRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);
}
