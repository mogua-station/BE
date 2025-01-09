package com.fesi6.team1.study_group.domain.user.dto;

import com.fesi6.team1.study_group.domain.user.entity.LoginType;
import com.fesi6.team1.study_group.domain.user.entity.UserTag;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateProfileRequestDTO {


    private final String nickname;
    private final String profileImg;
    private final String bio;
    private final List<UserTag> userTagList;


    public UpdateProfileRequestDTO(String nickname, String profileImg, String bio, List<UserTag> userTagList) {

        this.nickname = nickname;
        this.profileImg = profileImg;
        this.bio = bio;
        this.userTagList = userTagList;
    }
}
