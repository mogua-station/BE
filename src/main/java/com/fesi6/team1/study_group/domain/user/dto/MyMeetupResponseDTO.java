package com.fesi6.team1.study_group.domain.user.dto;

import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyMeetupResponseDTO {

    private final String title;
    private final String location;
    private final int currentParticipants;
    private final LocalDateTime recruitmentStartDate;
    private final LocalDateTime recruitmentEndDate;
    private final LocalDateTime meetingStartDate;
    private final LocalDateTime meetingEndDate;
    private final String thumbnail;
    private final boolean isFavorite;

    public MyMeetupResponseDTO(Meetup meetup, boolean isFavorite) {
        this.title = meetup.getTitle();
        this.location = meetup.getLocation();
        this.currentParticipants = meetup.getMeetupUsers().size(); // 현재 참가자 수
        this.recruitmentStartDate = meetup.getCreatedAt();
        this.recruitmentEndDate = meetup.getRecruitmentEndDate();
        this.meetingStartDate = meetup.getMeetingStartDate();
        this.meetingEndDate = meetup.getMeetingEndDate();
        this.thumbnail = meetup.getThumbnail();
        this.isFavorite = isFavorite;
    }

}