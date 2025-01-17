package com.fesi6.team1.study_group.domain.review.repository;

import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByMeetupId(Long meetupId, Pageable pageable);
}
