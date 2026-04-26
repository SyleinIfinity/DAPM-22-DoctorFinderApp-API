package doctor.Services.Interfaces.Appointments;

import doctor.Models.DTOs.Appointments.Requests.CreateAppointmentRequestDto;
import doctor.Models.DTOs.Appointments.Requests.RejectAppointmentRequestDto;
import doctor.Models.DTOs.Appointments.Responses.AppointmentDetailResponseDto;
import doctor.Models.DTOs.Appointments.Responses.AppointmentRequestResponseDto;
import doctor.Models.DTOs.Appointments.Responses.AppointmentSummaryResponseDto;
import java.util.List;

public interface AppointmentService {
    AppointmentDetailResponseDto createAppointment(CreateAppointmentRequestDto request);

    List<AppointmentSummaryResponseDto> getAppointments(Integer maNguoiDung, String scope);

    AppointmentDetailResponseDto getAppointmentDetail(Integer maPhieuDatLich);

    AppointmentDetailResponseDto cancelAppointment(Integer maPhieuDatLich);

    List<AppointmentRequestResponseDto> getAppointmentRequestsByDoctor(Integer maBacSi);

    AppointmentDetailResponseDto approveAppointment(Integer maPhieuDatLich);

    AppointmentDetailResponseDto rejectAppointment(
            Integer maPhieuDatLich, RejectAppointmentRequestDto request);
}

