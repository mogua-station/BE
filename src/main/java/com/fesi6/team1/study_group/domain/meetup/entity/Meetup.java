package com.fesi6.team1.study_group.domain.meetup.entity;

import com.fesi6.team1.study_group.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import static com.fesi6.team1.study_group.domain.meetup.entity.MeetupStatus.RECRUITING;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "meetups")
public class Meetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 모임 ID (자동 증가)

    @Column(nullable = false)
    private String title;  // 모임 제목

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false) // 수정 불가
    private MeetingType meetingType; // 모임 종류

    @Column(nullable = true)
    private MeetupLocation location;  // 모임 지역

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // 본문

    @Column(nullable = false)
    private LocalDateTime recruitmentEndDate;  // 모임 모집 종료일

    @Column(nullable = false)
    private LocalDateTime meetingStartDate;  // 모임 시작일

    @Column(nullable = false)
    private LocalDateTime meetingEndDate;  // 모임 종료일

    private String thumbnail;  // 모임 썸네일

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;  // 주최자 ID (User 엔티티와 관계 맺음)

    @Column(nullable = false)
    private int maxParticipants;  // 모집 인원

    @Column(nullable = false)
    private int minParticipants;  // 최소 인원

    @Column(nullable = false)
    private boolean isOnline;  // 온/오프라인 여부

    @OneToMany(mappedBy = "meetup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetupUser> meetupUsers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetupStatus status = RECRUITING;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Meetup(String title, MeetingType meetingType, MeetupLocation location, String content, LocalDateTime recruitmentEndDate,
                  LocalDateTime meetingStartDate, LocalDateTime meetingEndDate, String thumbnail,
                  User host, int maxParticipants, int minParticipants, boolean isOnline) {
        this.title = title;
        this.meetingType = meetingType;
        this.location = location;
        this.content = content;
        this.recruitmentEndDate = recruitmentEndDate;
        this.meetingStartDate = meetingStartDate;
        this.meetingEndDate = meetingEndDate;
        this.thumbnail = thumbnail;
        this.host = host;
        this.maxParticipants = maxParticipants;
        this.minParticipants = minParticipants;
        this.isOnline = isOnline;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.isOnline) {
            this.location = null;
        }
    }
}