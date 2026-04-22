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
@Table(name = "tai_lieu_bac_si")
public class TaiLieuBacSi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matailieu")
    private Integer maTaiLieu;

    @Column(name = "mabacsi", nullable = false)
    private Integer maBacSi;

    @Column(name = "tieudetailieu", nullable = false, length = 100)
    private String tieuDeTaiLieu;

    @Column(name = "duongdanfileurl", nullable = false)
    private String duongDanFileUrl;
}

