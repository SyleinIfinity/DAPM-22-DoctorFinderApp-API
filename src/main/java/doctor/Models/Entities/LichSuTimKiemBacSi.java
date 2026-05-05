package doctor.Models.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_su_tim_kiem_bac_si")
public class LichSuTimKiemBacSi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma")
    private Integer ma;

    @Column(name = "tukhoa", columnDefinition = "TEXT")
    private String tuKhoa;

    @Column(name = "chuyenkhoa", columnDefinition = "TEXT")
    private String chuyenKhoa;

    @Column(name = "mataikhoan")
    private Integer maTaiKhoan;

    @Column(name = "thoigian", nullable = false)
    private LocalDateTime thoiGian;
}
