package com.fesi6.team1.study_group.domain.user.controller;

import com.fesi6.team1.study_group.domain.user.dto.*;
import com.fesi6.team1.study_group.domain.user.service.KakaoService;
import com.fesi6.team1.study_group.domain.user.service.UserService;
import com.fesi6.team1.study_group.global.common.response.ApiResponse;
import com.fesi6.team1.study_group.global.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.fesi6.team1.study_group.global.common.response.ApiResponse.successResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final KakaoService kakaoService;

    /**
     *
     * 카카오 로그인 & 회원가입
     *
     **/
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code) {

        String kakaoToken = kakaoService.getKakaoToken(code);
        KakaoUserInfoDTO kakaoUserInfoDto = kakaoService.getKakaoUserInfo(kakaoToken);

        // 유저 저장 및 JWT 생성
        String jwtToken = userService.kakaoSave(kakaoUserInfoDto);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + jwtToken)
                .body("Login successful");
    }

    /**
     *
     * 커스텀 회원가입
     *
     **/
    @PostMapping("/sign-up")
    public ResponseEntity<?> sign(@RequestBody UserSignRequestDTO request) throws IOException {

        String jwtToken = userService.customSave(request);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + jwtToken)
                .body("Sign successful");
    }

    /**
     *
     * 커스텀 로그인
     *
     **/
    @PostMapping("/sign-in")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDTO request) throws IOException {

        String jwtToken = userService.customLogin(request);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + jwtToken)
                .body("Login successful");
    }


    /**
     *
     * 내 프로필 수정
     *
     **/
    @PatchMapping("/profile/me")
    public ResponseEntity<?> updateMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestPart(required = false) MultipartFile image,
                                             @RequestPart UpdateProfileRequestDTO request) throws IOException {
        Long userId = userDetails.getUserId();
        userService.updateMyProfile(userId, image, request);
        return ResponseEntity.ok().body("Profile update successful");
    }

    /**
     *
     * 비밀번호 변경
     *
     **/
    @PostMapping("/profile/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody UpdatePasswordRequestDTO request) throws IOException {
        Long userId = userDetails.getUserId();
        userService.updatePassword(userId, request);
        return ResponseEntity.ok().body("Password update successful");
    }

    /**
     *
     * 유저 프로필 조회
     *
     **/
    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> findUserProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long myId = userDetails.getUserId();
        return ResponseEntity.ok().body(successResponse(userService.findUserProfile(id, myId)));
    }

    /**
     *
     * 내 모임 조회
     *
     */
    @GetMapping("/meetup/participating/{type}")
    public ResponseEntity<ApiResponse<List<MyMeetupResponseDTO>>> getMyMeetups(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("type") String type) {

        Long userId = userDetails.getUserId();

        // 모임 또는 스터디 조회
        List<MyMeetupResponseDTO> meetups = userService.getMyMeetupsByType(userId, type);

        return ResponseEntity.ok().body(successResponse(meetups));
    }

    /**
     *
     * 내 리뷰 조회
     *
     */
//    @GetMapping("/reviews/{type}/{status}")
//    public ResponseEntity<List<MyReviewResponseDTO>> getMyReviews(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @PathVariable("type") String type,  // 'study' 또는 'tutoring'
//            @PathVariable("status") String status) { // 'eligible' 또는 'written'
//
//        Long userId = userDetails.getUserId();
//
//        // 'status'에 따른 분기 처리
//        if ("eligible".equalsIgnoreCase(status)) {
//            // 리뷰 작성 가능한 모임 목록 조회
//            return ResponseEntity.ok().body(reviewService.getEligibleReviews(userId, type));
//        } else if ("written".equalsIgnoreCase(status)) {
//            // 사용자가 작성한 리뷰 목록 조회
//            return ResponseEntity.ok().body(reviewService.getWrittenReviews(userId));
//        } else {
//            throw new IllegalArgumentException("Invalid status");
//        }
//    }
}
