package doctor.Models.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "danh_sach_theo_doi")
@IdClass(DanhSachTheoDoiId.class)
public class DanhSachTheoDoi {
    @Id
    @Column(name = "manguoidung")
    private Integer maNguoiDung;

    @Id
    @Column(name = "mabacsi")
    private Integer maBacSi;

    @Column(name = "ngaytheodoi", nullable = false)
    private LocalDateTime ngayTheoDoi;
}

