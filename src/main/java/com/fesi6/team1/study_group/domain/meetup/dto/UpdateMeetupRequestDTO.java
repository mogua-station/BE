package com.fesi6.team1.study_group.domain.meetup.dto;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateMeetupRequestDTO {

    private final String title;
    private final String content;

    public UpdateMeetupRequestDTO(Meetup meetup) {
        this.title = meetup.getTitle();
        this.content = meetup.getContent();
    }
}
