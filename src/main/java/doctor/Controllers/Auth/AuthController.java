package doctor.Controllers.Auth;

import doctor.Models.DTOs.Auth.Requests.LoginRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterRequestDto;
import doctor.Models.DTOs.Auth.Responses.ErrorResponseDto;
import doctor.Models.DTOs.Auth.Responses.LoginResponseDto;
import doctor.Models.DTOs.Auth.Responses.RegisterResponseDto;
import doctor.Services.Interfaces.Auth.AuthService;
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
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto("Request body is required"));
        }

        try {
            RegisterResponseDto response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Cannot register: " + ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto("Request body is required"));
        }

        try {
            LoginResponseDto response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Cannot login: " + ex.getMessage()));
        }
    }
}
