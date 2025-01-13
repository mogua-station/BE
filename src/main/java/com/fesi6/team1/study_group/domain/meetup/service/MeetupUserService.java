package com.fesi6.team1.study_group.domain.meetup.service;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetupUser;
import com.fesi6.team1.study_group.domain.meetup.repository.MeetupUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetupUserService {

    private final MeetupUserRepository meetupUserRepository;

    public void save(MeetupUser meetupUser) {
        meetupUserRepository.save(meetupUser);
    }

}
