package doctor.Services.Interfaces.Users;

import doctor.Models.DTOs.Users.Requests.UpdateUserProfileRequestDto;
import doctor.Models.DTOs.Users.Responses.UserProfileResponseDto;
import java.util.List;

public interface UserService {
    List<UserProfileResponseDto> getAllUsers();

    UserProfileResponseDto getUserProfileById(Integer maNguoiDung);

    UserProfileResponseDto getUserProfileByTaiKhoanId(Integer maTaiKhoan);

    UserProfileResponseDto updateUserProfile(Integer maNguoiDung, UpdateUserProfileRequestDto request);
}
