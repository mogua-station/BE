package com.fesi6.team1.study_group.domain.user.controller;

import com.fesi6.team1.study_group.domain.meetup.dto.UserCreateMeetupResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.user.dto.WishlistMeetupResponseDTO;
import com.fesi6.team1.study_group.domain.user.dto.WishlistMeetupResponseDTOList;
import com.fesi6.team1.study_group.domain.user.service.UserFavoriteService;
import com.fesi6.team1.study_group.global.common.response.ApiResponse;
import com.fesi6.team1.study_group.global.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    /**
     *
     * 찜하기
     *
     */
    @PostMapping("/{meetupId}")
    public ResponseEntity<?> requestWishlist(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable Long meetupId) {
        Long userId = userDetails.getUserId();
        userFavoriteService.requestWishlist(meetupId, userId);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("찜 성공"));
    }

    /**
     *
     * 찜 삭제
     *
     */
    @DeleteMapping("/{meetupId}")
    public ResponseEntity<ApiResponse<?>> deleteWishlist(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @PathVariable Long meetupId) {

        Long userId = userDetails.getUserId();
        userFavoriteService.deleteWishlist(meetupId, userId);
        return ResponseEntity.ok(ApiResponse.successWithMessage("찜 삭제 성공"));
    }

    /**
     *
     * 찜 조회
     *
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<WishlistMeetupResponseDTO>>> getWishlist(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        WishlistMeetupResponseDTOList wishlistMeetupResponseDTOList = userFavoriteService.getUserWishlist(userId,page,limit);
        ApiResponse<List<WishlistMeetupResponseDTO>> response = ApiResponse.successResponse(
                wishlistMeetupResponseDTOList.getUserWishlist(), wishlistMeetupResponseDTOList.getAdditionalData()
        );
        return ResponseEntity.ok().body(response);
    }
}
