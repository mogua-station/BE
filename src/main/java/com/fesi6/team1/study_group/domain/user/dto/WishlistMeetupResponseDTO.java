package com.fesi6.team1.study_group.domain.user.dto;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetupLocation;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetupStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class WishlistMeetupResponseDTO {
    private Long meetupId;
    private MeetingType meetingType;
    private boolean isOnline;
    private MeetupStatus meetupStatus;
    private MeetupLocation location;
    private String title;
    private String content;
    private LocalDateTime recruitmentStartDate;
    private LocalDateTime recruitmentEndDate;
    private LocalDateTime meetingStartDate;
    private LocalDateTime meetingEndDate;
    private String thumbnail;

    public WishlistMeetupResponseDTO(Meetup meetup) {
        meetupId = meetup.getId();
        meetingType = meetup.getMeetingType();
        isOnline = meetup.isOnline();
        meetupStatus = meetup.getStatus();
        location = meetup.getLocation();
        title = meetup.getTitle();
        content = meetup.getContent();
        recruitmentStartDate = meetup.getCreatedAt();
        recruitmentEndDate = meetup.getRecruitmentEndDate();
        meetingStartDate = meetup.getMeetingStartDate();
        meetingEndDate = meetup.getMeetingEndDate();
        thumbnail = meetup.getThumbnail();
    }
}
