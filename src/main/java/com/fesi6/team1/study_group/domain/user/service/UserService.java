package com.fesi6.team1.study_group.domain.user.service;

import com.fesi6.team1.study_group.domain.user.dto.KakaoUserInfoDto;
import com.fesi6.team1.study_group.domain.user.entity.User;
import com.fesi6.team1.study_group.domain.user.repository.UserRepository;
import com.fesi6.team1.study_group.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public String save(KakaoUserInfoDto kakaoUserInfoDto) {

        // 카카오 ID로 이미 회원이 존재하는지 확인
        User user = userRepository.findBySocialId(String.valueOf(kakaoUserInfoDto.getSocialId()))
                .orElseGet(() -> {
                    // 회원이 존재하지 않으면 새로 생성
                    User newUser = User.builder()
                            .socialId(String.valueOf(kakaoUserInfoDto.getSocialId()))
                            .email(kakaoUserInfoDto.getEmail())
                            .nickname(kakaoUserInfoDto.getNickname())
                            .profileImg(kakaoUserInfoDto.getProfileImage())
                            .build();

                    // 새로운 사용자 저장
                    return userRepository.save(newUser);
                });

        // JWT 토큰 발급
        return jwtTokenProvider.createToken(user.getId()); // socialId로 JWT 생성
    }
}