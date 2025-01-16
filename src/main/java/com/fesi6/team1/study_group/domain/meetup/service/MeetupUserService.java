package com.fesi6.team1.study_group.domain.meetup.service;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetupUser;
import com.fesi6.team1.study_group.domain.meetup.repository.MeetupUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetupUserService {

    private final MeetupUserRepository meetupUserRepository;

    public void save(MeetupUser meetupUser) {
        meetupUserRepository.save(meetupUser);
    }

    public void leaveMeetup(Long meetupId, Long userId) {
        MeetupUser meetupUser = meetupUserRepository.findByMeetupIdAndUserId(meetupId, userId)
                .orElseThrow(() -> new IllegalStateException("모임에 참가하지 않았습니다"));
        meetupUserRepository.delete(meetupUser);
    }

    public Page<Meetup> findByUserIdAndType(Long userId, MeetingType meetingType, Pageable pageable) {
        return meetupUserRepository.findByUserIdAndType(userId, meetingType, pageable);
    }

}
