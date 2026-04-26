package doctor.Controllers.Reviews;

import doctor.Models.DTOs.Reviews.Requests.CreateReviewRequestDto;
import doctor.Models.DTOs.Reviews.Responses.ReviewResponseDto;
import doctor.Services.Interfaces.Reviews.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
