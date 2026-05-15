package doctor.Services.Interfaces.Users;

import doctor.Models.DTOs.Users.Requests.UpdateUserProfileRequestDto;
import doctor.Models.DTOs.Users.Responses.UserProfileResponseDto;
import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    List<UserProfileResponseDto> getAllUsers();

    UserProfileResponseDto getUserProfileById(Integer maNguoiDung);

    UserProfileResponseDto getUserProfileByTaiKhoanId(Integer maTaiKhoan);

    UserProfileResponseDto updateUserProfile(Integer maNguoiDung, UpdateUserProfileRequestDto request);

    UserProfileResponseDto updateUserAvatar(Integer maNguoiDung, MultipartFile avatar)
            throws IOException;
}
