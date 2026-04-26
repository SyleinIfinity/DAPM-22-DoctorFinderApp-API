package doctor.Services.Interfaces.Admin;

import doctor.Models.DTOs.Admin.Requests.RejectDoctorProfileRequestDto;
import doctor.Models.DTOs.Admin.Responses.AdminDoctorProfileActionResponseDto;
import doctor.Models.DTOs.Admin.Responses.AdminDoctorProfileDetailResponseDto;
import doctor.Models.DTOs.Admin.Responses.PendingDoctorProfileResponseDto;
import java.util.List;

public interface AdminDoctorService {
    List<PendingDoctorProfileResponseDto> getPendingDoctors();

    AdminDoctorProfileDetailResponseDto getDoctorDetail(Integer maBacSi);

    AdminDoctorProfileActionResponseDto approveDoctorProfile(Integer maBacSi);

    AdminDoctorProfileActionResponseDto rejectDoctorProfile(
            Integer maBacSi, RejectDoctorProfileRequestDto request);
}

