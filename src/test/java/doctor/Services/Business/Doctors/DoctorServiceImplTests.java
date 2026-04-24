package doctor.Services.Business.Doctors;

import doctor.Models.DTOs.Doctors.Requests.UpdateDoctorProfileRequestDto;
import doctor.Models.DTOs.Doctors.Responses.DoctorProfileResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiKhoan;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DoctorServiceImplTests {
    private final BacSiRepository bacSiRepository = Mockito.mock(BacSiRepository.class);
    private final NguoiDungRepository nguoiDungRepository = Mockito.mock(NguoiDungRepository.class);
    private final TaiKhoanRepository taiKhoanRepository = Mockito.mock(TaiKhoanRepository.class);
    private final DoctorServiceImpl doctorService =
            new DoctorServiceImpl(bacSiRepository, nguoiDungRepository, taiKhoanRepository);

    @Test
    void getDoctorProfileById_shouldReturnMappedDoctorProfile() {
        BacSi bacSi = createDoctorEntity(55, 10, "CCHN-001");
        TaiKhoan taiKhoan = createAccountEntity(10, "doctor01");
        NguoiDung nguoiDung = createUserEntity(99, 10, "Tran", "Nam");

        Mockito.when(bacSiRepository.selectById(55)).thenReturn(Optional.of(bacSi));
        Mockito.when(taiKhoanRepository.selectById(10)).thenReturn(Optional.of(taiKhoan));
        Mockito.when(nguoiDungRepository.findByMaTaiKhoan(10)).thenReturn(Optional.of(nguoiDung));

        DoctorProfileResponseDto response = doctorService.getDoctorProfileById(55);

        Assertions.assertEquals(55, response.maBacSi());
        Assertions.assertEquals(10, response.maTaiKhoan());
        Assertions.assertEquals(99, response.maNguoiDung());
        Assertions.assertEquals("Tran Nam", response.hoTenDayDu());
        Assertions.assertEquals("Tim mach", response.chuyenKhoa());
    }

    @Test
    void updateDoctorProfile_shouldThrowWhenLicenseAlreadyUsed() {
        BacSi currentDoctor = createDoctorEntity(55, 10, "CCHN-001");
        BacSi existingDoctor = createDoctorEntity(77, 20, "CCHN-NEW");

        UpdateDoctorProfileRequestDto request =
                new UpdateDoctorProfileRequestDto(
                        "Noi tong quat",
                        "Bac si CKI",
                        "Ngoai tru",
                        "Phong kham B",
                        "Ha Noi",
                        "CCHN-NEW",
                        "Mo ta moi");

        Mockito.when(bacSiRepository.selectById(55)).thenReturn(Optional.of(currentDoctor));
        Mockito.when(bacSiRepository.findByMaChungChiHanhNghe("CCHN-NEW"))
                .thenReturn(Optional.of(existingDoctor));

        Assertions.assertThrows(
                IllegalArgumentException.class, () -> doctorService.updateDoctorProfile(55, request));
    }

    @Test
    void searchDoctors_shouldReturnMappedResultList() {
        BacSi bacSi = createDoctorEntity(55, 10, "CCHN-001");
        TaiKhoan taiKhoan = createAccountEntity(10, "doctor01");
        NguoiDung nguoiDung = createUserEntity(99, 10, "Tran", "Nam");

        Mockito.when(
                        bacSiRepository.search(
                                "nam", "tim", "hcm", "DA_DUYET", 10, 0))
                .thenReturn(List.of(bacSi));
        Mockito.when(taiKhoanRepository.selectById(10)).thenReturn(Optional.of(taiKhoan));
        Mockito.when(nguoiDungRepository.findByMaTaiKhoan(10)).thenReturn(Optional.of(nguoiDung));

        List<DoctorProfileResponseDto> result =
                doctorService.searchDoctors("nam", "tim", "hcm", "DA_DUYET", 10, 0);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(55, result.get(0).maBacSi());
        Assertions.assertEquals("DA_DUYET", result.get(0).trangThaiHoSo());
    }

    private BacSi createDoctorEntity(Integer maBacSi, Integer maTaiKhoan, String cchn) {
        BacSi bacSi = new BacSi();
        bacSi.setMaBacSi(maBacSi);
        bacSi.setMaTaiKhoan(maTaiKhoan);
        bacSi.setChuyenKhoa("Tim mach");
        bacSi.setTrinhDoChuyenMon("Thac si");
        bacSi.setLoaiHinhBacSi("Noi tru");
        bacSi.setTenCoSoYTe("Benh vien A");
        bacSi.setDiaChiLamViec("TP.HCM");
        bacSi.setMaChungChiHanhNghe(cchn);
        bacSi.setMoTaBanThan("Mo ta");
        bacSi.setTrangThaiHoSo("DA_DUYET");
        return bacSi;
    }

    private TaiKhoan createAccountEntity(Integer maTaiKhoan, String tenDangNhap) {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setMaTaiKhoan(maTaiKhoan);
        taiKhoan.setTenDangNhap(tenDangNhap);
        taiKhoan.setVaiTro("BAC_SI");
        taiKhoan.setTrangThaiHoatDong("HOAT_DONG");
        return taiKhoan;
    }

    private NguoiDung createUserEntity(Integer maNguoiDung, Integer maTaiKhoan, String hoLot, String ten) {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setMaNguoiDung(maNguoiDung);
        nguoiDung.setMaTaiKhoan(maTaiKhoan);
        nguoiDung.setHoLot(hoLot);
        nguoiDung.setTen(ten);
        nguoiDung.setSoDienThoai("0987654321");
        nguoiDung.setEmail("doctor@example.com");
        nguoiDung.setCccd("012345678901");
        nguoiDung.setAnhDaiDien("https://img/doc-avatar.png");
        return nguoiDung;
    }
}
