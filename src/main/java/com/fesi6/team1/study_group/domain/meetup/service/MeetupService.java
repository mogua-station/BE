package com.fesi6.team1.study_group.domain.meetup.service;

import com.fesi6.team1.study_group.domain.meetup.dto.CreateMeetupRequestDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.MeetupResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.UpdateMeetupRequestDTO;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetupLocation;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetupStatus;
import com.fesi6.team1.study_group.domain.meetup.repository.MeetupRepository;
import com.fesi6.team1.study_group.domain.meetup.specification.MeetupSpecification;
import com.fesi6.team1.study_group.domain.user.service.UserService;
import com.fesi6.team1.study_group.global.common.s3.S3FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MeetupService {

    private final MeetupRepository meetupRepository;
    private final UserService userService;
    private final S3FileService s3FileService;

    public Meetup findById(Long meetupId) {
        return meetupRepository.findById(meetupId)
                .orElseThrow(() -> new EntityNotFoundException("Meetup not found with ID: " + meetupId));
    }

    public void saveMeetup(MultipartFile image, CreateMeetupRequestDTO request, Long userId) throws IOException {

        String path = "meetupImage";
        String fileName;
        String basePath = "https://fesi6.s3.dualstack.ap-southeast-2.amazonaws.com/meetupImage/";
        if (image == null) {
            fileName = basePath + "defaultProfileImages.png"; // 전체 경로 포함한 파일 이름 생성
        } else {
            // 이미지 업로드 (파일 이름만 반환받음)
            String uploadedFileName = s3FileService.uploadFile(image, path);
            fileName = basePath + uploadedFileName; // 전체 경로 포함한 파일 이름 생성
        }

        Meetup meetup = Meetup.builder()
                .title(request.getTitle())
                .meetingType(request.getMeetingType())
                .content(request.getContent())
                .recruitmentEndDate(request.getRecruitmentEndDate())
                .meetingStartDate(request.getMeetingStartDate())
                .meetingEndDate(request.getMeetingEndDate())
                .thumbnail(fileName)
                .host(userService.findById(userId))
                .maxParticipants(request.getMaxParticipants())
                .minParticipants(request.getMinParticipants())
                .isOnline(request.isOnline())
                .location(request.isOnline() ? null : MeetupLocation.valueOf(String.valueOf(request.getLocation())))
                .build();


        meetupRepository.save(meetup);
    }

    public void updateMeetup(MultipartFile image, UpdateMeetupRequestDTO request, Long userId, Long meetupId) throws IOException, IllegalAccessException {

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임이 존재하지 않습니다."));

        if (!meetup.getHost().getId().equals(userId)) {
            throw new IllegalAccessException("수정 권한이 없습니다.");
        }

        meetup.setTitle(request.getTitle());
        meetup.setContent(request.getContent());

        if (image != null) {
            String path = "meetupImage";
            String basePath = "https://fesi6.s3.dualstack.ap-southeast-2.amazonaws.com/meetupImage/";
            String currentThumbnail = meetup.getThumbnail();

            boolean isDefaultImage = currentThumbnail != null && currentThumbnail.equals(basePath + "defaultProfileImages.png");

            if (!isDefaultImage && currentThumbnail != null) {
                String oldFilePath = currentThumbnail.replace(basePath, ""); // S3 경로에서 파일 경로 추출
                s3FileService.deleteFile(oldFilePath);
            }
            String uploadedFileName = s3FileService.uploadFile(image, path);
            meetup.setThumbnail(basePath + uploadedFileName);
        }
        meetupRepository.save(meetup);
    }

    public void deleteMeetup(Long userId, Long meetupId) throws IllegalAccessException {

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임이 존재하지 않습니다."));
        if (!meetup.getHost().getId().equals(userId)) {
            throw new IllegalAccessException("삭제 권한이 없습니다.");
        }

        String currentThumbnail = meetup.getThumbnail();
        String basePath = "https://fesi6.s3.dualstack.ap-southeast-2.amazonaws.com/meetupImage/";
        if (currentThumbnail != null && !currentThumbnail.equals(basePath + "defaultProfileImages.png")) {
            String oldFilePath = currentThumbnail.replace(basePath, ""); // S3 경로에서 파일 경로 추출
            s3FileService.deleteFile(oldFilePath);
        }

        meetupRepository.delete(meetup);
    }

    public Meetup findMeetupById(Long meetupId) {
        return meetupRepository.findById(meetupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임이 존재하지 않습니다."));
    }

    public Page<MeetupResponseDTO> getMeetupList(
            Integer page, Integer limit, String orderBy, String type, String state, String location,
            LocalDate startDate, LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, limit, getSortBy(orderBy));

        Specification<Meetup> spec = Specification.where(null);

        // 필터링 조건 추가
        if (!type.equals("ALL")) {
            spec = spec.and(MeetupSpecification.hasType(MeetingType.valueOf(type)));
        }
        if (!state.equals("ALL")) {
            spec = spec.and(MeetupSpecification.hasState(MeetupStatus.valueOf(state)));
        }
        if (!location.equals("ALL")) {
            spec = spec.and(MeetupSpecification.hasLocation(location));
        }
        if (startDate != null) {
            spec = spec.and(MeetupSpecification.startDateAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(MeetupSpecification.endDateBefore(endDate));
        }

        // Repository 호출
        Page<Meetup> meetups = meetupRepository.findAll(spec, pageable);

        // DTO 변환
        return meetups.map(MeetupResponseDTO::new);
    }

    private Sort getSortBy(String orderBy) {
        switch (orderBy) {
            case "deadline":
                return Sort.by(Sort.Direction.ASC, "recruitmentEndDate");
            case "participant":
                return Sort.by(Sort.Direction.DESC, "maxParticipants");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

}