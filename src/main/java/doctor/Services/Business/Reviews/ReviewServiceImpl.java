package doctor.Services.Business.Reviews;

import doctor.Models.DTOs.Reviews.Requests.CreateReviewRequestDto;
import doctor.Models.DTOs.Reviews.Responses.DoctorRatingSummaryResponseDto;
import doctor.Models.DTOs.Reviews.Responses.ReviewResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.ChiTietLich;
import doctor.Models.Entities.DanhGiaBacSi;
import doctor.Models.Entities.LichLamViec;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.PhieuDatLich;
import doctor.Models.Enums.TrangThaiPhieuDatLich;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.ChiTietLichRepository;
import doctor.Repositories.Interfaces.DanhGiaBacSiRepository;
import doctor.Repositories.Interfaces.LichLamViecRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.PhieuDatLichRepository;
import doctor.Services.Interfaces.Reviews.ReviewService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private static final int SO_SAO_MIN = 1;
    private static final int SO_SAO_MAX = 5;

    private final DanhGiaBacSiRepository danhGiaBacSiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BacSiRepository bacSiRepository;
    private final PhieuDatLichRepository phieuDatLichRepository;
    private final ChiTietLichRepository chiTietLichRepository;
    private final LichLamViecRepository lichLamViecRepository;

    @Override
    @Transactional
    public ReviewResponseDto createReview(CreateReviewRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        Integer maNguoiDung = normalizePositiveId(request.maNguoiDung(), "maNguoiDung");
        Integer maBacSi = normalizePositiveId(request.maBacSi(), "maBacSi");
        Integer soSao = normalizeSoSao(request.soSao());
        String noiDung = normalizeOptional(request.noiDung());

        NguoiDung nguoiDung = requireNguoiDung(maNguoiDung);
        requireBacSi(maBacSi);

        if (danhGiaBacSiRepository.findByMaNguoiDungAndMaBacSi(maNguoiDung, maBacSi).isPresent()) {
            throw new IllegalArgumentException("Ban da danh gia bac si nay");
        }

        if (!hasCompletedAppointment(maNguoiDung, maBacSi)) {
            throw new IllegalArgumentException("Chua co lich kham hoan thanh de danh gia bac si nay");
        }

        DanhGiaBacSi danhGia = new DanhGiaBacSi();
        danhGia.setMaNguoiDung(maNguoiDung);
        danhGia.setMaBacSi(maBacSi);
        danhGia.setSoSao(soSao);
        danhGia.setNoiDung(noiDung);
        danhGia.setThoiGian(LocalDateTime.now());
        DanhGiaBacSi created = danhGiaBacSiRepository.insert(danhGia);

        return mapToReviewResponse(created, nguoiDung);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByDoctor(Integer maBacSi) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        requireBacSi(normalizedMaBacSi);

        return danhGiaBacSiRepository.findByMaBacSi(normalizedMaBacSi).stream()
                .map(this::mapToReviewResponseWithUserLookup)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorRatingSummaryResponseDto getRatingSummary(Integer maBacSi) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        requireBacSi(normalizedMaBacSi);

        long tongDanhGia = danhGiaBacSiRepository.countByMaBacSi(normalizedMaBacSi);
        Double avg = danhGiaBacSiRepository.getAvgSoSaoByMaBacSi(normalizedMaBacSi);
        Double soSaoTrungBinh = tongDanhGia == 0 ? null : avg;
        return new DoctorRatingSummaryResponseDto(normalizedMaBacSi, tongDanhGia, soSaoTrungBinh);
    }

    private boolean hasCompletedAppointment(Integer maNguoiDung, Integer maBacSi) {
        return phieuDatLichRepository
                .findByMaBacSiAndTrangThaiPhieu(maBacSi, TrangThaiPhieuDatLich.DA_KHAM.name())
                .stream()
                .anyMatch(phieu -> phieu != null && maNguoiDung.equals(phieu.getMaNguoiDung()));
    }

    private ReviewResponseDto mapToReviewResponseWithUserLookup(DanhGiaBacSi danhGia) {
        if (danhGia == null) {
            throw new IllegalArgumentException("danhGia is required");
        }

        NguoiDung nguoiDung = nguoiDungRepository.selectById(danhGia.getMaNguoiDung()).orElse(null);
        return mapToReviewResponse(danhGia, nguoiDung);
    }

    private ReviewResponseDto mapToReviewResponse(DanhGiaBacSi danhGia, NguoiDung nguoiDung) {
        String hoTenNguoiDung =
                nguoiDung == null
                        ? null
                        : buildHoTenDayDu(nguoiDung.getHoLot(), nguoiDung.getTen());
        String anhDaiDienNguoiDung = nguoiDung == null ? null : nguoiDung.getAnhDaiDien();

        return new ReviewResponseDto(
                danhGia.getMaDanhGia(),
                danhGia.getMaNguoiDung(),
                hoTenNguoiDung,
                anhDaiDienNguoiDung,
                danhGia.getMaBacSi(),
                danhGia.getSoSao(),
                danhGia.getNoiDung(),
                danhGia.getThoiGian());
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

    private Integer normalizeSoSao(Integer soSao) {
        if (soSao == null) {
            throw new IllegalArgumentException("soSao is required");
        }
        if (soSao < SO_SAO_MIN || soSao > SO_SAO_MAX) {
            throw new IllegalArgumentException("soSao phai tu " + SO_SAO_MIN + " den " + SO_SAO_MAX);
        }
        return soSao;
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

