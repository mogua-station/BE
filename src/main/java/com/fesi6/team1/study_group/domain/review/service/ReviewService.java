package com.fesi6.team1.study_group.domain.review.service;

import com.fesi6.team1.study_group.domain.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

//    public List<ReviewResponseDTO> getEligibleReviews(Long userId, String type) {
//        // 리뷰 작성 가능한 모임 목록 조회 로직
//        // 예시: userId와 type에 맞는 모임에서 작성 가능한 리뷰 목록을 반환
//    }
//
//    public List<ReviewResponseDTO> getWrittenReviews(Long userId) {
//        // 사용자가 작성한 리뷰 목록 조회 로직
//        // 예시: userId로 작성한 리뷰 목록을 반환
//    }
}