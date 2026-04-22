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
@Table(name = "nguoi_dung")
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manguoidung")
    private Integer maNguoiDung;

    @Column(name = "mataikhoan", nullable = false)
    private Integer maTaiKhoan;

    @Column(name = "holot", nullable = false, length = 50)
    private String hoLot;

    @Column(name = "ten", nullable = false, length = 50)
    private String ten;

    @Column(name = "sodienthoai", nullable = false, length = 10)
    private String soDienThoai;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "cccd", nullable = false, length = 12)
    private String cccd;

    @Column(name = "anhdaidien")
    private String anhDaiDien;
}

