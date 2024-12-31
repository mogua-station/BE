package com.fesi6.team1.study_group.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String socialId;

    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    private String profileImg;

    @Builder
    public User(String socialId, String nickname, String email, String profileImg) {
        this.socialId = socialId;
        this.nickname = nickname;
        this.email = email;
        this.profileImg = profileImg;
    }

}
