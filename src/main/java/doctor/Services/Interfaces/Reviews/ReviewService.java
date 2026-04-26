package doctor.Services.Interfaces.Reviews;

import doctor.Models.DTOs.Reviews.Requests.CreateReviewRequestDto;
import doctor.Models.DTOs.Reviews.Responses.DoctorRatingSummaryResponseDto;
import doctor.Models.DTOs.Reviews.Responses.ReviewResponseDto;
import java.util.List;

public interface ReviewService {
    ReviewResponseDto createReview(CreateReviewRequestDto request);

    List<ReviewResponseDto> getReviewsByDoctor(Integer maBacSi);

    DoctorRatingSummaryResponseDto getRatingSummary(Integer maBacSi);
}

