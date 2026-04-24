package doctor.Services.Business.Doctors;

import doctor.Models.DTOs.Doctors.Requests.UpdateDoctorProfileRequestDto;
import doctor.Models.DTOs.Doctors.Responses.DoctorProfileResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiKhoan;
import doctor.Models.Enums.TrangThaiHoSoBacSi;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import doctor.Services.Interfaces.Doctors.DoctorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final BacSiRepository bacSiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponseDto> getAllDoctors() {
        return bacSiRepository.selectAll().stream().map(this::mapToDoctorProfile).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileResponseDto getDoctorProfileById(Integer maBacSi) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        BacSi bacSi =
                bacSiRepository
                        .selectById(normalizedMaBacSi)
                        .orElseThrow(() -> new IllegalArgumentException("Bac si khong ton tai"));
        return mapToDoctorProfile(bacSi);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileResponseDto getDoctorProfileByTaiKhoanId(Integer maTaiKhoan) {
        Integer normalizedMaTaiKhoan = normalizePositiveId(maTaiKhoan, "maTaiKhoan");
        BacSi bacSi =
                bacSiRepository
                        .findByMaTaiKhoan(normalizedMaTaiKhoan)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Bac si khong ton tai theo maTaiKhoan"));
        return mapToDoctorProfile(bacSi);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorProfileResponseDto> searchDoctors(
            String keyword,
            String chuyenKhoa,
            String diaChiLamViec,
            String trangThaiHoSo,
            Integer limit,
            Integer offset) {
        String normalizedKeyword = normalizeOptional(keyword);
        String normalizedChuyenKhoa = normalizeOptional(chuyenKhoa);
        String normalizedDiaChiLamViec = normalizeOptional(diaChiLamViec);
        String normalizedTrangThaiHoSo = normalizeTrangThaiHoSoOptional(trangThaiHoSo);

        Integer normalizedLimit = normalizeLimit(limit);
        Integer normalizedOffset = normalizeOffset(offset);

        return bacSiRepository
                .search(
                        normalizedKeyword,
                        normalizedChuyenKhoa,
                        normalizedDiaChiLamViec,
                        normalizedTrangThaiHoSo,
                        normalizedLimit,
                        normalizedOffset)
                .stream()
                .map(this::mapToDoctorProfile)
                .toList();
    }

    @Override
    @Transactional
    public DoctorProfileResponseDto updateDoctorProfile(
            Integer maBacSi, UpdateDoctorProfileRequestDto request) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        BacSi bacSi =
                bacSiRepository
                        .selectById(normalizedMaBacSi)
                        .orElseThrow(() -> new IllegalArgumentException("Bac si khong ton tai"));

        String chuyenKhoa = requireNotBlank(request.chuyenKhoa(), "chuyenKhoa");
        String trinhDoChuyenMon = requireNotBlank(request.trinhDoChuyenMon(), "trinhDoChuyenMon");
        String loaiHinhBacSi = requireNotBlank(request.loaiHinhBacSi(), "loaiHinhBacSi");
        String tenCoSoYTe = requireNotBlank(request.tenCoSoYTe(), "tenCoSoYTe");
        String diaChiLamViec = normalizeOptional(request.diaChiLamViec());
        String maChungChiHanhNghe =
                requireNotBlank(request.maChungChiHanhNghe(), "maChungChiHanhNghe");
        String moTaBanThan = normalizeOptional(request.moTaBanThan());

        bacSiRepository
                .findByMaChungChiHanhNghe(maChungChiHanhNghe)
                .ifPresent(
                        existing -> {
                            if (!existing.getMaBacSi().equals(normalizedMaBacSi)) {
                                throw new IllegalArgumentException("maChungChiHanhNghe da ton tai");
                            }
                        });

        bacSi.setChuyenKhoa(chuyenKhoa);
        bacSi.setTrinhDoChuyenMon(trinhDoChuyenMon);
        bacSi.setLoaiHinhBacSi(loaiHinhBacSi);
        bacSi.setTenCoSoYTe(tenCoSoYTe);
        bacSi.setDiaChiLamViec(diaChiLamViec);
        bacSi.setMaChungChiHanhNghe(maChungChiHanhNghe);
        bacSi.setMoTaBanThan(moTaBanThan);

        BacSi updatedBacSi = bacSiRepository.update(bacSi);
        return mapToDoctorProfile(updatedBacSi);
    }

    private DoctorProfileResponseDto mapToDoctorProfile(BacSi bacSi) {
        TaiKhoan taiKhoan =
                taiKhoanRepository
                        .selectById(bacSi.getMaTaiKhoan())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay tai khoan cua bac si"));

        NguoiDung nguoiDung =
                nguoiDungRepository
                        .findByMaTaiKhoan(bacSi.getMaTaiKhoan())
                        .orElseThrow(
                                () -> new IllegalStateException("Khong tim thay thong tin nguoi dung cua bac si"));

        return new DoctorProfileResponseDto(
                bacSi.getMaBacSi(),
                bacSi.getMaTaiKhoan(),
                nguoiDung.getMaNguoiDung(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getVaiTro(),
                taiKhoan.getTrangThaiHoatDong(),
                nguoiDung.getHoLot(),
                nguoiDung.getTen(),
                buildHoTenDayDu(nguoiDung.getHoLot(), nguoiDung.getTen()),
                nguoiDung.getSoDienThoai(),
                nguoiDung.getEmail(),
                nguoiDung.getAnhDaiDien(),
                bacSi.getChuyenKhoa(),
                bacSi.getTrinhDoChuyenMon(),
                bacSi.getLoaiHinhBacSi(),
                bacSi.getTenCoSoYTe(),
                bacSi.getDiaChiLamViec(),
                bacSi.getMaChungChiHanhNghe(),
                bacSi.getMoTaBanThan(),
                bacSi.getTrangThaiHoSo());
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

    private Integer normalizePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return id;
    }

    private Integer normalizeLimit(Integer limit) {
        if (limit == null) {
            return null;
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        return limit;
    }

    private Integer normalizeOffset(Integer offset) {
        if (offset == null) {
            return null;
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }
        return offset;
    }

    private String normalizeTrangThaiHoSoOptional(String trangThaiHoSo) {
        String normalized = normalizeOptional(trangThaiHoSo);
        if (normalized == null) {
            return null;
        }
        try {
            return TrangThaiHoSoBacSi.valueOf(normalized.toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("trangThaiHoSo khong hop le");
        }
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
}
