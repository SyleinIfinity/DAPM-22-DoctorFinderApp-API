package doctor.Services.Business.Auth;

import doctor.Models.DTOs.Auth.Requests.LoginRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterDoctorAccountRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterDoctorProfileRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterUserAccountRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterUserInfoRequestDto;
import doctor.Models.DTOs.Auth.Responses.AccountDoctorInfoResponseDto;
import doctor.Models.DTOs.Auth.Responses.AccountInfoResponseDto;
import doctor.Models.DTOs.Auth.Responses.LoginResponseDto;
import doctor.Models.DTOs.Auth.Responses.RegisterResponseDto;
import doctor.Models.DTOs.Auth.Responses.UpgradeToDoctorResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiKhoan;
import doctor.Models.Enums.TrangThaiHoSoBacSi;
import doctor.Models.Enums.TrangThaiHoatDongTaiKhoan;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import doctor.Security.LoginAttemptService;
import doctor.Security.PasswordHashHelper;
import doctor.Services.Interfaces.Auth.AuthService;
import doctor.Services.Interfaces.Auth.OtpService;
import doctor.Services.Interfaces.DoctorDocuments.DoctorDocumentService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final OtpService otpService;
    private final LoginAttemptService loginAttemptService;
    private final DoctorDocumentService doctorDocumentService;

    @Value("${app.otp.require-proof-token:true}")
    private boolean requireOtpProofToken = true;

    @Override
    @Transactional
    public RegisterResponseDto registerUser(RegisterUserAccountRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        return registerInternal(
                request.thongTinNguoiDung(), null, request.otpProofToken(), ROLE_NGUOI_DUNG);
    }

    @Override
    @Transactional
    public RegisterResponseDto registerDoctor(RegisterDoctorAccountRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        return registerInternal(
                request.thongTinNguoiDung(),
                request.thongTinBacSi(),
                request.otpProofToken(),
                ROLE_BAC_SI);
    }

    private RegisterResponseDto registerInternal(
            RegisterUserInfoRequestDto userInfoRequest,
            RegisterDoctorProfileRequestDto doctorProfileRequest,
            String otpProofToken,
            String vaiTro) {
        RegisterUserInfo userInfo = validateAndNormalizeUserInfo(userInfoRequest);
        validateUniqueInfo(
                userInfo.tenDangNhap(),
                userInfo.soDienThoai(),
                userInfo.email(),
                userInfo.cccd());

        DoctorRegisterData doctorData = validateDoctorDataIfNeeded(vaiTro, doctorProfileRequest);
        if (requireOtpProofToken) {
            otpService.consumeOtpProofToken(
                    requireNotBlank(otpProofToken, "otpProofToken"), userInfo.email(), "REGISTER");
        }

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(userInfo.tenDangNhap());
        taiKhoan.setMatKhauHash(passwordHashHelper.hashPassword(userInfo.matKhau()));
        taiKhoan.setVaiTro(vaiTro);
        taiKhoan.setTrangThaiHoatDong(TrangThaiHoatDongTaiKhoan.HOAT_DONG.name());
        taiKhoan.setNgayTao(LocalDateTime.now());
        TaiKhoan createdTaiKhoan = taiKhoanRepository.insert(taiKhoan);

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setMaTaiKhoan(createdTaiKhoan.getMaTaiKhoan());
        nguoiDung.setHoLot(userInfo.hoLot());
        nguoiDung.setTen(userInfo.ten());
        nguoiDung.setSoDienThoai(userInfo.soDienThoai());
        nguoiDung.setEmail(userInfo.email());
        nguoiDung.setCccd(userInfo.cccd());
        nguoiDung.setAnhDaiDien(userInfo.anhDaiDien());
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
                buildRegisterSuccessMessage(vaiTro),
                createdTaiKhoan.getMaTaiKhoan(),
                createdTaiKhoan.getTenDangNhap(),
                createdTaiKhoan.getVaiTro(),
                createdTaiKhoan.getTrangThaiHoatDong(),
                createdNguoiDung.getMaNguoiDung(),
                maBacSi);
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto request, String clientIp) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        String tenDangNhap = normalizeTenDangNhap(request.tenDangNhap());
        String matKhau = requireNotBlank(request.matKhau(), "matKhau");
        String normalizedClientIp = normalizeClientIp(clientIp);

        loginAttemptService.assertAllowed(tenDangNhap, normalizedClientIp);

        TaiKhoan taiKhoan =
                taiKhoanRepository
                        .findByTenDangNhap(tenDangNhap)
                        .orElse(null);

        if (taiKhoan == null) {
            loginAttemptService.recordFailure(tenDangNhap, normalizedClientIp);
            throw new IllegalArgumentException("Sai ten dang nhap hoac mat khau");
        }

        if (!TrangThaiHoatDongTaiKhoan.HOAT_DONG.name().equals(taiKhoan.getTrangThaiHoatDong())) {
            loginAttemptService.recordFailure(tenDangNhap, normalizedClientIp);
            throw new IllegalArgumentException("Tai khoan khong o trang thai hoat dong");
        }

        if (!isPasswordMatchedWithMigration(taiKhoan, matKhau)) {
            loginAttemptService.recordFailure(tenDangNhap, normalizedClientIp);
            throw new IllegalArgumentException("Sai ten dang nhap hoac mat khau");
        }

        loginAttemptService.reset(tenDangNhap, normalizedClientIp);

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

    @Override
    @Transactional(readOnly = true)
    public AccountInfoResponseDto getAccountInfo(Integer maTaiKhoan) {
        Integer normalizedMaTaiKhoan = normalizePositiveId(maTaiKhoan, "maTaiKhoan");
        AccountProfileContext context = resolveAccountProfileContext(normalizedMaTaiKhoan);
        TaiKhoan taiKhoan = context.taiKhoan();
        NguoiDung nguoiDung = context.nguoiDung();

        return new AccountInfoResponseDto(
                taiKhoan.getMaTaiKhoan(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getVaiTro(),
                taiKhoan.getTrangThaiHoatDong(),
                taiKhoan.getNgayTao(),
                nguoiDung.getMaNguoiDung(),
                nguoiDung.getHoLot(),
                nguoiDung.getTen(),
                nguoiDung.getSoDienThoai(),
                nguoiDung.getEmail(),
                nguoiDung.getCccd(),
                nguoiDung.getAnhDaiDien());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDoctorInfoResponseDto getAccountInfoWithDoctor(Integer maTaiKhoan) {
        Integer normalizedMaTaiKhoan = normalizePositiveId(maTaiKhoan, "maTaiKhoan");
        AccountProfileContext context = resolveAccountProfileContext(normalizedMaTaiKhoan);
        TaiKhoan taiKhoan = context.taiKhoan();
        NguoiDung nguoiDung = context.nguoiDung();
        BacSi bacSi = bacSiRepository.findByMaTaiKhoan(normalizedMaTaiKhoan).orElse(null);

        return new AccountDoctorInfoResponseDto(
                taiKhoan.getMaTaiKhoan(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getVaiTro(),
                taiKhoan.getTrangThaiHoatDong(),
                taiKhoan.getNgayTao(),
                nguoiDung.getMaNguoiDung(),
                nguoiDung.getHoLot(),
                nguoiDung.getTen(),
                nguoiDung.getSoDienThoai(),
                nguoiDung.getEmail(),
                nguoiDung.getCccd(),
                nguoiDung.getAnhDaiDien(),
                bacSi != null,
                bacSi == null ? null : bacSi.getMaBacSi(),
                bacSi == null ? null : bacSi.getChuyenKhoa(),
                bacSi == null ? null : bacSi.getTrinhDoChuyenMon(),
                bacSi == null ? null : bacSi.getLoaiHinhBacSi(),
                bacSi == null ? null : bacSi.getTenCoSoYTe(),
                bacSi == null ? null : bacSi.getDiaChiLamViec(),
                bacSi == null ? null : bacSi.getMaChungChiHanhNghe(),
                bacSi == null ? null : bacSi.getMoTaBanThan(),
                bacSi == null ? null : bacSi.getTrangThaiHoSo());
    }

    @Override
    @Transactional
    public UpgradeToDoctorResponseDto upgradeToDoctor(
            Integer maTaiKhoan,
            RegisterDoctorProfileRequestDto thongTinBacSi,
            List<String> tieuDeTaiLieu,
            List<MultipartFile> files) {
        Integer normalizedMaTaiKhoan = normalizePositiveId(maTaiKhoan, "maTaiKhoan");
        if (thongTinBacSi == null) {
            throw new IllegalArgumentException("thongTinBacSi is required");
        }

        TaiKhoan taiKhoan =
                taiKhoanRepository
                        .selectById(normalizedMaTaiKhoan)
                        .orElseThrow(() -> new IllegalArgumentException("Tai khoan khong ton tai"));

        if (!TrangThaiHoatDongTaiKhoan.HOAT_DONG.name().equals(normalizeOptional(taiKhoan.getTrangThaiHoatDong()))) {
            throw new IllegalArgumentException("Tai khoan khong o trang thai HOAT_DONG");
        }

        nguoiDungRepository
                .findByMaTaiKhoan(normalizedMaTaiKhoan)
                .orElseThrow(() -> new IllegalArgumentException("Thong tin nguoi dung khong ton tai"));

        BacSi existingBacSi = bacSiRepository.findByMaTaiKhoan(normalizedMaTaiKhoan).orElse(null);
        if (existingBacSi != null) {
            String currentStatus = normalizeOptional(existingBacSi.getTrangThaiHoSo());
            if (TrangThaiHoSoBacSi.KHOA.name().equals(currentStatus)) {
                throw new IllegalArgumentException("Ho so bac si dang bi khoa");
            }
            if (TrangThaiHoSoBacSi.DA_DUYET.name().equals(currentStatus)) {
                return new UpgradeToDoctorResponseDto(
                        false,
                        "Tai khoan da co ho so bac si da duyet",
                        normalizedMaTaiKhoan,
                        existingBacSi.getMaBacSi(),
                        currentStatus,
                        0);
            }
            if (TrangThaiHoSoBacSi.CHO_DUYET.name().equals(currentStatus)) {
                return new UpgradeToDoctorResponseDto(
                        false,
                        "Ho so bac si dang cho duyet",
                        normalizedMaTaiKhoan,
                        existingBacSi.getMaBacSi(),
                        currentStatus,
                        0);
            }
        }

        DoctorRegisterData doctorData =
                validateDoctorProfileForUpgrade(thongTinBacSi, existingBacSi == null ? null : existingBacSi.getMaBacSi());

        BacSi bacSi;
        boolean createdNew = false;
        if (existingBacSi == null) {
            bacSi = new BacSi();
            bacSi.setMaTaiKhoan(normalizedMaTaiKhoan);
            createdNew = true;
        } else {
            bacSi = existingBacSi;
        }

        bacSi.setChuyenKhoa(doctorData.chuyenKhoa());
        bacSi.setTrinhDoChuyenMon(doctorData.trinhDoChuyenMon());
        bacSi.setLoaiHinhBacSi(doctorData.loaiHinhBacSi());
        bacSi.setTenCoSoYTe(doctorData.tenCoSoYTe());
        bacSi.setDiaChiLamViec(doctorData.diaChiLamViec());
        bacSi.setMaChungChiHanhNghe(doctorData.maChungChiHanhNghe());
        bacSi.setMoTaBanThan(doctorData.moTaBanThan());
        bacSi.setTrangThaiHoSo(TrangThaiHoSoBacSi.CHO_DUYET.name());

        BacSi saved = createdNew ? bacSiRepository.insert(bacSi) : bacSiRepository.update(bacSi);

        int uploadedCount = uploadMinhChungIfAny(saved.getMaBacSi(), tieuDeTaiLieu, files);

        return new UpgradeToDoctorResponseDto(
                true,
                createdNew
                        ? "Gui yeu cau mo tai khoan bac si thanh cong, ho so dang cho duyet"
                        : "Cap nhat ho so bac si va gui lai yeu cau duyet thanh cong",
                normalizedMaTaiKhoan,
                saved.getMaBacSi(),
                saved.getTrangThaiHoSo(),
                uploadedCount);
    }

    private void validatePassword(String matKhau, String xacNhanMatKhau) {
        if (matKhau.length() < 8) {
            throw new IllegalArgumentException("matKhau phai co it nhat 8 ky tu");
        }
        if (!matKhau.equals(xacNhanMatKhau)) {
            throw new IllegalArgumentException("matKhau va xacNhanMatKhau khong khop");
        }
    }

    private RegisterUserInfo validateAndNormalizeUserInfo(RegisterUserInfoRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("thongTinNguoiDung is required");
        }

        String tenDangNhap = normalizeTenDangNhap(request.tenDangNhap());
        String matKhau = requireNotBlank(request.matKhau(), "matKhau");
        String xacNhanMatKhau = requireNotBlank(request.xacNhanMatKhau(), "xacNhanMatKhau");
        String hoLot = requireNotBlank(request.hoLot(), "hoLot");
        String ten = requireNotBlank(request.ten(), "ten");
        String soDienThoai = normalizeSoDienThoai(request.soDienThoai());
        String email = normalizeEmail(request.email());
        String cccd = normalizeCccd(request.cccd());
        String anhDaiDien = normalizeOptional(request.anhDaiDien());

        validatePassword(matKhau, xacNhanMatKhau);

        return new RegisterUserInfo(
                tenDangNhap, matKhau, hoLot, ten, soDienThoai, email, cccd, anhDaiDien);
    }

    private String buildRegisterSuccessMessage(String vaiTro) {
        if (ROLE_BAC_SI.equals(vaiTro)) {
            return "Dang ky tai khoan bac si thanh cong, ho so dang cho duyet";
        }
        return "Dang ky tai khoan nguoi dung thanh cong";
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
            String vaiTro, RegisterDoctorProfileRequestDto request) {
        if (!ROLE_BAC_SI.equals(vaiTro)) {
            return null;
        }
        if (request == null) {
            throw new IllegalArgumentException("thongTinBacSi is required");
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

    private DoctorRegisterData validateDoctorProfileForUpgrade(
            RegisterDoctorProfileRequestDto request, Integer currentMaBacSi) {
        if (request == null) {
            throw new IllegalArgumentException("thongTinBacSi is required");
        }

        String chuyenKhoa = requireNotBlank(request.chuyenKhoa(), "chuyenKhoa");
        String trinhDoChuyenMon = requireNotBlank(request.trinhDoChuyenMon(), "trinhDoChuyenMon");
        String loaiHinhBacSi = requireNotBlank(request.loaiHinhBacSi(), "loaiHinhBacSi");
        String tenCoSoYTe = requireNotBlank(request.tenCoSoYTe(), "tenCoSoYTe");
        String diaChiLamViec = normalizeOptional(request.diaChiLamViec());
        String maChungChiHanhNghe =
                requireNotBlank(request.maChungChiHanhNghe(), "maChungChiHanhNghe");
        String moTaBanThan = normalizeOptional(request.moTaBanThan());

        BacSi existing = bacSiRepository.findByMaChungChiHanhNghe(maChungChiHanhNghe).orElse(null);
        if (existing != null && (currentMaBacSi == null || !currentMaBacSi.equals(existing.getMaBacSi()))) {
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

    private int uploadMinhChungIfAny(
            Integer maBacSi, List<String> tieuDeTaiLieu, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return 0;
        }
        if (tieuDeTaiLieu == null || tieuDeTaiLieu.isEmpty()) {
            throw new IllegalArgumentException("tieuDeTaiLieu is required when files is provided");
        }
        if (tieuDeTaiLieu.size() != files.size()) {
            throw new IllegalArgumentException("tieuDeTaiLieu must have the same size as files");
        }

        int uploadedCount = 0;
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String title = tieuDeTaiLieu.get(i);
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("file is required");
            }
            doctorDocumentService.uploadDocument(maBacSi, title, file);
            uploadedCount++;
        }
        return uploadedCount;
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

    private Integer normalizePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return id;
    }

    private String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown";
        }
        return clientIp.trim();
    }

    private AccountProfileContext resolveAccountProfileContext(Integer maTaiKhoan) {
        TaiKhoan taiKhoan =
                taiKhoanRepository
                        .selectById(maTaiKhoan)
                        .orElseThrow(() -> new IllegalArgumentException("Tai khoan khong ton tai"));

        NguoiDung nguoiDung =
                nguoiDungRepository
                        .findByMaTaiKhoan(maTaiKhoan)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Thong tin nguoi dung khong ton tai"));

        return new AccountProfileContext(taiKhoan, nguoiDung);
    }

    private boolean isPasswordMatchedWithMigration(TaiKhoan taiKhoan, String plainPassword) {
        String storedHash = taiKhoan.getMatKhauHash();
        if (passwordHashHelper.matches(plainPassword, storedHash)) {
            return true;
        }

        if (!isLegacySha256Hash(storedHash)) {
            return false;
        }

        String legacyHash = sha256Hex(plainPassword);
        if (!legacyHash.equalsIgnoreCase(storedHash)) {
            return false;
        }

        taiKhoan.setMatKhauHash(passwordHashHelper.hashPassword(plainPassword));
        taiKhoanRepository.update(taiKhoan);
        return true;
    }

    private boolean isLegacySha256Hash(String hash) {
        return hash != null && hash.matches("^[a-fA-F0-9]{64}$");
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Cannot initialize SHA-256", ex);
        }
    }

    private record DoctorRegisterData(
            String chuyenKhoa,
            String trinhDoChuyenMon,
            String loaiHinhBacSi,
            String tenCoSoYTe,
            String diaChiLamViec,
            String maChungChiHanhNghe,
            String moTaBanThan) {}

    private record RegisterUserInfo(
            String tenDangNhap,
            String matKhau,
            String hoLot,
            String ten,
            String soDienThoai,
            String email,
            String cccd,
            String anhDaiDien) {}

    private record AccountProfileContext(TaiKhoan taiKhoan, NguoiDung nguoiDung) {}
}
