package doctor.Controllers.Auth;

import doctor.Models.DTOs.Auth.Requests.LoginRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterDoctorAccountRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterDoctorProfileRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterUserAccountRequestDto;
import doctor.Models.DTOs.Auth.Requests.UpgradeToDoctorRequestDto;
import doctor.Models.DTOs.Auth.Responses.AccountDoctorInfoResponseDto;
import doctor.Models.DTOs.Auth.Responses.AccountInfoResponseDto;
import doctor.Models.DTOs.Auth.Responses.LoginResponseDto;
import doctor.Models.DTOs.Auth.Responses.RegisterResponseDto;
import doctor.Models.DTOs.Auth.Responses.UpgradeToDoctorResponseDto;
import doctor.Services.Interfaces.Auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register/user")
    public ResponseEntity<RegisterResponseDto> registerUser(
            @RequestBody RegisterUserAccountRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        RegisterResponseDto response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<RegisterResponseDto> registerDoctor(
            @RequestBody RegisterDoctorAccountRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        RegisterResponseDto response = authService.registerDoctor(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto request, HttpServletRequest servletRequest) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        LoginResponseDto response = authService.login(request, resolveClientIp(servletRequest));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upgrade-to-doctor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpgradeToDoctorResponseDto> upgradeToDoctor(
            @RequestParam Integer maTaiKhoan,
            @RequestPart("thongTinBacSi") RegisterDoctorProfileRequestDto thongTinBacSi,
            @RequestParam(value = "tieuDeTaiLieu", required = false) List<String> tieuDeTaiLieu,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        UpgradeToDoctorResponseDto response =
                authService.upgradeToDoctor(maTaiKhoan, thongTinBacSi, tieuDeTaiLieu, files);
        return ResponseEntity.status(response.upgraded() ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }

    @PostMapping(value = "/upgrade-to-doctor", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpgradeToDoctorResponseDto> upgradeToDoctorJson(
            @RequestBody UpgradeToDoctorRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        UpgradeToDoctorResponseDto response =
                authService.upgradeToDoctor(request.maTaiKhoan(), request.thongTinBacSi(), null, null);
        return ResponseEntity.status(response.upgraded() ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }

    @GetMapping("/account/{maTaiKhoan}")
    public ResponseEntity<AccountInfoResponseDto> getAccountInfo(@PathVariable Integer maTaiKhoan) {
        return ResponseEntity.ok(authService.getAccountInfo(maTaiKhoan));
    }

    @GetMapping("/account/{maTaiKhoan}/doctor")
    public ResponseEntity<AccountDoctorInfoResponseDto> getAccountInfoWithDoctor(
            @PathVariable Integer maTaiKhoan) {
        return ResponseEntity.ok(authService.getAccountInfoWithDoctor(maTaiKhoan));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
