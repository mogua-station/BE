package com.fesi6.team1.study_group.domain.meetup.controller;


import com.fesi6.team1.study_group.domain.meetup.service.MeetupRequestService;
import com.fesi6.team1.study_group.global.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetups/{meetupId}/requests")
public class MeetupRequestController {

    private final MeetupRequestService meetupRequestService;

    /**
     *
     * 모임 신청
     *
     */
    @PostMapping
    public ResponseEntity<?> createMeetupRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable Long meetupId) {
        Long userId = userDetails.getUserId();
        meetupRequestService.requestMeetup(meetupId, userId);
        return ResponseEntity.ok().body("Meetup request created successfully");
    }

//    /**
//     *
//     * 모임 수락
//     *
//     */
//    @PatchMapping("/{requestId}")
//    public ResponseEntity<?> processRequest(@PathVariable Long meetupId,
//                                            @PathVariable Long requestId) {
//        meetupRequestService.processRequest(meetupId, requestId, action);
//        return ResponseEntity.ok().body("Meetup request processed successfully");
//    }
}

