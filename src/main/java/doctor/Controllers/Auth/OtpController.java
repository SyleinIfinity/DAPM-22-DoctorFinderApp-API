package doctor.Controllers.Auth;

import doctor.Models.DTOs.Auth.Requests.ConsumeOtpRequestDto;
import doctor.Models.DTOs.Auth.Requests.SendOtpRequestDto;
import doctor.Models.DTOs.Auth.Requests.VerifyOtpRequestDto;
import doctor.Models.DTOs.Auth.Responses.ErrorResponseDto;
import doctor.Models.DTOs.Auth.Responses.OtpConsumeResponseDto;
import doctor.Models.DTOs.Auth.Responses.OtpSendResponseDto;
import doctor.Models.DTOs.Auth.Responses.OtpVerifyResponseDto;
import doctor.Services.Interfaces.Auth.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> sendOtp(@RequestBody SendOtpRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto("Request body is required"));
        }

        try {
            OtpSendResponseDto result =
                    otpService.sendOtp(
                            request.email(),
                            request.purpose(),
                            Boolean.TRUE.equals(request.forceResend()));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Cannot send OTP: " + ex.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto("Request body is required"));
        }

        try {
            OtpVerifyResponseDto result =
                    otpService.verifyOtp(request.email(), request.purpose(), request.otpCode());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Cannot verify OTP: " + ex.getMessage()));
        }
    }

    @PostMapping("/consume")
    public ResponseEntity<?> consumeOtp(@RequestBody ConsumeOtpRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto("Request body is required"));
        }

        try {
            OtpConsumeResponseDto result =
                    otpService.consumeVerifiedOtp(request.email(), request.purpose());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Cannot consume OTP: " + ex.getMessage()));
        }
    }
}
