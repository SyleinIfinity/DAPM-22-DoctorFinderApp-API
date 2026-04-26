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
import doctor.Services.Interfaces.Auth.OtpService;
import doctor.Services.Interfaces.DoctorDocuments.DoctorDocumentService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class AuthServiceImplTests {
    private final TaiKhoanRepository taiKhoanRepository = Mockito.mock(TaiKhoanRepository.class);
    private final NguoiDungRepository nguoiDungRepository = Mockito.mock(NguoiDungRepository.class);
    private final BacSiRepository bacSiRepository = Mockito.mock(BacSiRepository.class);
    private final PasswordHashHelper passwordHashHelper = Mockito.mock(PasswordHashHelper.class);
    private final OtpService otpService = Mockito.mock(OtpService.class);
    private final LoginAttemptService loginAttemptService = Mockito.mock(LoginAttemptService.class);
    private final DoctorDocumentService doctorDocumentService = Mockito.mock(DoctorDocumentService.class);

    private final AuthServiceImpl authService =
            new AuthServiceImpl(
                    taiKhoanRepository,
                    nguoiDungRepository,
                    bacSiRepository,
                    passwordHashHelper,
                    otpService,
                    loginAttemptService,
                    doctorDocumentService);

    @Test
    void registerNguoiDung_shouldCreateTaiKhoanAndNguoiDung() {
        RegisterUserInfoRequestDto thongTinNguoiDung =
                new RegisterUserInfoRequestDto(
                        "khanh123",
                        "abc12345",
                        "abc12345",
                        "Phan Van",
                        "Khanh",
                        "0987654321",
                        "khanh@example.com",
                        "012345678901",
                        null);
        RegisterUserAccountRequestDto request =
                new RegisterUserAccountRequestDto(thongTinNguoiDung, "otp-proof-token");

        Mockito.doNothing()
                .when(otpService)
                .consumeOtpProofToken("otp-proof-token", "khanh@example.com", "REGISTER");

        Mockito.when(taiKhoanRepository.existsByTenDangNhap("khanh123")).thenReturn(false);
        Mockito.when(nguoiDungRepository.existsBySoDienThoai("0987654321")).thenReturn(false);
        Mockito.when(nguoiDungRepository.existsByEmail("khanh@example.com")).thenReturn(false);
        Mockito.when(nguoiDungRepository.existsByCccd("012345678901")).thenReturn(false);
        Mockito.when(passwordHashHelper.hashPassword("abc12345")).thenReturn("hashed-value");

        Mockito.when(taiKhoanRepository.insert(ArgumentMatchers.any(TaiKhoan.class)))
                .thenAnswer(
                        invocation -> {
                            TaiKhoan entity = invocation.getArgument(0);
                            entity.setMaTaiKhoan(10);
                            return entity;
                        });

        Mockito.when(nguoiDungRepository.insert(ArgumentMatchers.any(NguoiDung.class)))
                .thenAnswer(
                        invocation -> {
                            NguoiDung entity = invocation.getArgument(0);
                            entity.setMaNguoiDung(99);
                            return entity;
                        });

        RegisterResponseDto response = authService.registerUser(request);

        Assertions.assertTrue(response.registered());
        Assertions.assertEquals(10, response.maTaiKhoan());
        Assertions.assertEquals(99, response.maNguoiDung());
        Assertions.assertEquals("NGUOI_DUNG", response.vaiTro());
        Assertions.assertNull(response.maBacSi());
    }

    @Test
    void registerBacSi_shouldCreateTaiKhoanNguoiDungAndBacSi() {
        RegisterUserInfoRequestDto thongTinNguoiDung =
                new RegisterUserInfoRequestDto(
                        "doctor01",
                        "abc12345",
                        "abc12345",
                        "Tran",
                        "Nam",
                        "0977000111",
                        "doctor@example.com",
                        "123456789012",
                        "https://img/doc-avatar.png");

        RegisterDoctorProfileRequestDto thongTinBacSi =
                new RegisterDoctorProfileRequestDto(
                        "Tim mach",
                        "Thac si",
                        "Noi tru",
                        "Benh vien A",
                        "TP.HCM",
                        "CCHN-001",
                        "Mo ta ban than");

        RegisterDoctorAccountRequestDto request =
                new RegisterDoctorAccountRequestDto(
                        thongTinNguoiDung, thongTinBacSi, "otp-proof-token-doc");

        Mockito.doNothing()
                .when(otpService)
                .consumeOtpProofToken("otp-proof-token-doc", "doctor@example.com", "REGISTER");

        Mockito.when(taiKhoanRepository.existsByTenDangNhap("doctor01")).thenReturn(false);
        Mockito.when(nguoiDungRepository.existsBySoDienThoai("0977000111")).thenReturn(false);
        Mockito.when(nguoiDungRepository.existsByEmail("doctor@example.com")).thenReturn(false);
        Mockito.when(nguoiDungRepository.existsByCccd("123456789012")).thenReturn(false);
        Mockito.when(bacSiRepository.findByMaChungChiHanhNghe("CCHN-001")).thenReturn(Optional.empty());
        Mockito.when(passwordHashHelper.hashPassword("abc12345")).thenReturn("hashed-doctor");

        Mockito.when(taiKhoanRepository.insert(ArgumentMatchers.any(TaiKhoan.class)))
                .thenAnswer(
                        invocation -> {
                            TaiKhoan entity = invocation.getArgument(0);
                            entity.setMaTaiKhoan(20);
                            return entity;
                        });

        Mockito.when(nguoiDungRepository.insert(ArgumentMatchers.any(NguoiDung.class)))
                .thenAnswer(
                        invocation -> {
                            NguoiDung entity = invocation.getArgument(0);
                            entity.setMaNguoiDung(199);
                            return entity;
                        });

        Mockito.when(bacSiRepository.insert(ArgumentMatchers.any(BacSi.class)))
                .thenAnswer(
                        invocation -> {
                            BacSi entity = invocation.getArgument(0);
                            entity.setMaBacSi(299);
                            return entity;
                        });

        RegisterResponseDto response = authService.registerDoctor(request);

        Assertions.assertTrue(response.registered());
        Assertions.assertEquals(20, response.maTaiKhoan());
        Assertions.assertEquals(199, response.maNguoiDung());
        Assertions.assertEquals(299, response.maBacSi());
        Assertions.assertEquals("BAC_SI", response.vaiTro());

        Mockito.verify(bacSiRepository)
                .insert(
                        ArgumentMatchers.argThat(
                                bacSi ->
                                        TrangThaiHoSoBacSi.CHO_DUYET.name()
                                                .equals(bacSi.getTrangThaiHoSo())));
    }

    @Test
    void login_shouldReturnAuthenticatedWhenPasswordMatches() {
        LoginRequestDto request = new LoginRequestDto("khanh123", "abc12345");
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setMaTaiKhoan(10);
        taiKhoan.setTenDangNhap("khanh123");
        taiKhoan.setMatKhauHash("hashed-value");
        taiKhoan.setVaiTro("NGUOI_DUNG");
        taiKhoan.setTrangThaiHoatDong(TrangThaiHoatDongTaiKhoan.HOAT_DONG.name());

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setMaNguoiDung(99);

        Mockito.when(taiKhoanRepository.findByTenDangNhap("khanh123"))
                .thenReturn(Optional.of(taiKhoan));
        Mockito.when(passwordHashHelper.matches("abc12345", "hashed-value")).thenReturn(true);
        Mockito.when(nguoiDungRepository.findByMaTaiKhoan(10)).thenReturn(Optional.of(nguoiDung));
        Mockito.when(bacSiRepository.findByMaTaiKhoan(10)).thenReturn(Optional.empty());

        LoginResponseDto response = authService.login(request, "127.0.0.1");

        Assertions.assertTrue(response.authenticated());
        Assertions.assertEquals(10, response.maTaiKhoan());
        Assertions.assertEquals(99, response.maNguoiDung());
    }

    @Test
    void getAccountInfo_shouldReturnAccountProfile() {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setMaTaiKhoan(10);
        taiKhoan.setTenDangNhap("khanh123");
        taiKhoan.setVaiTro("NGUOI_DUNG");
        taiKhoan.setTrangThaiHoatDong("HOAT_DONG");
        taiKhoan.setNgayTao(LocalDateTime.of(2026, 4, 24, 12, 0));

        NguoiDung nguoiDung =
                new NguoiDung(
                        99,
                        10,
                        "Phan Van",
                        "Khanh",
                        "0987654321",
                        "khanh@example.com",
                        "012345678901",
                        "https://img/avatar.png");

        Mockito.when(taiKhoanRepository.selectById(10)).thenReturn(Optional.of(taiKhoan));
        Mockito.when(nguoiDungRepository.findByMaTaiKhoan(10)).thenReturn(Optional.of(nguoiDung));

        AccountInfoResponseDto response = authService.getAccountInfo(10);

        Assertions.assertEquals(10, response.maTaiKhoan());
        Assertions.assertEquals("khanh123", response.tenDangNhap());
        Assertions.assertEquals(99, response.maNguoiDung());
        Assertions.assertEquals("khanh@example.com", response.email());
    }

    @Test
    void getAccountInfoWithDoctor_shouldReturnDoctorInfoWhenExists() {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setMaTaiKhoan(10);
        taiKhoan.setTenDangNhap("doctor01");
        taiKhoan.setVaiTro("BAC_SI");
        taiKhoan.setTrangThaiHoatDong("HOAT_DONG");
        taiKhoan.setNgayTao(LocalDateTime.of(2026, 4, 24, 12, 0));

        NguoiDung nguoiDung =
                new NguoiDung(
                        99,
                        10,
                        "Tran",
                        "Nam",
                        "0987654321",
                        "doctor@example.com",
                        "012345678901",
                        null);

        BacSi bacSi = new BacSi();
        bacSi.setMaBacSi(55);
        bacSi.setMaTaiKhoan(10);
        bacSi.setChuyenKhoa("Tim mach");
        bacSi.setTrinhDoChuyenMon("Thac si");
        bacSi.setLoaiHinhBacSi("Noi tru");
        bacSi.setTenCoSoYTe("Benh vien A");
        bacSi.setDiaChiLamViec("TP.HCM");
        bacSi.setMaChungChiHanhNghe("CCHN-001");
        bacSi.setMoTaBanThan("Mo ta");
        bacSi.setTrangThaiHoSo("CHO_DUYET");

        Mockito.when(taiKhoanRepository.selectById(10)).thenReturn(Optional.of(taiKhoan));
        Mockito.when(nguoiDungRepository.findByMaTaiKhoan(10)).thenReturn(Optional.of(nguoiDung));
        Mockito.when(bacSiRepository.findByMaTaiKhoan(10)).thenReturn(Optional.of(bacSi));

        AccountDoctorInfoResponseDto response = authService.getAccountInfoWithDoctor(10);

        Assertions.assertTrue(response.coTaiKhoanBacSi());
        Assertions.assertEquals(55, response.maBacSi());
        Assertions.assertEquals("Tim mach", response.chuyenKhoa());
        Assertions.assertEquals("CCHN-001", response.maChungChiHanhNghe());
    }
}
