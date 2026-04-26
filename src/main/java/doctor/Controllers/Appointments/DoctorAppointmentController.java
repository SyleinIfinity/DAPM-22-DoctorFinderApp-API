package doctor.Controllers.Appointments;

import doctor.Models.DTOs.Appointments.Responses.AppointmentRequestResponseDto;
import doctor.Services.Interfaces.Appointments.AppointmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorAppointmentController {
    private final AppointmentService appointmentService;

    @GetMapping("/{maBacSi}/appointment-requests")
    public ResponseEntity<List<AppointmentRequestResponseDto>> getAppointmentRequests(
            @PathVariable Integer maBacSi) {
        return ResponseEntity.ok(appointmentService.getAppointmentRequestsByDoctor(maBacSi));
    }
}

