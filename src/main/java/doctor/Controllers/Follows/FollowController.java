package doctor.Controllers.Follows;

import doctor.Models.DTOs.Follows.Responses.FollowActionResponseDto;
import doctor.Models.DTOs.Follows.Responses.FollowedDoctorResponseDto;
import doctor.Services.Interfaces.Follows.FollowService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @GetMapping
    public ResponseEntity<List<FollowedDoctorResponseDto>> getFollows(@RequestParam Integer maNguoiDung) {
        return ResponseEntity.ok(followService.getFollowedDoctors(maNguoiDung));
    }

    @PostMapping("/{maBacSi}")
    public ResponseEntity<FollowActionResponseDto> followDoctor(
            @RequestParam Integer maNguoiDung, @PathVariable Integer maBacSi) {
        FollowActionResponseDto result = followService.followDoctor(maNguoiDung, maBacSi);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{maBacSi}")
    public ResponseEntity<FollowActionResponseDto> unfollowDoctor(
            @RequestParam Integer maNguoiDung, @PathVariable Integer maBacSi) {
        return ResponseEntity.ok(followService.unfollowDoctor(maNguoiDung, maBacSi));
    }
}
