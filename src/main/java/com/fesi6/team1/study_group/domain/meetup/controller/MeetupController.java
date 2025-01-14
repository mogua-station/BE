package com.fesi6.team1.study_group.domain.meetup.controller;

import com.fesi6.team1.study_group.domain.meetup.dto.CreateMeetupRequestDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.MeetupResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.UpdateMeetupRequestDTO;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupRequestService;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupService;
import com.fesi6.team1.study_group.global.common.response.ApiResponse;
import com.fesi6.team1.study_group.global.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.fesi6.team1.study_group.global.common.response.ApiResponse.successResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetups")
public class MeetupController {

    private final MeetupService meetupService;
    private final MeetupRequestService meetupRequestService;

    /**
     *
     * 모임 생성
     *
     */
    @PostMapping("/meetups")
    public ResponseEntity<?> CreateMeetups(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestPart(required = false) MultipartFile image,
                                           @RequestPart CreateMeetupRequestDTO request) throws IOException {
        Long userId = userDetails.getUserId();
        meetupService.saveMeetup(image, request, userId);
        return ResponseEntity.ok().body("Meetup created successfully");
    }

    /**
     *
     * 모임 수정
     *
     */
    @PatchMapping("/meetups/{id}")
    public ResponseEntity<?> updateMeetups(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long meetupId,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart UpdateMeetupRequestDTO request) throws IOException, IllegalAccessException {
        Long userId = userDetails.getUserId();
        meetupService.updateMeetup(image, request, userId, meetupId);
        return ResponseEntity.ok().body("Meetup updated successfully");
    }

    /**
     *
     * 모임 삭제
     *
     */
    @DeleteMapping("/meetups/{id}")
    public ResponseEntity<?> deleteMeetup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long meetupId) throws IllegalAccessException {
        Long userId = userDetails.getUserId();
        meetupService.deleteMeetup(userId, meetupId);
        return ResponseEntity.ok().body("Meetup deleted successfully");
    }

//    /**
//     *
//     * 모임 단건 조회
//     *
//     */
//    @GetMapping("/meetups/{id}")
//    public ResponseEntity<ApiResponse<MeetupResponseDTO>> getMeetupById(
//            @PathVariable("id") Long meetupId) {
//        MeetupResponseDTO response = meetupService.findMeetupById(meetupId);
//        return ResponseEntity.ok().body(successResponse(response));
//    }
}