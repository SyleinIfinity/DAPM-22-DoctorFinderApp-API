package doctor.Controllers.Admin;

import doctor.Models.DTOs.Admin.Requests.RejectDoctorProfileRequestDto;
import doctor.Models.DTOs.Admin.Responses.AdminDoctorProfileActionResponseDto;
import doctor.Models.DTOs.Admin.Responses.AdminDoctorProfileDetailResponseDto;
import doctor.Models.DTOs.Admin.Responses.PendingDoctorProfileResponseDto;
import doctor.Services.Interfaces.Admin.AdminDoctorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/doctors")
@RequiredArgsConstructor
public class AdminDoctorController {
    private final AdminDoctorService adminDoctorService;

    @GetMapping("/pending")
    public ResponseEntity<List<PendingDoctorProfileResponseDto>> getPendingDoctors() {
        return ResponseEntity.ok(adminDoctorService.getPendingDoctors());
    }

    @GetMapping("/{maBacSi}")
    public ResponseEntity<AdminDoctorProfileDetailResponseDto> getDoctorDetail(@PathVariable Integer maBacSi) {
        return ResponseEntity.ok(adminDoctorService.getDoctorDetail(maBacSi));
    }

    @PostMapping("/{maBacSi}/approve")
    public ResponseEntity<AdminDoctorProfileActionResponseDto> approveDoctorProfile(@PathVariable Integer maBacSi) {
        return ResponseEntity.ok(adminDoctorService.approveDoctorProfile(maBacSi));
    }

    @PostMapping("/{maBacSi}/reject")
    public ResponseEntity<AdminDoctorProfileActionResponseDto> rejectDoctorProfile(
            @PathVariable Integer maBacSi, @RequestBody RejectDoctorProfileRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        return ResponseEntity.ok(adminDoctorService.rejectDoctorProfile(maBacSi, request));
    }
}

