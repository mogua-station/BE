package com.fesi6.team1.study_group.domain.meetup.controller;

import com.fesi6.team1.study_group.domain.meetup.dto.CreateMeetupRequestDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.MeetupResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.UpdateMeetupRequestDTO;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupService;
import com.fesi6.team1.study_group.global.common.response.ApiResponse;
import com.fesi6.team1.study_group.global.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meetups")
public class MeetupController {

    private final MeetupService meetupService;

    /**
     *
     * 모임 생성
     *
     */
    @PostMapping
    public ResponseEntity<?> CreateMeetups(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestPart(required = false) MultipartFile image,
                                           @RequestPart CreateMeetupRequestDTO request) throws IOException {
        Long userId = userDetails.getUserId();
        meetupService.saveMeetup(image, request, userId);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Meetup created successfully"));
    }

    /**
     *
     * 모임 수정
     *
     */
    @PatchMapping("/{meetupId}")
    public ResponseEntity<?> updateMeetups(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("meetupId") Long meetupId,
            @RequestPart(required = false) MultipartFile image,
            @RequestPart UpdateMeetupRequestDTO request) throws IOException, IllegalAccessException {
        Long userId = userDetails.getUserId();
        meetupService.updateMeetup(image, request, userId, meetupId);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Meetup updated successfully"));
    }

    /**
     *
     * 모임 삭제
     *
     */
    @DeleteMapping("/{meetupId}")
    public ResponseEntity<?> deleteMeetup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("meetupId") Long meetupId) throws IllegalAccessException {
        Long userId = userDetails.getUserId();
        meetupService.deleteMeetup(userId, meetupId);
        return ResponseEntity.ok().body(ApiResponse.successWithMessage("Meetup deleted successfully"));
    }

    /**
     *
     * 모임 단건 조회
     *
     */
    @GetMapping("/{meetupId}")
    public ResponseEntity<ApiResponse<MeetupResponseDTO>> getMeetupById(
            @PathVariable("meetupId") Long meetupId) {
        MeetupResponseDTO response = new MeetupResponseDTO(meetupService.findMeetupById(meetupId));
        return ResponseEntity.ok().body(ApiResponse.successResponse(response));
    }

    /**
     *
     * 모임 리스트 조회
     *
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<MeetupResponseDTO>>> getMeetupList(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "orderBy", defaultValue = "latest") String orderBy,
            @RequestParam(value = "type", defaultValue = "STUDY") String type,
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @RequestParam(value = "location", defaultValue = "ALL") String location,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 서비스 호출
        Page<MeetupResponseDTO> meetupList = meetupService.getMeetupList(
                page, limit, orderBy, type, state, location, startDate, endDate
        );

        if (meetupList.isEmpty()) {
            System.out.println("No meetups found for the given parameters.");
        } else {
            System.out.println("Meetups found: " + meetupList.getTotalElements());
        }

        boolean isLast = meetupList.isLast();
        Integer nextPage = isLast ? null : page + 1;

        Map<String, Object> additionalData = Map.of(
                "nextPage", nextPage,
                "isLast", isLast
        );

        ApiResponse<List<MeetupResponseDTO>> response = ApiResponse.successResponse(
                meetupList.getContent(), additionalData
        );

        return ResponseEntity.ok().body(response);
    }
}