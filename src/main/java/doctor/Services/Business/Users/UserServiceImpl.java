package doctor.Services.Business.Users;

import doctor.Models.DTOs.Users.Requests.UpdateUserProfileRequestDto;
import doctor.Models.DTOs.Users.Responses.UserProfileResponseDto;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiKhoan;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import doctor.Services.Interfaces.Users.UserService;
import doctor.Utils.CloudinaryFileUploadHelper;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");

    private final NguoiDungRepository nguoiDungRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final CloudinaryFileUploadHelper cloudinaryFileUploadHelper;

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> getAllUsers() {
        return nguoiDungRepository.selectAll().stream().map(this::mapToUserProfile).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfileById(Integer maNguoiDung) {
        Integer normalizedMaNguoiDung = normalizePositiveId(maNguoiDung, "maNguoiDung");
        NguoiDung nguoiDung =
                nguoiDungRepository
                        .selectById(normalizedMaNguoiDung)
                        .orElseThrow(() -> new IllegalArgumentException("Nguoi dung khong ton tai"));
        return mapToUserProfile(nguoiDung);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfileByTaiKhoanId(Integer maTaiKhoan) {
        Integer normalizedMaTaiKhoan = normalizePositiveId(maTaiKhoan, "maTaiKhoan");
        NguoiDung nguoiDung =
                nguoiDungRepository
                        .findByMaTaiKhoan(normalizedMaTaiKhoan)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Nguoi dung khong ton tai theo maTaiKhoan"));
        return mapToUserProfile(nguoiDung);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateUserProfile(
            Integer maNguoiDung, UpdateUserProfileRequestDto request) {
        Integer normalizedMaNguoiDung = normalizePositiveId(maNguoiDung, "maNguoiDung");
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        NguoiDung nguoiDung =
                nguoiDungRepository
                        .selectById(normalizedMaNguoiDung)
                        .orElseThrow(() -> new IllegalArgumentException("Nguoi dung khong ton tai"));

        String hoLot = requireNotBlank(request.hoLot(), "hoLot");
        String ten = requireNotBlank(request.ten(), "ten");
        String soDienThoai = normalizeSoDienThoai(request.soDienThoai());
        String email = normalizeEmail(request.email());
        String cccd = normalizeCccd(request.cccd());
        String anhDaiDien = normalizeOptional(request.anhDaiDien());

        validateUniqueFields(normalizedMaNguoiDung, soDienThoai, email, cccd);

        nguoiDung.setHoLot(hoLot);
        nguoiDung.setTen(ten);
        nguoiDung.setSoDienThoai(soDienThoai);
        nguoiDung.setEmail(email);
        nguoiDung.setCccd(cccd);
        nguoiDung.setAnhDaiDien(anhDaiDien);

        NguoiDung updatedNguoiDung = nguoiDungRepository.update(nguoiDung);
        return mapToUserProfile(updatedNguoiDung);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateUserAvatar(Integer maNguoiDung, MultipartFile avatar)
            throws IOException {
        Integer normalizedMaNguoiDung = normalizePositiveId(maNguoiDung, "maNguoiDung");
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("avatar is required");
        }

        NguoiDung nguoiDung =
                nguoiDungRepository
                        .selectById(normalizedMaNguoiDung)
                        .orElseThrow(() -> new IllegalArgumentException("Nguoi dung khong ton tai"));

        String oldPublicId = normalizeOptional(nguoiDung.getAnhDaiDienPublicId());
        var uploaded = cloudinaryFileUploadHelper.uploadUserAvatar(avatar);
        if (uploaded == null || uploaded.getUrl() == null || uploaded.getUrl().isBlank()) {
            throw new IllegalStateException("Khong the upload avatar");
        }

        nguoiDung.setAnhDaiDien(uploaded.getUrl());
        nguoiDung.setAnhDaiDienPublicId(uploaded.getPublicId());
        nguoiDungRepository.update(nguoiDung);

        if (oldPublicId != null && !oldPublicId.equals(uploaded.getPublicId())) {
            cloudinaryFileUploadHelper.deleteImage(oldPublicId);
        }

        return mapToUserProfile(nguoiDung);
    }

    private void validateUniqueFields(
            Integer maNguoiDung, String soDienThoai, String email, String cccd) {
        nguoiDungRepository
                .findBySoDienThoai(soDienThoai)
                .ifPresent(
                        existing -> {
                            if (!existing.getMaNguoiDung().equals(maNguoiDung)) {
                                throw new IllegalArgumentException("soDienThoai da ton tai");
                            }
                        });

        nguoiDungRepository
                .findByEmail(email)
                .ifPresent(
                        existing -> {
                            if (!existing.getMaNguoiDung().equals(maNguoiDung)) {
                                throw new IllegalArgumentException("email da ton tai");
                            }
                        });

        nguoiDungRepository
                .findByCccd(cccd)
                .ifPresent(
                        existing -> {
                            if (!existing.getMaNguoiDung().equals(maNguoiDung)) {
                                throw new IllegalArgumentException("cccd da ton tai");
                            }
                        });
    }

    private UserProfileResponseDto mapToUserProfile(NguoiDung nguoiDung) {
        TaiKhoan taiKhoan =
                taiKhoanRepository
                        .selectById(nguoiDung.getMaTaiKhoan())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Khong tim thay tai khoan cua nguoi dung"));

        String hoTenDayDu = buildHoTenDayDu(nguoiDung.getHoLot(), nguoiDung.getTen());
        return new UserProfileResponseDto(
                nguoiDung.getMaNguoiDung(),
                nguoiDung.getMaTaiKhoan(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getVaiTro(),
                taiKhoan.getTrangThaiHoatDong(),
                nguoiDung.getHoLot(),
                nguoiDung.getTen(),
                hoTenDayDu,
                nguoiDung.getSoDienThoai(),
                nguoiDung.getEmail(),
                nguoiDung.getCccd(),
                nguoiDung.getAnhDaiDien(),
                nguoiDung.getAnhDaiDienPublicId());
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

    private String normalizeSoDienThoai(String soDienThoai) {
        String normalized = requireNotBlank(soDienThoai, "soDienThoai");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("soDienThoai phai la 10 chu so");
        }
        return normalized;
    }

    private String normalizeEmail(String email) {
        String normalized = requireNotBlank(email, "email").toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("email khong hop le");
        }
        return normalized;
    }

    private String normalizeCccd(String cccd) {
        String normalized = requireNotBlank(cccd, "cccd");
        if (!CCCD_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("cccd phai la 12 chu so");
        }
        return normalized;
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
