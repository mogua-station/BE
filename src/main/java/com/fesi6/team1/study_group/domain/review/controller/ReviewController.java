package com.fesi6.team1.study_group.domain.review.controller;

import com.fesi6.team1.study_group.domain.review.dto.CreateReviewRequestDTO;
import com.fesi6.team1.study_group.domain.review.service.ReviewService;
import com.fesi6.team1.study_group.global.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    @PostMapping("/reviews/{id}")
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long meetupId,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart CreateReviewRequestDTO request) throws IOException {

        Long userId = userDetails.getUserId();
        reviewService.saveReview(image, request, userId, meetupId);
        return ResponseEntity.ok().body("Review created successfully");
    }


}
