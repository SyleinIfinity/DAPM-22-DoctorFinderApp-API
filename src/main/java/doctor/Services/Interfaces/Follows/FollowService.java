package doctor.Services.Interfaces.Follows;

import doctor.Models.DTOs.Follows.Responses.FollowActionResponseDto;
import doctor.Models.DTOs.Follows.Responses.FollowedDoctorResponseDto;
import java.util.List;

public interface FollowService {
    List<FollowedDoctorResponseDto> getFollowedDoctors(Integer maNguoiDung);

    FollowActionResponseDto followDoctor(Integer maNguoiDung, Integer maBacSi);

    FollowActionResponseDto unfollowDoctor(Integer maNguoiDung, Integer maBacSi);
}

