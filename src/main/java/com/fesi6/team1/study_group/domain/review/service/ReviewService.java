package com.fesi6.team1.study_group.domain.review.service;

import com.fesi6.team1.study_group.domain.meetup.service.MeetupService;
import com.fesi6.team1.study_group.domain.review.dto.CreateReviewRequestDTO;
import com.fesi6.team1.study_group.domain.review.entity.Review;
import com.fesi6.team1.study_group.domain.review.repository.ReviewRepository;
import com.fesi6.team1.study_group.domain.user.entity.User;
import com.fesi6.team1.study_group.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MeetupService meetupService;
    private final UserService userService;

    public void saveReview(MultipartFile image, CreateReviewRequestDTO request, Long userId, Long meetupId) {

        Review review = Review.builder()
                .meetup(meetupService.findMeetupById(meetupId))
                .user(userService.findById(userId))
                .content(request.getContent())
                .rating(request.getRating())
                .build();

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