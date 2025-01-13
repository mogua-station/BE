package com.fesi6.team1.study_group.domain.user.dto;

import com.fesi6.team1.study_group.domain.user.entity.LoginType;
import com.fesi6.team1.study_group.domain.user.entity.UserTag;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateProfileRequestDTO {

    private final String nickname;
    private final String bio;
    private final List<String> userTagList;

    public UpdateProfileRequestDTO(String nickname,String bio, List<String> userTagList) {

        this.nickname = nickname;
        this.bio = bio;
        this.userTagList = userTagList;
    }
}