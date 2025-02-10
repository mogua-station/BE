package com.fesi6.team1.study_group.domain.user.repository;

import com.fesi6.team1.study_group.domain.user.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    // Optional로 데이터가 있는지 확인 (존재 여부 체크용)
    Optional<UserFavorite> findByMeetupIdAndUserId(Long meetupId, Long userId);

    // 단일 엔티티 반환 (데이터가 반드시 있어야 할 경우)
    default UserFavorite getByMeetupIdAndUserId(Long meetupId, Long userId) {
        return findByMeetupIdAndUserId(meetupId, userId)
                .orElseThrow(() -> new IllegalStateException("찜 정보를 찾을 수 없습니다."));
    }

    Page<UserFavorite> findByUserId(Long userId, Pageable pageable);

    Page<UserFavorite> findAll(Specification<UserFavorite> spec, Pageable pageable);
}