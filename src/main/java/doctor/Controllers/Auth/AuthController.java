package doctor.Controllers.Auth;

import doctor.Models.DTOs.Auth.Requests.LoginRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterDoctorAccountRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterUserAccountRequestDto;
import doctor.Models.DTOs.Auth.Responses.AccountDoctorInfoResponseDto;
import doctor.Models.DTOs.Auth.Responses.AccountInfoResponseDto;
import doctor.Models.DTOs.Auth.Responses.LoginResponseDto;
import doctor.Models.DTOs.Auth.Responses.RegisterResponseDto;
import doctor.Services.Interfaces.Auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<RegisterResponseDto> registerDoctor(
            @RequestBody RegisterDoctorAccountRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        RegisterResponseDto response = authService.registerDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
