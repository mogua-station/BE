package com.fesi6.team1.study_group.domain.review.dto;

import com.fesi6.team1.study_group.domain.review.entity.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateReviewRequestDTO {

    private final int rating;
    private final String content;
    private final String userNickname;
    private final LocalDateTime reviewDate;

    public CreateReviewRequestDTO(Review review) {
        this.content = review.getContent();
        this.rating = review.getRating();
        this.reviewDate = review.getCreatedAt();
        this.userNickname = review.getUser().getNickname();
    }

}
