package com.fesi6.team1.study_group.domain.meetup.repository;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetupUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetupUserRepository extends JpaRepository<MeetupUser, Long> {
}
