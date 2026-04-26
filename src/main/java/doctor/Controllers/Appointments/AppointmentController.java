package doctor.Controllers.Appointments;

import doctor.Models.DTOs.Appointments.Requests.CreateAppointmentRequestDto;
import doctor.Models.DTOs.Appointments.Requests.RejectAppointmentRequestDto;
import doctor.Models.DTOs.Appointments.Responses.AppointmentDetailResponseDto;
import doctor.Models.DTOs.Appointments.Responses.AppointmentSummaryResponseDto;
import doctor.Services.Interfaces.Appointments.AppointmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentDetailResponseDto> createAppointment(
            @RequestBody CreateAppointmentRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        AppointmentDetailResponseDto created = appointmentService.createAppointment(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentSummaryResponseDto>> getAppointments(
            @RequestParam Integer maNguoiDung, @RequestParam String scope) {
        return ResponseEntity.ok(appointmentService.getAppointments(maNguoiDung, scope));
    }

    @GetMapping("/{maPhieuDatLich}")
    public ResponseEntity<AppointmentDetailResponseDto> getAppointmentDetail(
            @PathVariable Integer maPhieuDatLich) {
        return ResponseEntity.ok(appointmentService.getAppointmentDetail(maPhieuDatLich));
    }

    @PostMapping("/{maPhieuDatLich}/cancel")
    public ResponseEntity<AppointmentDetailResponseDto> cancelAppointment(
            @PathVariable Integer maPhieuDatLich) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(maPhieuDatLich));
    }

    @PostMapping("/{maPhieuDatLich}/approve")
    public ResponseEntity<AppointmentDetailResponseDto> approveAppointment(
            @PathVariable Integer maPhieuDatLich) {
        return ResponseEntity.ok(appointmentService.approveAppointment(maPhieuDatLich));
    }

    @PostMapping("/{maPhieuDatLich}/reject")
    public ResponseEntity<AppointmentDetailResponseDto> rejectAppointment(
            @PathVariable Integer maPhieuDatLich,
            @RequestBody RejectAppointmentRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        return ResponseEntity.ok(appointmentService.rejectAppointment(maPhieuDatLich, request));
    }
}
