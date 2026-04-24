package doctor.Services.Business.Users;

import doctor.Models.DTOs.Users.Requests.UpdateUserProfileRequestDto;
import doctor.Models.DTOs.Users.Responses.UserProfileResponseDto;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiKhoan;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class UserServiceImplTests {
    private final NguoiDungRepository nguoiDungRepository = Mockito.mock(NguoiDungRepository.class);
    private final TaiKhoanRepository taiKhoanRepository = Mockito.mock(TaiKhoanRepository.class);
    private final UserServiceImpl userService = new UserServiceImpl(nguoiDungRepository, taiKhoanRepository);

    @Test
    void getUserProfileById_shouldReturnMappedProfile() {
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
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setMaTaiKhoan(10);
        taiKhoan.setTenDangNhap("khanh123");
        taiKhoan.setVaiTro("NGUOI_DUNG");
        taiKhoan.setTrangThaiHoatDong("HOAT_DONG");

        Mockito.when(nguoiDungRepository.selectById(99)).thenReturn(Optional.of(nguoiDung));
        Mockito.when(taiKhoanRepository.selectById(10)).thenReturn(Optional.of(taiKhoan));

        UserProfileResponseDto response = userService.getUserProfileById(99);

        Assertions.assertEquals(99, response.maNguoiDung());
        Assertions.assertEquals(10, response.maTaiKhoan());
        Assertions.assertEquals("khanh123", response.tenDangNhap());
        Assertions.assertEquals("Phan Van Khanh", response.hoTenDayDu());
    }

    @Test
    void updateUserProfile_shouldThrowWhenEmailAlreadyUsed() {
        NguoiDung currentUser =
                new NguoiDung(
                        99, 10, "Phan Van", "Khanh", "0987654321", "khanh@example.com", "012345678901", null);
        NguoiDung existingByEmail =
                new NguoiDung(
                        88,
                        11,
                        "Nguyen",
                        "A",
                        "0911222333",
                        "used@example.com",
                        "999999999999",
                        null);

        UpdateUserProfileRequestDto request =
                new UpdateUserProfileRequestDto(
                        "Phan Van",
                        "Khanh",
                        "0987654321",
                        "used@example.com",
                        "012345678901",
                        null);

        Mockito.when(nguoiDungRepository.selectById(99)).thenReturn(Optional.of(currentUser));
        Mockito.when(nguoiDungRepository.findBySoDienThoai("0987654321"))
                .thenReturn(Optional.of(currentUser));
        Mockito.when(nguoiDungRepository.findByEmail("used@example.com"))
                .thenReturn(Optional.of(existingByEmail));

        Assertions.assertThrows(
                IllegalArgumentException.class, () -> userService.updateUserProfile(99, request));
    }

    @Test
    void updateUserProfile_shouldUpdateAndReturnProfile() {
        NguoiDung currentUser =
                new NguoiDung(
                        99, 10, "Phan Van", "Khanh", "0987654321", "khanh@example.com", "012345678901", null);
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setMaTaiKhoan(10);
        taiKhoan.setTenDangNhap("khanh123");
        taiKhoan.setVaiTro("NGUOI_DUNG");
        taiKhoan.setTrangThaiHoatDong("HOAT_DONG");

        UpdateUserProfileRequestDto request =
                new UpdateUserProfileRequestDto(
                        "Phan",
                        "Khanh",
                        "0911222333",
                        "khanh.new@example.com",
                        "123456789012",
                        "https://img/new-avatar.png");

        Mockito.when(nguoiDungRepository.selectById(99)).thenReturn(Optional.of(currentUser));
        Mockito.when(nguoiDungRepository.findBySoDienThoai("0911222333")).thenReturn(Optional.empty());
        Mockito.when(nguoiDungRepository.findByEmail("khanh.new@example.com")).thenReturn(Optional.empty());
        Mockito.when(nguoiDungRepository.findByCccd("123456789012")).thenReturn(Optional.empty());
        Mockito.when(nguoiDungRepository.update(ArgumentMatchers.any(NguoiDung.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(taiKhoanRepository.selectById(10)).thenReturn(Optional.of(taiKhoan));

        UserProfileResponseDto response = userService.updateUserProfile(99, request);

        Assertions.assertEquals("0911222333", response.soDienThoai());
        Assertions.assertEquals("khanh.new@example.com", response.email());
        Assertions.assertEquals("123456789012", response.cccd());
        Assertions.assertEquals("https://img/new-avatar.png", response.anhDaiDien());
    }
}
