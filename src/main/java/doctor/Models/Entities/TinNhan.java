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
@Table(name = "tin_nhan")
public class TinNhan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matinnhan")
    private Integer maTinNhan;

    @Column(name = "macuochoithoai", nullable = false)
    private Integer maCuocHoiThoai;

    @Column(name = "mataikhoangui", nullable = false)
    private Integer maTaiKhoanGui;

    @Column(name = "loainoidung", nullable = false, length = 50)
    private String loaiNoiDung;

    @Column(name = "noidungtinnhan", nullable = false)
    private String noiDungTinNhan;

    @Column(name = "thoigiangui", nullable = false)
    private LocalDateTime thoiGianGui;
}

