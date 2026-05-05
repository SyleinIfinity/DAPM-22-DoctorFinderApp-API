package doctor.Services.Business.Admin;

import doctor.Models.DTOs.Admin.Requests.RejectDoctorProfileRequestDto;
import doctor.Models.DTOs.Admin.Responses.AdminDoctorProfileActionResponseDto;
import doctor.Models.DTOs.Admin.Responses.AdminDoctorProfileDetailResponseDto;
import doctor.Models.DTOs.Admin.Responses.PendingDoctorProfileResponseDto;
import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiLieuBacSi;
import doctor.Models.Enums.TrangThaiHoSoBacSi;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiLieuBacSiRepository;
import doctor.Services.Interfaces.Admin.AdminDoctorService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDoctorServiceImpl implements AdminDoctorService {
    private final BacSiRepository bacSiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final TaiLieuBacSiRepository taiLieuBacSiRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PendingDoctorProfileResponseDto> getPendingDoctors() {
        List<BacSi> pending =
                bacSiRepository.findByTrangThaiHoSo(TrangThaiHoSoBacSi.CHO_DUYET.name()).stream()
                        .sorted(Comparator.comparing(BacSi::getMaBacSi, Comparator.nullsLast(Comparator.naturalOrder()))
                                .reversed())
                        .toList();

        return pending.stream().map(this::mapToPendingDoctorProfile).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDoctorProfileDetailResponseDto getDoctorDetail(Integer maBacSi) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        BacSi bacSi =
                bacSiRepository
                        .selectById(normalizedMaBacSi)
                        .orElseThrow(() -> new IllegalArgumentException("Bac si khong ton tai"));

        NguoiDung nguoiDung =
                nguoiDungRepository
                        .findByMaTaiKhoan(bacSi.getMaTaiKhoan())
                        .orElse(null);

        List<DoctorDocumentResponseDto> documents =
                taiLieuBacSiRepository.findByMaBacSi(normalizedMaBacSi).stream()
                        .map(this::mapToDoctorDocument)
                        .toList();

        String hoTenDayDu =
                nguoiDung == null
                        ? null
                        : buildHoTenDayDu(nguoiDung.getHoLot(), nguoiDung.getTen());

        return new AdminDoctorProfileDetailResponseDto(
                bacSi.getMaBacSi(),
                bacSi.getMaTaiKhoan(),
                nguoiDung == null ? null : nguoiDung.getMaNguoiDung(),
                nguoiDung == null ? null : nguoiDung.getHoLot(),
                nguoiDung == null ? null : nguoiDung.getTen(),
                hoTenDayDu,
                nguoiDung == null ? null : nguoiDung.getSoDienThoai(),
                nguoiDung == null ? null : nguoiDung.getEmail(),
                nguoiDung == null ? null : nguoiDung.getCccd(),
                nguoiDung == null ? null : nguoiDung.getAnhDaiDien(),
                bacSi.getChuyenKhoa(),
                bacSi.getTrinhDoChuyenMon(),
                bacSi.getLoaiHinhBacSi(),
                bacSi.getTenCoSoYTe(),
                bacSi.getDiaChiLamViec(),
                bacSi.getMaChungChiHanhNghe(),
                bacSi.getMoTaBanThan(),
                bacSi.getTrangThaiHoSo(),
                documents);
    }

    @Override
    @Transactional
    public AdminDoctorProfileActionResponseDto approveDoctorProfile(Integer maBacSi) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        BacSi bacSi =
                bacSiRepository
                        .selectById(normalizedMaBacSi)
                        .orElseThrow(() -> new IllegalArgumentException("Bac si khong ton tai"));

        String current = normalizeOptional(bacSi.getTrangThaiHoSo());
        if (TrangThaiHoSoBacSi.KHOA.name().equals(current)) {
            throw new IllegalArgumentException("Ho so bac si dang bi khoa");
        }
        if (TrangThaiHoSoBacSi.DA_DUYET.name().equals(current)) {
            return new AdminDoctorProfileActionResponseDto(
                    true, "Ho so da duoc duyet truoc do", normalizedMaBacSi, current, null);
        }

        bacSi.setTrangThaiHoSo(TrangThaiHoSoBacSi.DA_DUYET.name());
        bacSi.setNgayDuyetHoSo(LocalDateTime.now());
        BacSi updated = bacSiRepository.update(bacSi);
        return new AdminDoctorProfileActionResponseDto(
                true, "Phe duyet ho so bac si thanh cong", normalizedMaBacSi, updated.getTrangThaiHoSo(), null);
    }

    @Override
    @Transactional
    public AdminDoctorProfileActionResponseDto rejectDoctorProfile(
            Integer maBacSi, RejectDoctorProfileRequestDto request) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        String lyDoTuChoi = requireNotBlank(request.lyDoTuChoi(), "lyDoTuChoi");

        BacSi bacSi =
                bacSiRepository
                        .selectById(normalizedMaBacSi)
                        .orElseThrow(() -> new IllegalArgumentException("Bac si khong ton tai"));

        bacSi.setTrangThaiHoSo(TrangThaiHoSoBacSi.TU_CHOI.name());
        BacSi updated = bacSiRepository.update(bacSi);
        return new AdminDoctorProfileActionResponseDto(
                true,
                "Tu choi ho so bac si thanh cong",
                normalizedMaBacSi,
                updated.getTrangThaiHoSo(),
                lyDoTuChoi);
    }

    private PendingDoctorProfileResponseDto mapToPendingDoctorProfile(BacSi bacSi) {
        if (bacSi == null) {
            throw new IllegalArgumentException("bacSi is required");
        }

        NguoiDung nguoiDung =
                nguoiDungRepository.findByMaTaiKhoan(bacSi.getMaTaiKhoan()).orElse(null);

        String hoTenDayDu =
                nguoiDung == null
                        ? null
                        : buildHoTenDayDu(nguoiDung.getHoLot(), nguoiDung.getTen());

        int soLuongTaiLieu = taiLieuBacSiRepository.findByMaBacSi(bacSi.getMaBacSi()).size();

        return new PendingDoctorProfileResponseDto(
                bacSi.getMaBacSi(),
                bacSi.getMaTaiKhoan(),
                nguoiDung == null ? null : nguoiDung.getMaNguoiDung(),
                hoTenDayDu,
                nguoiDung == null ? null : nguoiDung.getSoDienThoai(),
                nguoiDung == null ? null : nguoiDung.getEmail(),
                bacSi.getChuyenKhoa(),
                bacSi.getMaChungChiHanhNghe(),
                bacSi.getTrangThaiHoSo(),
                soLuongTaiLieu);
    }

    private DoctorDocumentResponseDto mapToDoctorDocument(TaiLieuBacSi entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is required");
        }
        return new DoctorDocumentResponseDto(
                entity.getMaTaiLieu(),
                entity.getMaBacSi(),
                entity.getTieuDeTaiLieu(),
                entity.getDuongDanFileUrl());
    }

    private Integer normalizePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return id;
    }

    private String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
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

