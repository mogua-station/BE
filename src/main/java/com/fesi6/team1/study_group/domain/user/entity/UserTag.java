package com.fesi6.team1.study_group.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_tags")
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // primary key

    @ManyToOne(fetch = FetchType.LAZY)  // user_id와 매핑
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 해당 태그를 가진 사용자

    @Column(nullable = false)
    private String tag;  // 태그 이름

}
