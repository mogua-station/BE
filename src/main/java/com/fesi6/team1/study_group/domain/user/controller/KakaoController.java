package com.fesi6.team1.study_group.domain.user.controller;

import com.fesi6.team1.study_group.domain.user.dto.KakaoUserInfoDto;
import com.fesi6.team1.study_group.domain.user.service.KakaoService;
import com.fesi6.team1.study_group.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;
    private final UserService userService;

    @GetMapping("/user/kakao/callback")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code) {

        String kakaoToken = kakaoService.getKakaoToken(code);
        KakaoUserInfoDto kakaoUserInfoDto = kakaoService.getKakaoUserInfo(kakaoToken);

        String jwtToken = userService.save(kakaoUserInfoDto);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + jwtToken)
                .body("Login successful");
    }
}
