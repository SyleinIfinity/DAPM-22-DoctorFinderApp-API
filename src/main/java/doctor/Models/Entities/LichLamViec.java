package doctor.Models.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "lich_lam_viec")
public class LichLamViec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "malichlamviec")
    private Integer maLichLamViec;

    @Column(name = "mabacsi", nullable = false)
    private Integer maBacSi;

    @Column(name = "thutrongtuan")
    private Integer thuTrongTuan;

    @Column(name = "giobatdau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gioketthuc", nullable = false)
    private LocalTime gioKetThuc;

    @Column(name = "ngaycuthe")
    private LocalDate ngayCuThe;

    @Column(name = "makhunggio", nullable = false)
    private Integer maKhungGio;

    @Column(name = "soluongtoida", nullable = false)
    private Integer soLuongToiDa;

    @Column(name = "trangthailich", nullable = false, length = 50)
    private String trangThaiLich;
}

