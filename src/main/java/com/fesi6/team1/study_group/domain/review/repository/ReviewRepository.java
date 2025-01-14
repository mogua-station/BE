package com.fesi6.team1.study_group.domain.review.repository;

import com.fesi6.team1.study_group.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
