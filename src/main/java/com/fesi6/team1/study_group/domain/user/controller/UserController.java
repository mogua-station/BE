package com.fesi6.team1.study_group.domain.user.controller;

import com.fesi6.team1.study_group.domain.meetup.dto.*;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupService;
import com.fesi6.team1.study_group.domain.review.dto.UserWrittenReviewResponseDTO;
import com.fesi6.team1.study_group.domain.review.dto.UserWrittenReviewResponseDTOList;
import com.fesi6.team1.study_group.domain.review.service.ReviewService;
import com.fesi6.team1.study_group.domain.user.dto.*;
import com.fesi6.team1.study_group.domain.user.service.KakaoService;
import com.fesi6.team1.study_group.domain.user.service.UserService;
import com.fesi6.team1.study_group.global.common.response.ApiResponse;
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
    private final ReviewService reviewService;
    private final MeetupService meetupService;
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
                        "userId",userResponse.getId(),
                        "email", userResponse.getEmail(),
                        "name", userResponse.getName(),
                        "profileImg",userResponse.getProfileImg()
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
    public ResponseEntity<ApiResponse<?>> updateMyProfile(@AuthenticationPrincipal Long userId,
                                                          @RequestPart(required = false) MultipartFile image,
                                                          @RequestPart UpdateProfileRequestDTO request) throws IOException {
        userService.updateMyProfile(userId, image, request);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Profile update successful"));
    }

    /**
     *
     * 비밀번호 변경
     *
     **/
    @PostMapping("/profile/password")
    public ResponseEntity<ApiResponse<?>> updatePassword(@AuthenticationPrincipal Long userId,
                                                         @RequestBody UpdatePasswordRequestDTO request) throws IOException {
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
            @AuthenticationPrincipal Long myId) {
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
            @AuthenticationPrincipal Long userId,
            @PathVariable("profileUserId") Long profileUserId,
            @PathVariable("type") MeetingType type,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit ) {

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
    /** 작성 가능한 리뷰 조회 **/
    @GetMapping("/{id}/reviews/{type}/eligible")
    public ResponseEntity<ApiResponse<List<UserEligibleReviewResponseDTO>>> getUserEligibleReviews(
            @PathVariable("id") Long userId,
            @PathVariable("type") MeetingType type,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit ) {

        UserEligibleReviewResponseDTOList userEligibleReviewResponseDTOList = reviewService.getUserEligibleReviewResponse(userId, type, page, limit);

        ApiResponse<List<UserEligibleReviewResponseDTO>> response = ApiResponse.successResponse(
                userEligibleReviewResponseDTOList.getEligibleReview(), userEligibleReviewResponseDTOList.getAdditionalData()
        );
        return ResponseEntity.ok().body(response);
    }
    /** 작성한 리뷰 조회 **/
    @GetMapping("/{id}/reviews/{type}/written")
    public ResponseEntity<ApiResponse<List<UserWrittenReviewResponseDTO>>> getUserWrittenReviews(
            @PathVariable("id") Long profileUserId,
            @PathVariable("type") MeetingType type,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit ) {

        UserWrittenReviewResponseDTOList userWrittenReviewResponseDTOList = reviewService.getUserWrittenReviewResponse(profileUserId, type, page, limit);

        ApiResponse<List<UserWrittenReviewResponseDTO>> response = ApiResponse.successResponse(
                userWrittenReviewResponseDTOList.getWrittenReview(), userWrittenReviewResponseDTOList.getAdditionalData()
        );
        return ResponseEntity.ok().body(response);
    }

    /**
     *
     * 만든 모임 조회
     *
     */
    @GetMapping("/{id}/meetups/created/{type}")
    public ResponseEntity<ApiResponse<List<UserCreateMeetupResponseDTO>>> getUserCreateMeetup(
            @PathVariable("id") Long userId,
            @PathVariable("type") MeetingType type,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit ) {

        UserCreateMeetupResponseDTOList userCreateMeetupResponseDTOList = meetupService.getUserCreateMeetupResponse(userId, type, page, limit);

        ApiResponse<List<UserCreateMeetupResponseDTO>> response = ApiResponse.successResponse(
                userCreateMeetupResponseDTOList.getUserCreateMeetupList(), userCreateMeetupResponseDTOList.getAdditionalData()
        );
        return ResponseEntity.ok().body(response);
    }

    /**
     *
     * 수강평 조회
     *
     */
    @GetMapping("/{id}/reviews/received")
    public ResponseEntity<ApiResponse<List<UserTutoringReviewResponseDTO>>> getStudentReviewsForMyTutoring(
            @PathVariable("id") Long userId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit ) {

        UserTutoringReviewResponseDTOList userTutoringReviewResponseDTOList = reviewService.getUserTutoringReview(userId, page, limit);
        ApiResponse<List<UserTutoringReviewResponseDTO>> response = ApiResponse.successResponse(
                userTutoringReviewResponseDTOList.getUserTutoringReview(), userTutoringReviewResponseDTOList.getAdditionalData()
        );
        return ResponseEntity.ok().body(response);
    }

}