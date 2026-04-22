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
@Table(name = "tai_khoan")
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mataikhoan")
    private Integer maTaiKhoan;

    @Column(name = "tendangnhap", nullable = false, length = 50)
    private String tenDangNhap;

    @Column(name = "matkhauhash", nullable = false, length = 255)
    private String matKhauHash;

    @Column(name = "vaitro", nullable = false, length = 50)
    private String vaiTro;

    @Column(name = "trangthaihoatdong", nullable = false, length = 50)
    private String trangThaiHoatDong;

    @Column(name = "ngaytao", nullable = false)
    private LocalDateTime ngayTao;
}

