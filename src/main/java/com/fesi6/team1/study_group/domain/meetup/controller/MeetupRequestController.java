package com.fesi6.team1.study_group.domain.meetup.controller;


import com.fesi6.team1.study_group.domain.meetup.service.MeetupRequestService;
import com.fesi6.team1.study_group.global.common.response.ApiResponse;
import com.fesi6.team1.study_group.global.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetups")
public class MeetupRequestController {

    private final MeetupRequestService meetupRequestService;

    /**
     *
     * 모임 신청
     *
     */
    @PostMapping("/{meetupId}/join")
    public ResponseEntity<?> createMeetupRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable Long meetupId) {
        Long userId = userDetails.getUserId();
        meetupRequestService.requestMeetup(meetupId, userId);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Meetup request created successfully"));
    }

    /**
     *
     * 모임 탈퇴
     *
     */
    @DeleteMapping("/{meetupId}/leave")
    public ResponseEntity<ApiResponse<?>> leaveMeetup(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @PathVariable Long meetupId) {

        Long userId = userDetails.getUserId();
        meetupRequestService.leaveMeetup(meetupId, userId);
        return ResponseEntity.ok(ApiResponse.successWithMessage("Successfully left the meetup"));
    }
}

