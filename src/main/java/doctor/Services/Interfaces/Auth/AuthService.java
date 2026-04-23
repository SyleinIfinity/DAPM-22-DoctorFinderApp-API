package doctor.Services.Interfaces.Auth;

import doctor.Models.DTOs.Auth.Requests.LoginRequestDto;
import doctor.Models.DTOs.Auth.Requests.RegisterRequestDto;
import doctor.Models.DTOs.Auth.Responses.LoginResponseDto;
import doctor.Models.DTOs.Auth.Responses.RegisterResponseDto;

public interface AuthService {
    RegisterResponseDto register(RegisterRequestDto request);

    LoginResponseDto login(LoginRequestDto request);
}
