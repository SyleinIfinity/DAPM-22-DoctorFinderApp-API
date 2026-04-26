package doctor.Services.Business.Admin;

import doctor.Models.DTOs.Admin.Requests.UpdateAccountRoleRequestDto;
import doctor.Models.DTOs.Admin.Responses.AdminAccountActionResponseDto;
import doctor.Models.DTOs.Admin.Responses.AdminAccountResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiKhoan;
import doctor.Models.Enums.TrangThaiHoatDongTaiKhoan;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import doctor.Services.Interfaces.Admin.AdminAccountService;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAccountServiceImpl implements AdminAccountService {
    private final TaiKhoanRepository taiKhoanRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BacSiRepository bacSiRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminAccountResponseDto> getAccounts() {
        return taiKhoanRepository.selectAll().stream()
                .sorted(
                        Comparator.comparing(
                                        TaiKhoan::getMaTaiKhoan,
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                                .reversed())
                .map(this::mapToAdminAccountResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdminAccountActionResponseDto lockAccount(Integer maTaiKhoan) {
        TaiKhoan taiKhoan = requireTaiKhoan(maTaiKhoan);
        taiKhoan.setTrangThaiHoatDong(TrangThaiHoatDongTaiKhoan.KHOA.name());
        TaiKhoan updated = taiKhoanRepository.update(taiKhoan);
        return mapToActionResponse(updated, "Khoa tai khoan thanh cong");
    }

    @Override
    @Transactional
    public AdminAccountActionResponseDto unlockAccount(Integer maTaiKhoan) {
        TaiKhoan taiKhoan = requireTaiKhoan(maTaiKhoan);
        taiKhoan.setTrangThaiHoatDong(TrangThaiHoatDongTaiKhoan.HOAT_DONG.name());
        TaiKhoan updated = taiKhoanRepository.update(taiKhoan);
        return mapToActionResponse(updated, "Mo khoa tai khoan thanh cong");
    }

    @Override
    @Transactional
    public AdminAccountActionResponseDto updateRole(Integer maTaiKhoan, UpdateAccountRoleRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        String vaiTro = requireNotBlank(request.vaiTro(), "vaiTro");
        if (vaiTro.length() > 50) {
            throw new IllegalArgumentException("vaiTro toi da 50 ky tu");
        }

        TaiKhoan taiKhoan = requireTaiKhoan(maTaiKhoan);
        taiKhoan.setVaiTro(vaiTro);
        TaiKhoan updated = taiKhoanRepository.update(taiKhoan);
        return mapToActionResponse(updated, "Cap nhat vai tro thanh cong");
    }

    private AdminAccountResponseDto mapToAdminAccountResponse(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new IllegalArgumentException("taiKhoan is required");
        }

        NguoiDung nguoiDung = nguoiDungRepository.findByMaTaiKhoan(taiKhoan.getMaTaiKhoan()).orElse(null);
        BacSi bacSi = bacSiRepository.findByMaTaiKhoan(taiKhoan.getMaTaiKhoan()).orElse(null);

        String hoTenNguoiDung =
                nguoiDung == null
                        ? null
                        : buildHoTenDayDu(nguoiDung.getHoLot(), nguoiDung.getTen());

        return new AdminAccountResponseDto(
                taiKhoan.getMaTaiKhoan(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getVaiTro(),
                taiKhoan.getTrangThaiHoatDong(),
                taiKhoan.getNgayTao(),
                nguoiDung == null ? null : nguoiDung.getMaNguoiDung(),
                hoTenNguoiDung,
                nguoiDung == null ? null : nguoiDung.getSoDienThoai(),
                nguoiDung == null ? null : nguoiDung.getEmail(),
                bacSi == null ? null : bacSi.getMaBacSi(),
                bacSi == null ? null : bacSi.getTrangThaiHoSo());
    }

    private AdminAccountActionResponseDto mapToActionResponse(TaiKhoan taiKhoan, String message) {
        return new AdminAccountActionResponseDto(
                true,
                message,
                taiKhoan.getMaTaiKhoan(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getVaiTro(),
                taiKhoan.getTrangThaiHoatDong(),
                taiKhoan.getNgayTao());
    }

    private TaiKhoan requireTaiKhoan(Integer maTaiKhoan) {
        Integer normalized = normalizePositiveId(maTaiKhoan, "maTaiKhoan");
        return taiKhoanRepository
                .selectById(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan khong ton tai"));
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

