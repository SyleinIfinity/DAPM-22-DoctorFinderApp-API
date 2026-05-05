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
@Table(name = "luot_xem_ho_so_bac_si")
public class LuotXemHoSoBacSi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_luot")
    private Integer maLuot;

    @Column(name = "mabacsi", nullable = false)
    private Integer maBacSi;

    @Column(name = "mataikhoan")
    private Integer maTaiKhoan;

    @Column(name = "thoigian", nullable = false)
    private LocalDateTime thoiGian;
}
