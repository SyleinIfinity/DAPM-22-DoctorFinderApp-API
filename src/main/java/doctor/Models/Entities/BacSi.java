package doctor.Models.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bac_si")
public class BacSi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mabacsi")
    private Integer maBacSi;

    @Column(name = "mataikhoan", nullable = false)
    private Integer maTaiKhoan;

    @Column(name = "chuyenkhoa", nullable = false, length = 100)
    private String chuyenKhoa;

    @Column(name = "trinhdochuyenmon", nullable = false, length = 100)
    private String trinhDoChuyenMon;

    @Column(name = "loaihinhbacsi", nullable = false, length = 50)
    private String loaiHinhBacSi;

    @Column(name = "tencosoyte", nullable = false, length = 100)
    private String tenCoSoYTe;

    @Column(name = "diachilamviec")
    private String diaChiLamViec;

    @Column(name = "machungchihanhnghe", nullable = false, length = 50)
    private String maChungChiHanhNghe;

    @Column(name = "motabanthan")
    private String moTaBanThan;

    @Column(name = "trangthaihoso", nullable = false, length = 50)
    private String trangThaiHoSo;
}

