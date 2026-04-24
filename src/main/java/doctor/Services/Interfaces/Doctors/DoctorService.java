package doctor.Services.Interfaces.Doctors;

import doctor.Models.DTOs.Doctors.Requests.UpdateDoctorProfileRequestDto;
import doctor.Models.DTOs.Doctors.Responses.DoctorProfileResponseDto;
import java.util.List;

public interface DoctorService {
    List<DoctorProfileResponseDto> getAllDoctors();

    DoctorProfileResponseDto getDoctorProfileById(Integer maBacSi);

    DoctorProfileResponseDto getDoctorProfileByTaiKhoanId(Integer maTaiKhoan);

    List<DoctorProfileResponseDto> searchDoctors(
            String keyword,
            String chuyenKhoa,
            String diaChiLamViec,
            String trangThaiHoSo,
            Integer limit,
            Integer offset);

    DoctorProfileResponseDto updateDoctorProfile(
            Integer maBacSi, UpdateDoctorProfileRequestDto request);
}
