package com.fesi6.team1.study_group.domain.user.controller;

import com.fesi6.team1.study_group.domain.meetup.dto.MeetupListResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.MeetupResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
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
import java.util.Map;

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
    public ResponseEntity<ApiResponse<?>> kakaoLogin(@RequestParam String code) {

        String kakaoToken = kakaoService.getKakaoToken(code);
        KakaoUserInfoDTO kakaoUserInfoDto = kakaoService.getKakaoUserInfo(kakaoToken);
        String jwtToken = userService.kakaoSave(kakaoUserInfoDto);

        Map<String, Object> userData = Map.of(
                "email", kakaoUserInfoDto.getEmail(),
                "name", kakaoUserInfoDto.getNickname()
        );

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + jwtToken)  // access token 헤더에 추가
                .body(ApiResponse.successWithDataAndMessage(Map.of("user", userData), "Login successful"));
    }

    /**
     *
     * 커스텀 회원가입
     *
     **/
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> sign(@RequestBody UserSignRequestDTO request) throws IOException {

        Map<String, Object> responseData = userService.customSave(request);
        String jwtToken = (String) responseData.get("jwtToken");
        Map<String, Object> wrappedUserData = Map.of(
                "user", responseData.get("user")  // "user" 데이터 감싸기
        );

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + jwtToken)  // 헤더에 JWT 추가
                .body(ApiResponse.successWithDataAndMessage(wrappedUserData, "Sign-up successful"));
    }


    /**
     *
     * 커스텀 로그인
     *
     **/
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody UserLoginRequestDTO request) throws IOException {
        // 로그인 서비스 호출 (JWT 토큰과 사용자 정보 반환)
        UserLoginResponseDTO userResponse = userService.customLogin(request);

        // 사용자 정보를 "user"로 감싼 Map 생성 (JWT 제거)
        Map<String, Object> responseData = Map.of(
                "user", Map.of(
                        "email", userResponse.getEmail(),
                        "name", userResponse.getName()
                )
        );
        // JWT 포함하여 응답 반환
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + userResponse.getJwtToken()) // JWT는 헤더에만 포함
                .body(ApiResponse.successResponse(responseData)); // 사용자 정보만 본문에 반환
    }


    /**
     *
     * 내 프로필 수정
     *
     **/
    @PatchMapping("/profile/me")
    public ResponseEntity<ApiResponse<?>> updateMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                               @RequestPart(required = false) MultipartFile image,
                                                               @RequestPart UpdateProfileRequestDTO request) throws IOException {
        Long userId = userDetails.getUserId();
        userService.updateMyProfile(userId, image, request);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Profile update successful"));
    }

    /**
     *
     * 비밀번호 변경
     *
     **/
    @PostMapping("/profile/password")
    public ResponseEntity<ApiResponse<?>> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody UpdatePasswordRequestDTO request) throws IOException {
        Long userId = userDetails.getUserId();
        userService.updatePassword(userId, request);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Password update successful"));
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
        UserProfileResponseDTO userProfile = userService.findUserProfile(id, myId);
        return ResponseEntity.ok().body(ApiResponse.successResponse(userProfile));
    }

    /**
     *
     * 유저 모임 조회
     *
     **/
    @GetMapping("/{profileUserId}/meetups/participating/{type}")
    public ResponseEntity<ApiResponse<List<MeetupResponseDTO>>> getUserMeetups(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("profileUserId") Long profileUserId,
            @PathVariable("type") MeetingType type,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit ) {

        Long userId = userDetails.getUserId();
        MeetupListResponseDTO meetupListResponseDTO = userService.getUserMeetupsByType(profileUserId, type, page, limit);

        ApiResponse<List<MeetupResponseDTO>> response = ApiResponse.successResponse(
                meetupListResponseDTO.getMeetups(), meetupListResponseDTO.getAdditionalData()
        );
        return ResponseEntity.ok().body(response);
    }


    /**
     *
     * 유저 리뷰 조회
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
