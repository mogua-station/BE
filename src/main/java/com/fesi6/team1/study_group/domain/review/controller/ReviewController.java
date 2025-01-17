package com.fesi6.team1.study_group.domain.review.controller;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.review.dto.CreateReviewRequestDTO;
import com.fesi6.team1.study_group.domain.review.dto.ReviewResponseDTO;
import com.fesi6.team1.study_group.domain.review.dto.ReviewResponseDTOList;
import com.fesi6.team1.study_group.domain.review.dto.UpdateReviewRequestDTO;
import com.fesi6.team1.study_group.domain.review.service.ReviewService;
import com.fesi6.team1.study_group.domain.meetup.dto.UserEligibleReviewResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.UserEligibleReviewResponseDTOList;
import com.fesi6.team1.study_group.domain.review.dto.UserWrittenReviewResponseDTO;
import com.fesi6.team1.study_group.domain.review.dto.UserWrittenReviewResponseDTOList;
import com.fesi6.team1.study_group.global.common.response.ApiResponse;
import com.fesi6.team1.study_group.global.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     *
     * 리뷰 작성
     *
     */
    @PostMapping
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateReviewRequestDTO request) throws IOException {

        Long userId = userDetails.getUserId();
        reviewService.saveReview(request, userId);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Review created successfully"));
    }

    /**
     *
     * 리뷰 조회
     *
     */
    @GetMapping("/{meetupId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByMeetupId(
            @PathVariable Long meetupId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "3") Integer limit) {

        ReviewResponseDTOList reviewResponseDTOList = reviewService.getReviewsByMeetupId(meetupId, page, limit);

        ApiResponse<List<ReviewResponseDTO>> response = ApiResponse.successResponse(
                reviewResponseDTOList.getReview(), reviewResponseDTOList.getAdditionalData()
        );
        return ResponseEntity.ok().body(response);
    }

    /**
     *
     * 리뷰 수정
     *
     */
    @PatchMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @RequestBody UpdateReviewRequestDTO request) throws IOException {

        Long userId = userDetails.getUserId();
        reviewService.updateReview(request, reviewId, userId);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Review updated successfully"));
    }

    /**
     *
     * 리뷰 삭제
     *
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId) throws IOException {

        Long userId = userDetails.getUserId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Review deleted successfully"));
    }

}