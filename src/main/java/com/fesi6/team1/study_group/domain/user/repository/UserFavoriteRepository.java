package com.fesi6.team1.study_group.domain.user.repository;

import com.fesi6.team1.study_group.domain.user.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    Optional<UserFavorite> findByUserIdAndMeetupId(Long userId, Long meetupId);
}

