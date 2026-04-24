package doctor.Controllers.Auth;

import doctor.Models.DTOs.Auth.Requests.LoginRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterRequestDto;
import doctor.Models.DTOs.Auth.Responses.LoginResponseDto;
import doctor.Models.DTOs.Auth.Responses.RegisterResponseDto;
import doctor.Services.Interfaces.Auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        RegisterResponseDto response = authService.register(request);
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

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
