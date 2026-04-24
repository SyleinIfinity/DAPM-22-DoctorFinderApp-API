package doctor.Controllers.Auth;

import doctor.Models.DTOs.Auth.Requests.SendOtpRequestDto;
import doctor.Models.DTOs.Auth.Requests.VerifyOtpRequestDto;
import doctor.Models.DTOs.Auth.Responses.OtpSendResponseDto;
import doctor.Models.DTOs.Auth.Responses.OtpVerifyResponseDto;
import doctor.Services.Interfaces.Auth.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<OtpSendResponseDto> sendOtp(
            @RequestBody SendOtpRequestDto request, HttpServletRequest servletRequest) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        OtpSendResponseDto result =
                otpService.sendOtp(
                        request.email(),
                        request.purpose(),
                        Boolean.TRUE.equals(request.forceResend()),
                        resolveClientIp(servletRequest));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpVerifyResponseDto> verifyOtp(
            @RequestBody VerifyOtpRequestDto request, HttpServletRequest servletRequest) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        OtpVerifyResponseDto result =
                otpService.verifyOtp(
                        request.email(),
                        request.purpose(),
                        request.otpCode(),
                        resolveClientIp(servletRequest));
        return ResponseEntity.ok(result);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
