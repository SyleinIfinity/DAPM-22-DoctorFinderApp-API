package doctor.Services.Business.Auth;

import doctor.Models.DTOs.Auth.Requests.LoginRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterRequestDto;
import doctor.Models.DTOs.Auth.Responses.LoginResponseDto;
import doctor.Models.DTOs.Auth.Responses.RegisterResponseDto;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiKhoan;
import doctor.Models.Enums.TrangThaiHoatDongTaiKhoan;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import doctor.Security.LoginAttemptService;
import doctor.Security.PasswordHashHelper;
import doctor.Services.Interfaces.Auth.OtpService;
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

    private final AuthServiceImpl authService =
            new AuthServiceImpl(
                    taiKhoanRepository,
                    nguoiDungRepository,
                    bacSiRepository,
                    passwordHashHelper,
                    otpService,
                    loginAttemptService);

    @Test
    void registerNguoiDung_shouldCreateTaiKhoanAndNguoiDung() {
        RegisterRequestDto request =
                new RegisterRequestDto(
                        "khanh123",
                        "abc12345",
                        "abc12345",
                        "NGUOI_DUNG",
                        "Phan Van",
                        "Khanh",
                        "0987654321",
                        "khanh@example.com",
                        "otp-proof-token",
                        "012345678901",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

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

        RegisterResponseDto response = authService.register(request);

        Assertions.assertTrue(response.registered());
        Assertions.assertEquals(10, response.maTaiKhoan());
        Assertions.assertEquals(99, response.maNguoiDung());
        Assertions.assertNull(response.maBacSi());
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
}
