package doctor.Services.Business.Follows;

import doctor.Models.DTOs.Follows.Responses.FollowActionResponseDto;
import doctor.Models.DTOs.Follows.Responses.FollowedDoctorResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.DanhSachTheoDoi;
import doctor.Models.Entities.DanhSachTheoDoiId;
import doctor.Models.Entities.NguoiDung;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.DanhSachTheoDoiRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Services.Interfaces.Follows.FollowService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final DanhSachTheoDoiRepository danhSachTheoDoiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BacSiRepository bacSiRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FollowedDoctorResponseDto> getFollowedDoctors(Integer maNguoiDung) {
        Integer normalizedMaNguoiDung = normalizePositiveId(maNguoiDung, "maNguoiDung");
        requireNguoiDung(normalizedMaNguoiDung);

        return danhSachTheoDoiRepository.findByMaNguoiDung(normalizedMaNguoiDung).stream()
                .map(this::mapToFollowedDoctor)
                .toList();
    }

    @Override
    @Transactional
    public FollowActionResponseDto followDoctor(Integer maNguoiDung, Integer maBacSi) {
        Integer normalizedMaNguoiDung = normalizePositiveId(maNguoiDung, "maNguoiDung");
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        requireNguoiDung(normalizedMaNguoiDung);
        requireBacSi(normalizedMaBacSi);

        DanhSachTheoDoiId id = new DanhSachTheoDoiId(normalizedMaNguoiDung, normalizedMaBacSi);
        DanhSachTheoDoi existing = danhSachTheoDoiRepository.selectById(id).orElse(null);
        if (existing != null) {
            return new FollowActionResponseDto(
                    true,
                    "Da theo doi bac si",
                    normalizedMaNguoiDung,
                    normalizedMaBacSi,
                    true,
                    existing.getNgayTheoDoi());
        }

        DanhSachTheoDoi created =
                danhSachTheoDoiRepository.insert(
                        new DanhSachTheoDoi(
                                normalizedMaNguoiDung, normalizedMaBacSi, LocalDateTime.now()));

        return new FollowActionResponseDto(
                true,
                "Theo doi bac si thanh cong",
                normalizedMaNguoiDung,
                normalizedMaBacSi,
                true,
                created.getNgayTheoDoi());
    }

    @Override
    @Transactional
    public FollowActionResponseDto unfollowDoctor(Integer maNguoiDung, Integer maBacSi) {
        Integer normalizedMaNguoiDung = normalizePositiveId(maNguoiDung, "maNguoiDung");
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        requireNguoiDung(normalizedMaNguoiDung);
        requireBacSi(normalizedMaBacSi);

        DanhSachTheoDoiId id = new DanhSachTheoDoiId(normalizedMaNguoiDung, normalizedMaBacSi);
        DanhSachTheoDoi existing = danhSachTheoDoiRepository.selectById(id).orElse(null);
        if (existing != null) {
            danhSachTheoDoiRepository.deleteById(id);
        }

        return new FollowActionResponseDto(
                true,
                existing == null ? "Ban chua theo doi bac si nay" : "Huy theo doi thanh cong",
                normalizedMaNguoiDung,
                normalizedMaBacSi,
                false,
                null);
    }

    private FollowedDoctorResponseDto mapToFollowedDoctor(DanhSachTheoDoi follow) {
        if (follow == null) {
            throw new IllegalArgumentException("follow is required");
        }

        Integer maNguoiDung = follow.getMaNguoiDung();
        Integer maBacSi = follow.getMaBacSi();
        BacSi bacSi = requireBacSi(maBacSi);
        NguoiDung thongTinBacSi = nguoiDungRepository.findByMaTaiKhoan(bacSi.getMaTaiKhoan()).orElse(null);

        String hoTenBacSi =
                thongTinBacSi == null
                        ? null
                        : buildHoTenDayDu(thongTinBacSi.getHoLot(), thongTinBacSi.getTen());
        String anhDaiDienBacSi = thongTinBacSi == null ? null : thongTinBacSi.getAnhDaiDien();

        return new FollowedDoctorResponseDto(
                maNguoiDung,
                maBacSi,
                hoTenBacSi,
                bacSi.getChuyenKhoa(),
                bacSi.getTenCoSoYTe(),
                bacSi.getDiaChiLamViec(),
                anhDaiDienBacSi,
                follow.getNgayTheoDoi());
    }

    private NguoiDung requireNguoiDung(Integer maNguoiDung) {
        return nguoiDungRepository
                .selectById(maNguoiDung)
                .orElseThrow(() -> new IllegalArgumentException("Nguoi dung khong ton tai"));
    }

    private BacSi requireBacSi(Integer maBacSi) {
        return bacSiRepository
                .selectById(maBacSi)
                .orElseThrow(() -> new IllegalArgumentException("Bac si khong ton tai"));
    }

    private Integer normalizePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return id;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String buildHoTenDayDu(String hoLot, String ten) {
        String normalizedHoLot = normalizeOptional(hoLot);
        String normalizedTen = normalizeOptional(ten);
        if (normalizedHoLot == null) {
            return normalizedTen;
        }
        if (normalizedTen == null) {
            return normalizedHoLot;
        }
        return normalizedHoLot + " " + normalizedTen;
    }
}

