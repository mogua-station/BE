package com.fesi6.team1.study_group.domain.review.service;

import com.fesi6.team1.study_group.domain.meetup.dto.MeetupListResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.MeetupResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetupUser;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupService;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupUserService;
import com.fesi6.team1.study_group.domain.review.dto.CreateReviewRequestDTO;
import com.fesi6.team1.study_group.domain.review.dto.ReviewResponseDTO;
import com.fesi6.team1.study_group.domain.review.dto.ReviewResponseDTOList;
import com.fesi6.team1.study_group.domain.review.dto.UpdateReviewRequestDTO;
import com.fesi6.team1.study_group.domain.review.entity.Review;
import com.fesi6.team1.study_group.domain.review.repository.ReviewRepository;
import com.fesi6.team1.study_group.domain.user.entity.User;
import com.fesi6.team1.study_group.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MeetupService meetupService;
    private final MeetupUserService meetupUserService;
    private final UserService userService;

    public void saveReview(CreateReviewRequestDTO request, Long userId) {

        Long meetupId = request.getMeetupId();
        MeetupUser meetupUser = meetupUserService.findByMeetupIdAndUserId(meetupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임에 대한 사용자가 존재하지 않습니다."));
        if (meetupUser.isHasReview()) {
            throw new IllegalStateException("이미 리뷰를 작성하셨습니다.");
        }

        Meetup meetup = meetupService.findMeetupById(meetupId);
        User user = userService.findById(userId);

        Review review = Review.builder()
                .meetup(meetup)
                .user(user)
                .content(request.getContent())
                .rating(request.getRating())
                .build();
        reviewRepository.save(review);

        meetupUser.setHasReview(true);
        meetupUserService.save(meetupUser);
    }

    public void updateReview(UpdateReviewRequestDTO request, Long reviewId, Long userId) {

        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));;
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("리뷰를 수정할 권한이 없습니다");
        }

        review.setContent(request.getContent());
        review.setRating(request.getRating());
        reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId, Long userId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("리뷰를 삭제할 권한이 없습니다.");
        }
        reviewRepository.delete(review);

        MeetupUser meetupUser = meetupUserService.findByMeetupIdAndUserId(
                        review.getMeetup().getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임에 대한 사용자가 존재하지 않습니다."));
        meetupUser.setHasReview(false);
        meetupUserService.save(meetupUser);
    }

    public ReviewResponseDTOList getReviewsByMeetupId(Long meetupId, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.asc("createdAt"))); // createdAt 컬럼을 기준으로 정렬
        Page<Review> reviewPage = reviewRepository.findByMeetupId(meetupId, pageable);

        List<ReviewResponseDTO> reviewResponseDTOList = reviewPage.getContent().stream()
                .map(review -> new ReviewResponseDTO(review))
                .collect(Collectors.toList());

        Integer nextPage = reviewPage.hasNext() ? page + 1 : -1;
        boolean isLast = reviewPage.isLast();
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("nextPage", nextPage);
        additionalData.put("isLast", isLast);

        return new ReviewResponseDTOList(reviewResponseDTOList, additionalData);
    }
}