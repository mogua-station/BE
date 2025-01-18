package com.fesi6.team1.study_group.domain.user.entity;

import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_favorites")
public class UserFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 찜 정보 ID (자동 증가)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 찜한 사용자 (User 엔티티와 관계 맺음)

    @ManyToOne
    @JoinColumn(name = "meetup_id", nullable = false)
    private Meetup meetup;  // 찜한 모임 (Meetup 엔티티와 관계 맺음)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 찜한 날짜

    @Column(nullable = false)
    private LocalDateTime updatedAt;  // 수정된 날짜

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;  // 생성 시 현재 시간 설정
        this.updatedAt = now;  // 생성 시 현재 시간 설정
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();  // 수정 시 현재 시간 설정
    }


    @Builder
    public UserFavorite(User user, Meetup meetup) {
        this.user = user;
        this.meetup = meetup;
    }
}
