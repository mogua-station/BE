package com.fesi6.team1.study_group.domain.review.dto;

import com.fesi6.team1.study_group.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserWrittenReviewResponseDTO {

    private Long userId;
    private String nickname;
    private String profileImg;
    private int rating;
    private Long meetupId;
    private String title;
    private String content;
    private LocalDateTime meetingEndDate;
    private String thumbnail;
    private LocalDateTime reviewDate;

    public UserWrittenReviewResponseDTO(Review review) {
        userId = review.getUser().getId();
        nickname = review.getUser().getNickname();
        profileImg = review.getUser().getProfileImg();
        rating = review.getRating();
        meetupId = review.getMeetup().getId();
        title = review.getMeetup().getTitle();
        content = review.getMeetup().getContent();
        meetingEndDate = review.getMeetup().getMeetingEndDate();
        thumbnail = review.getMeetup().getThumbnail();
        reviewDate = review.getCreatedAt();
    }

}
