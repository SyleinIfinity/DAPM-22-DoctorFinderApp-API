package doctor.Services.Business.Auth;

import doctor.Models.DTOs.Auth.Requests.LoginRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterRequestDto;
import doctor.Models.DTOs.Auth.Responses.LoginResponseDto;
import doctor.Models.DTOs.Auth.Responses.RegisterResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiKhoan;
import doctor.Models.Enums.TrangThaiHoSoBacSi;
import doctor.Models.Enums.TrangThaiHoatDongTaiKhoan;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import doctor.Services.Interfaces.Auth.AuthService;
import doctor.Utils.PasswordHashHelper;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String ROLE_NGUOI_DUNG = "NGUOI_DUNG";
    private static final String ROLE_BAC_SI = "BAC_SI";
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");

    private final TaiKhoanRepository taiKhoanRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BacSiRepository bacSiRepository;
    private final PasswordHashHelper passwordHashHelper;

    @Override
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        String tenDangNhap = normalizeTenDangNhap(request.tenDangNhap());
        String matKhau = requireNotBlank(request.matKhau(), "matKhau");
        String xacNhanMatKhau = requireNotBlank(request.xacNhanMatKhau(), "xacNhanMatKhau");
        String vaiTro = normalizeVaiTro(request.vaiTro());
        String hoLot = requireNotBlank(request.hoLot(), "hoLot");
        String ten = requireNotBlank(request.ten(), "ten");
        String soDienThoai = normalizeSoDienThoai(request.soDienThoai());
        String email = normalizeEmail(request.email());
        String cccd = normalizeCccd(request.cccd());
        String anhDaiDien = normalizeOptional(request.anhDaiDien());

        validatePassword(matKhau, xacNhanMatKhau);
        validateUniqueInfo(tenDangNhap, soDienThoai, email, cccd);

        DoctorRegisterData doctorData = validateDoctorDataIfNeeded(vaiTro, request);

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(tenDangNhap);
        taiKhoan.setMatKhauHash(passwordHashHelper.hashPassword(matKhau));
        taiKhoan.setVaiTro(vaiTro);
        taiKhoan.setTrangThaiHoatDong(TrangThaiHoatDongTaiKhoan.HOAT_DONG.name());
        taiKhoan.setNgayTao(LocalDateTime.now());
        TaiKhoan createdTaiKhoan = taiKhoanRepository.insert(taiKhoan);

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setMaTaiKhoan(createdTaiKhoan.getMaTaiKhoan());
        nguoiDung.setHoLot(hoLot);
        nguoiDung.setTen(ten);
        nguoiDung.setSoDienThoai(soDienThoai);
        nguoiDung.setEmail(email);
        nguoiDung.setCccd(cccd);
        nguoiDung.setAnhDaiDien(anhDaiDien);
        NguoiDung createdNguoiDung = nguoiDungRepository.insert(nguoiDung);

        Integer maBacSi = null;
        if (doctorData != null) {
            BacSi bacSi = new BacSi();
            bacSi.setMaTaiKhoan(createdTaiKhoan.getMaTaiKhoan());
            bacSi.setChuyenKhoa(doctorData.chuyenKhoa());
            bacSi.setTrinhDoChuyenMon(doctorData.trinhDoChuyenMon());
            bacSi.setLoaiHinhBacSi(doctorData.loaiHinhBacSi());
            bacSi.setTenCoSoYTe(doctorData.tenCoSoYTe());
            bacSi.setDiaChiLamViec(doctorData.diaChiLamViec());
            bacSi.setMaChungChiHanhNghe(doctorData.maChungChiHanhNghe());
            bacSi.setMoTaBanThan(doctorData.moTaBanThan());
            bacSi.setTrangThaiHoSo(TrangThaiHoSoBacSi.CHO_DUYET.name());
            BacSi createdBacSi = bacSiRepository.insert(bacSi);
            maBacSi = createdBacSi.getMaBacSi();
        }

        return new RegisterResponseDto(
                true,
                "Dang ky thanh cong",
                createdTaiKhoan.getMaTaiKhoan(),
                createdTaiKhoan.getTenDangNhap(),
                createdTaiKhoan.getVaiTro(),
                createdTaiKhoan.getTrangThaiHoatDong(),
                createdNguoiDung.getMaNguoiDung(),
                maBacSi);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        String tenDangNhap = normalizeTenDangNhap(request.tenDangNhap());
        String matKhau = requireNotBlank(request.matKhau(), "matKhau");

        TaiKhoan taiKhoan =
                taiKhoanRepository
                        .findByTenDangNhap(tenDangNhap)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Sai ten dang nhap hoac mat khau"));

        if (!TrangThaiHoatDongTaiKhoan.HOAT_DONG.name().equals(taiKhoan.getTrangThaiHoatDong())) {
            throw new IllegalArgumentException("Tai khoan khong o trang thai hoat dong");
        }

        if (!passwordHashHelper.matches(matKhau, taiKhoan.getMatKhauHash())) {
            throw new IllegalArgumentException("Sai ten dang nhap hoac mat khau");
        }

        Integer maNguoiDung =
                nguoiDungRepository
                        .findByMaTaiKhoan(taiKhoan.getMaTaiKhoan())
                        .map(NguoiDung::getMaNguoiDung)
                        .orElse(null);

        Integer maBacSi =
                bacSiRepository
                        .findByMaTaiKhoan(taiKhoan.getMaTaiKhoan())
                        .map(BacSi::getMaBacSi)
                        .orElse(null);

        return new LoginResponseDto(
                true,
                "Dang nhap thanh cong",
                taiKhoan.getMaTaiKhoan(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getVaiTro(),
                taiKhoan.getTrangThaiHoatDong(),
                maNguoiDung,
                maBacSi);
    }

    private void validatePassword(String matKhau, String xacNhanMatKhau) {
        if (matKhau.length() < 8) {
            throw new IllegalArgumentException("matKhau phai co it nhat 8 ky tu");
        }
        if (!matKhau.equals(xacNhanMatKhau)) {
            throw new IllegalArgumentException("matKhau va xacNhanMatKhau khong khop");
        }
    }

    private void validateUniqueInfo(
            String tenDangNhap, String soDienThoai, String email, String cccd) {
        if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
            throw new IllegalArgumentException("tenDangNhap da ton tai");
        }
        if (nguoiDungRepository.existsBySoDienThoai(soDienThoai)) {
            throw new IllegalArgumentException("soDienThoai da ton tai");
        }
        if (nguoiDungRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("email da ton tai");
        }
        if (nguoiDungRepository.existsByCccd(cccd)) {
            throw new IllegalArgumentException("cccd da ton tai");
        }
    }

    private DoctorRegisterData validateDoctorDataIfNeeded(
            String vaiTro, RegisterRequestDto request) {
        if (!ROLE_BAC_SI.equals(vaiTro)) {
            return null;
        }

        String chuyenKhoa = requireNotBlank(request.chuyenKhoa(), "chuyenKhoa");
        String trinhDoChuyenMon = requireNotBlank(request.trinhDoChuyenMon(), "trinhDoChuyenMon");
        String loaiHinhBacSi = requireNotBlank(request.loaiHinhBacSi(), "loaiHinhBacSi");
        String tenCoSoYTe = requireNotBlank(request.tenCoSoYTe(), "tenCoSoYTe");
        String diaChiLamViec = normalizeOptional(request.diaChiLamViec());
        String maChungChiHanhNghe =
                requireNotBlank(request.maChungChiHanhNghe(), "maChungChiHanhNghe");
        String moTaBanThan = normalizeOptional(request.moTaBanThan());

        if (bacSiRepository.findByMaChungChiHanhNghe(maChungChiHanhNghe).isPresent()) {
            throw new IllegalArgumentException("maChungChiHanhNghe da ton tai");
        }

        return new DoctorRegisterData(
                chuyenKhoa,
                trinhDoChuyenMon,
                loaiHinhBacSi,
                tenCoSoYTe,
                diaChiLamViec,
                maChungChiHanhNghe,
                moTaBanThan);
    }

    private String normalizeTenDangNhap(String tenDangNhap) {
        String normalized = requireNotBlank(tenDangNhap, "tenDangNhap").toLowerCase();
        if (normalized.length() < 4 || normalized.length() > 50) {
            throw new IllegalArgumentException("tenDangNhap phai trong khoang 4-50 ky tu");
        }
        return normalized;
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

    private String normalizeVaiTro(String vaiTro) {
        String normalized = requireNotBlank(vaiTro, "vaiTro").toUpperCase();
        return switch (normalized) {
            case "NGUOI_DUNG", "NGUOIDUNG", "USER" -> ROLE_NGUOI_DUNG;
            case "BAC_SI", "BACSI", "DOCTOR" -> ROLE_BAC_SI;
            default -> throw new IllegalArgumentException("vaiTro khong hop le");
        };
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private record DoctorRegisterData(
            String chuyenKhoa,
            String trinhDoChuyenMon,
            String loaiHinhBacSi,
            String tenCoSoYTe,
            String diaChiLamViec,
            String maChungChiHanhNghe,
            String moTaBanThan) {}
}
