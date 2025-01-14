package com.fesi6.team1.study_group.domain.meetup.repository;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetupUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetupUserRepository extends JpaRepository<MeetupUser, Long> {
    // 특정 모임에 참가한 사용자를 찾는 메서드
    Optional<MeetupUser> findByMeetupIdAndUserId(Long meetupId, Long userId);

    // 이미 참가한 모임에서 해당 사용자를 삭제하는 메서드
    void delete(MeetupUser meetupUser);
}
