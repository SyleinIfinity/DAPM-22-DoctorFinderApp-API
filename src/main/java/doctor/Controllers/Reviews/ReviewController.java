package doctor.Controllers.Reviews;

import doctor.Models.DTOs.Reviews.Requests.CreateReviewRequestDto;
import doctor.Models.DTOs.Reviews.Responses.DoctorRatingSummaryResponseDto;
import doctor.Models.DTOs.Reviews.Responses.ReviewResponseDto;
import doctor.Services.Interfaces.Reviews.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(@RequestBody CreateReviewRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        ReviewResponseDto created = reviewService.createReview(request);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/doctors/{maBacSi}")
    public ResponseEntity<ReviewResponseDto> createDoctorReview(
            @PathVariable Integer maBacSi,
            @RequestBody CreateReviewRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        CreateReviewRequestDto normalized =
                new CreateReviewRequestDto(request.maNguoiDung(), maBacSi, request.soSao(), request.noiDung());
        ReviewResponseDto created = reviewService.createReview(normalized);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/doctors/{maBacSi}")
    public ResponseEntity<List<ReviewResponseDto>> getDoctorReviews(@PathVariable Integer maBacSi) {
        return ResponseEntity.ok(reviewService.getReviewsByDoctor(maBacSi));
    }

    @GetMapping("/doctors/{maBacSi}/summary")
    public ResponseEntity<DoctorRatingSummaryResponseDto> getRatingSummary(@PathVariable Integer maBacSi) {
        return ResponseEntity.ok(reviewService.getRatingSummary(maBacSi));
    }
}
