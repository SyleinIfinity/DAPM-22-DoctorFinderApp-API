package doctor.Controllers.Users;

import doctor.Models.DTOs.Users.Requests.UpdateUserProfileRequestDto;
import doctor.Models.DTOs.Users.Responses.UserProfileResponseDto;
import doctor.Services.Interfaces.Users.UserService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserProfileResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{maNguoiDung}")
    public ResponseEntity<UserProfileResponseDto> getUserById(@PathVariable Integer maNguoiDung) {
        return ResponseEntity.ok(userService.getUserProfileById(maNguoiDung));
    }

    @GetMapping("/by-account/{maTaiKhoan}")
    public ResponseEntity<UserProfileResponseDto> getUserByMaTaiKhoan(@PathVariable Integer maTaiKhoan) {
        return ResponseEntity.ok(userService.getUserProfileByTaiKhoanId(maTaiKhoan));
    }

    @PutMapping(value = "/{maNguoiDung}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserProfileResponseDto> updateUserProfile(
            @PathVariable Integer maNguoiDung, @RequestBody UpdateUserProfileRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        return ResponseEntity.ok(userService.updateUserProfile(maNguoiDung, request));
    }

    @PutMapping(value = "/{maNguoiDung}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponseDto> updateUserAvatar(
            @PathVariable Integer maNguoiDung, @RequestPart("avatar") MultipartFile avatar) {
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("avatar is required");
        }
        try {
            return ResponseEntity.ok(userService.updateUserAvatar(maNguoiDung, avatar));
        } catch (IOException ex) {
            throw new IllegalStateException("Khong the upload avatar", ex);
        }
    }
}
