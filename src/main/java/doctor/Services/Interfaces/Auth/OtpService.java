package doctor.Services.Interfaces.Auth;

import doctor.Models.DTOs.Auth.Responses.OtpSendResponseDto;
import doctor.Models.DTOs.Auth.Responses.OtpVerifyResponseDto;

public interface OtpService {
    OtpSendResponseDto sendOtp(String email, String purpose, boolean forceResend, String clientIp);

    OtpVerifyResponseDto verifyOtp(String email, String purpose, String otpCode, String clientIp);

    void consumeOtpProofToken(String otpProofToken, String email, String purpose);
}
