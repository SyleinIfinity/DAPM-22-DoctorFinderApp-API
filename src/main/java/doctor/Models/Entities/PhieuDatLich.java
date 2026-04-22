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
@Table(name = "phieu_dat_lich")
public class PhieuDatLich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maphieudatlich")
    private Integer maPhieuDatLich;

    @Column(name = "manguoidung", nullable = false)
    private Integer maNguoiDung;

    @Column(name = "machitiet", nullable = false)
    private Integer maChiTiet;

    @Column(name = "loaiphieu", nullable = false, length = 50)
    private String loaiPhieu;

    @Column(name = "trieuchungghichu")
    private String trieuChungGhiChu;

    @Column(name = "trangthaiphieu", nullable = false, length = 50)
    private String trangThaiPhieu;

    @Column(name = "lydotuchoi")
    private String lyDoTuChoi;
}

