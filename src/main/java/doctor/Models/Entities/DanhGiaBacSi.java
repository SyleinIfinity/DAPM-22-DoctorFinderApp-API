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
@Table(name = "danh_gia_bac_si")
public class DanhGiaBacSi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madanhgia")
    private Integer maDanhGia;

    @Column(name = "manguoidung", nullable = false)
    private Integer maNguoiDung;

    @Column(name = "mabacsi", nullable = false)
    private Integer maBacSi;

    @Column(name = "sosao", nullable = false)
    private Integer soSao;

    @Column(name = "noidung")
    private String noiDung;

    @Column(name = "thoigian", nullable = false)
    private LocalDateTime thoiGian;
}

