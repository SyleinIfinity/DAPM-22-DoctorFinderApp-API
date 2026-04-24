package doctor.Services.Interfaces.Auth;

import doctor.Models.DTOs.Auth.Requests.LoginRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterDoctorAccountRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterUserAccountRequestDto;
import doctor.Models.DTOs.Auth.Responses.AccountDoctorInfoResponseDto;
import doctor.Models.DTOs.Auth.Responses.AccountInfoResponseDto;
import doctor.Models.DTOs.Auth.Responses.LoginResponseDto;
import doctor.Models.DTOs.Auth.Responses.RegisterResponseDto;

public interface AuthService {
    RegisterResponseDto registerUser(RegisterUserAccountRequestDto request);

    RegisterResponseDto registerDoctor(RegisterDoctorAccountRequestDto request);

    LoginResponseDto login(LoginRequestDto request, String clientIp);

    AccountInfoResponseDto getAccountInfo(Integer maTaiKhoan);

    AccountDoctorInfoResponseDto getAccountInfoWithDoctor(Integer maTaiKhoan);
}
