package doctor.Models.Entities;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DanhSachTheoDoiId implements Serializable {
    private Integer maNguoiDung;
    private Integer maBacSi;
}

