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
@Table(name = "cuoc_hoi_thoai")
public class CuocHoiThoai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "macuochoithoai")
    private Integer maCuocHoiThoai;

    @Column(name = "manguoidung", nullable = false)
    private Integer maNguoiDung;

    @Column(name = "mabacsi", nullable = false)
    private Integer maBacSi;

    @Column(name = "ngaytao", nullable = false)
    private LocalDateTime ngayTao;
}

