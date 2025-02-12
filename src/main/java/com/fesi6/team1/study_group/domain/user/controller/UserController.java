package com.fesi6.team1.study_group.domain.user.controller;

import com.fesi6.team1.study_group.domain.meetup.dto.*;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupService;
import com.fesi6.team1.study_group.domain.review.dto.UserWrittenReviewResponseDTO;
import com.fesi6.team1.study_group.domain.review.dto.UserWrittenReviewResponseDTOList;
import com.fesi6.team1.study_group.domain.review.service.ReviewService;
import com.fesi6.team1.study_group.domain.user.dto.*;
import com.fesi6.team1.study_group.domain.user.entity.User;
import com.fesi6.team1.study_group.domain.user.service.KakaoService;
import com.fesi6.team1.study_group.domain.user.service.UserService;
import com.fesi6.team1.study_group.global.common.response.ApiResponse;
import com.fesi6.team1.study_group.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
@CrossOrigin(origins = "https://mogua.vercel.app", allowCredentials = "true")
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final MeetupService meetupService;
    private final KakaoService kakaoService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     *
     * Access Token 재발급 (Refresh Token 사용)
     *
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> reissueAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.errorResponse("Refresh token is missing"));
        }
        try {
            ResponseCookie newAccessTokenCookie = userService.reissueAccessToken(refreshToken);

            // 추가 데이터로 새로운 Access Token 정보 전달
            Map<String, Object> additionalData = Map.of(
                    "newAccessToken", newAccessTokenCookie.getValue(),
                    "message", "Access token reissued successfully"
            );
            return ResponseEntity.ok()
                    .header("Set-Cookie", newAccessTokenCookie.toString())
                    .body(ApiResponse.successResponse(null, additionalData));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.errorResponse(e.getMessage()));
        }
    }

    /**
     *
     * 토큰 유효성 확인
     *
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyToken(@CookieValue(value = "accessToken", required = false) String accessToken) {
        if (accessToken == null || !jwtTokenProvider.validateAccessToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.errorResponse("Invalid or expired access token"));
        }
        return ResponseEntity.ok(ApiResponse.successWithMessage("Access token is valid"));
    }

    /**
     *
     * 카카오 로그인 & 회원가입
     *
     **/
    @PostMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<?>> kakaoLogin(@RequestBody Map<String, String> requestBody) {
        String code = requestBody.get("code");  // 🔹 Request Body에서 code 추출
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.errorResponse("Authorization code is missing"));
        }
        String kakaoToken = kakaoService.getKakaoToken(code);
        KakaoUserInfoDTO kakaoUserInfoDto = kakaoService.getKakaoUserInfo(kakaoToken);
        User user = userService.kakaoSave(kakaoUserInfoDto);

        ResponseCookie accessTokenCookie = userService.createAccessTokenCookie(user.getId());
        ResponseCookie refreshTokenCookie = userService.createRefreshTokenCookie(user.getId());

        Map<String, Object> userData = Map.of(
                "userId",user.getId(),
                "email", kakaoUserInfoDto.getEmail(),
                "name", kakaoUserInfoDto.getNickname(),
                "profileImg",user.getProfileImg()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.successWithDataAndMessage(Map.of("user", userData), "Login successful"));
    }

    /**
     *
     * 커스텀 회원가입
     *
     **/
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> signUp(@RequestBody UserSignRequestDTO request) throws IOException {

        Map<String, Object> responseData = userService.customSave(request);
        Long userId = (Long) responseData.get("userId");

        ResponseCookie accessTokenCookie = userService.createAccessTokenCookie(userId);
        ResponseCookie refreshTokenCookie = userService.createRefreshTokenCookie(userId);

        Map<String, Object> wrappedUserData = Map.of(
                "user", responseData.get("user")
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.successWithDataAndMessage(wrappedUserData, "Sign-up successful"));
    }

    /**
     *
     * 커스텀 로그인 (Access Token + Refresh Token 발급)
     *
     **/
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody UserLoginRequestDTO request) throws IOException {
        // 로그인 서비스 호출 (JWT 토큰과 사용자 정보 반환)
        UserLoginResponseDTO userResponse = userService.customLogin(request);

        // Access Token & Refresh Token 생성
        ResponseCookie accessTokenCookie = userService.createAccessTokenCookie(userResponse.getId());
        ResponseCookie refreshTokenCookie = userService.createRefreshTokenCookie(userResponse.getId());

        // 사용자 정보를 "user"로 감싼 Map 생성
        Map<String, Object> responseData = Map.of(
                "user", Map.of(
                        "userId", userResponse.getId(),
                        "email", userResponse.getEmail(),
                        "name", userResponse.getName(),
                        "profileImg", userResponse.getProfileImg()
                )
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.successResponse(responseData));
    }

    /**
     *
     * 내 프로필 수정
     *
     **/
    @PatchMapping("/profile/me")
    public ResponseEntity<ApiResponse<?>> updateMyProfile(
            @AuthenticationPrincipal Long userId,
            @RequestPart(value ="image", required = false) MultipartFile image,
            @RequestPart("request") UpdateProfileRequestDTO request) throws IOException {
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