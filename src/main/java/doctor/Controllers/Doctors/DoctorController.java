package doctor.Controllers.Doctors;

import doctor.Models.DTOs.Doctors.Requests.UpdateDoctorProfileRequestDto;
import doctor.Models.DTOs.Doctors.Responses.DoctorProfileResponseDto;
import doctor.Services.Interfaces.Doctors.DoctorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorProfileResponseDto>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{maBacSi}")
    public ResponseEntity<DoctorProfileResponseDto> getDoctorById(@PathVariable Integer maBacSi) {
        return ResponseEntity.ok(doctorService.getDoctorProfileById(maBacSi));
    }

    @GetMapping("/by-account/{maTaiKhoan}")
    public ResponseEntity<DoctorProfileResponseDto> getDoctorByMaTaiKhoan(
            @PathVariable Integer maTaiKhoan) {
        return ResponseEntity.ok(doctorService.getDoctorProfileByTaiKhoanId(maTaiKhoan));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorProfileResponseDto>> searchDoctors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String chuyenKhoa,
            @RequestParam(required = false) String diaChiLamViec,
            @RequestParam(required = false) String trangThaiHoSo,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        return ResponseEntity.ok(
                doctorService.searchDoctors(
                        keyword, chuyenKhoa, diaChiLamViec, trangThaiHoSo, limit, offset));
    }

    @PutMapping("/{maBacSi}")
    public ResponseEntity<DoctorProfileResponseDto> updateDoctorProfile(
            @PathVariable Integer maBacSi, @RequestBody UpdateDoctorProfileRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        return ResponseEntity.ok(doctorService.updateDoctorProfile(maBacSi, request));
    }
}
