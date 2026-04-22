package doctor.Models.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chi_tiet_lich")
public class ChiTietLich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "machitiet")
    private Integer maChiTiet;

    @Column(name = "malichlamviec", nullable = false)
    private Integer maLichLamViec;

    @Column(name = "giobatdau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gioketthuc", nullable = false)
    private LocalTime gioKetThuc;

    @Column(name = "trangthai", nullable = false, length = 50)
    private String trangThai;

    @Column(name = "khoaden")
    private LocalDateTime khoaDen;

    @Column(name = "maphieudatlichhientai")
    private Integer maPhieuDatLichHienTai;
}

