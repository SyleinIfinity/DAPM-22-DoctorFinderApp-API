package doctor.Repositories.Interfaces;

import doctor.Models.Entities.DanhSachTheoDoi;
import doctor.Models.Entities.DanhSachTheoDoiId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DanhSachTheoDoiRepository extends JpaRepository<DanhSachTheoDoi, DanhSachTheoDoiId> {}

