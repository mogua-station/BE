package com.fesi6.team1.study_group.domain.meetup.dto;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MeetupResponseDTO {

    private final String title;
    private final MeetingType meetingType;
    private final String location;
    private final String content;
    private final LocalDateTime recruitmentEndDate;
    private final LocalDateTime meetingStartDate;
    private final LocalDateTime meetingEndDate;
    private final int maxParticipants;
    private final int minParticipants;
    private final boolean isOnline;
    private final String thumbnail;
    private final String hostNickname;
    private final Long hostId;

    public MeetupResponseDTO(Meetup meetup) {
        this.title = meetup.getTitle();
        this.meetingType = meetup.getMeetingType();
        this.location = meetup.getLocation();
        this.content = meetup.getContent();
        this.recruitmentEndDate = meetup.getRecruitmentEndDate();
        this.meetingStartDate = meetup.getMeetingStartDate();
        this.meetingEndDate = meetup.getMeetingEndDate();
        this.maxParticipants = meetup.getMaxParticipants();
        this.minParticipants = meetup.getMinParticipants();
        this.isOnline = meetup.isOnline();
        this.thumbnail = meetup.getThumbnail();
        this.hostNickname = meetup.getHost().getNickname();
        this.hostId = meetup.getHost().getId();
    }
}
