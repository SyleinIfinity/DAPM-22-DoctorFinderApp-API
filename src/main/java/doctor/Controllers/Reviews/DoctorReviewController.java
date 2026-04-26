package doctor.Controllers.Reviews;

import doctor.Models.DTOs.Reviews.Responses.DoctorRatingSummaryResponseDto;
import doctor.Models.DTOs.Reviews.Responses.ReviewResponseDto;
import doctor.Services.Interfaces.Reviews.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{maBacSi}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getDoctorReviews(@PathVariable Integer maBacSi) {
        return ResponseEntity.ok(reviewService.getReviewsByDoctor(maBacSi));
    }

    @GetMapping("/{maBacSi}/rating-summary")
    public ResponseEntity<DoctorRatingSummaryResponseDto> getRatingSummary(@PathVariable Integer maBacSi) {
        return ResponseEntity.ok(reviewService.getRatingSummary(maBacSi));
    }
}

