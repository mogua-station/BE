package com.fesi6.team1.study_group.domain.meetup.service;

import com.fesi6.team1.study_group.domain.meetup.dto.*;
import com.fesi6.team1.study_group.domain.meetup.entity.*;
import com.fesi6.team1.study_group.domain.meetup.repository.MeetupRepository;
import com.fesi6.team1.study_group.domain.meetup.specification.MeetupSpecification;
import com.fesi6.team1.study_group.domain.review.dto.UserWrittenReviewResponseDTO;
import com.fesi6.team1.study_group.domain.review.dto.UserWrittenReviewResponseDTOList;
import com.fesi6.team1.study_group.domain.user.entity.User;
import com.fesi6.team1.study_group.domain.user.service.UserService;
import com.fesi6.team1.study_group.global.common.s3.S3FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void save(Meetup meetup) {
        meetupRepository.save(meetup);
    }

    public Map<String, Object> saveMeetup(MultipartFile image, CreateMeetupRequestDTO request, Long userId) throws IOException {

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
        User host = userService.findById(userId);

        Meetup meetup = Meetup.builder()
                .title(request.getTitle())
                .meetingType(request.getMeetingType())
                .content(request.getContent())
                .recruitmentEndDate(request.getRecruitmentEndDate())
                .meetingStartDate(request.getMeetingStartDate())
                .meetingEndDate(request.getMeetingEndDate())
                .thumbnail(fileName)
                .host(host)
                .maxParticipants(request.getMaxParticipants())
                .minParticipants(request.getMinParticipants())
                .isOnline(request.getIsOnline())
                .location(request.getIsOnline() ? MeetupLocation.UNDEFINED : request.getLocation())
                .build();
        meetupRepository.save(meetup);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("meetupId", meetup.getId());
        return responseData;
    }

    public void updateMeetup(MultipartFile image, UpdateMeetupRequestDTO request, Long userId, Long meetupId) throws IOException, IllegalAccessException {

        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임이 존재하지 않습니다."));

        if (!meetup.getHost().getId().equals(userId)) {
            throw new IllegalAccessException("수정 권한이 없습니다.");
        }

        meetup.setTitle(request.getTitle());
        meetup.setContent(request.getContent());
        String path = "meetupImage";
        String basePath = "https://fesi6.s3.dualstack.ap-southeast-2.amazonaws.com/meetupImage/";
        if (image != null) {
            String currentThumbnail = meetup.getThumbnail();
            boolean isDefaultImage = currentThumbnail != null && currentThumbnail.equals(basePath + "defaultProfileImages.png");
            if (!isDefaultImage && currentThumbnail != null) {
                String oldFilePath = currentThumbnail.replace(basePath, ""); // S3 경로에서 파일 경로 추출
                s3FileService.deleteFile(oldFilePath);
            }
            String uploadedFileName = s3FileService.uploadFile(image, path);
            meetup.setThumbnail(basePath + uploadedFileName);
        } else {
            String currentThumbnail = meetup.getThumbnail();
            boolean isDefaultImage = currentThumbnail != null && currentThumbnail.equals(basePath + "defaultProfileImages.png");
            if (!isDefaultImage && currentThumbnail != null) {
                String oldFilePath = currentThumbnail.replace(basePath, ""); // S3 경로에서 파일 경로 추출
                s3FileService.deleteFile(oldFilePath);
            }
            meetup.setThumbnail(basePath + "defaultProfileImages.png");
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

    public Meetup findMeetupByIdWithStatusUpdate(Long meetupId) {
        Meetup meetup = meetupRepository.findById(meetupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임이 존재하지 않습니다."));

        // 상태 업데이트
        meetup.updateStatusIfNeeded();
        meetupRepository.save(meetup);

        return meetup;
    }

    public MeetupListResponseDTO getMeetupListWithStatusUpdate(
            Integer page, Integer limit, String orderBy, String type, String state, String location,
            LocalDate startDate, LocalDate endDate) {

        Pageable pageable = PageRequest.of(page, limit, getSortBy(orderBy));
        Specification<Meetup> spec = Specification.where(null);

        // 필터링 조건 적용
        if (!"ALL".equals(type)) {
            try {
                spec = spec.and(MeetupSpecification.hasType(MeetingType.valueOf(type)));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException("Invalid meeting type: " + type);
            }
        }
        if (!"ALL".equals(state)) {
            try {
                spec = spec.and(MeetupSpecification.hasState(MeetupStatus.valueOf(state)));
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException("Invalid meeting state: " + state);
            }
        }
        if (!"ALL".equals(location)) {
            spec = spec.and(MeetupSpecification.hasLocation(location));
        }
        if (startDate != null) {
            spec = spec.and(MeetupSpecification.startDateAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(MeetupSpecification.endDateBefore(endDate));
        }

        Page<Meetup> meetups = meetupRepository.findAll(spec, pageable);

        // 상태 업데이트 및 저장
        List<Meetup> updatedMeetups = meetups.stream()
                .peek(Meetup::updateStatusIfNeeded)
                .collect(Collectors.toList());
        meetupRepository.saveAll(updatedMeetups);

        // DTO 변환
        List<MeetupResponseDTO> meetupResponseList = updatedMeetups.stream()
                .map(MeetupResponseDTO::new)
                .collect(Collectors.toList());

        long totalElements = meetups.getTotalElements();
        int totalPages = (int) Math.ceil((double) totalElements / limit);
        boolean isLast = page == totalPages - 1;
        Integer nextPage = isLast ? -1 : page + 1;

        Map<String, Object> additionalData = Map.of(
                "nextPage", nextPage,
                "isLast", isLast
        );

        return new MeetupListResponseDTO(meetupResponseList, additionalData);
    }

    private Sort getSortBy(String orderBy) {
        switch (orderBy) {
            case "deadline":
                return Sort.by(Sort.Direction.ASC, "recruitmentEndDate"); // 마감일 오름차순 (임박순)
            case "participant":
                return Sort.by(Sort.Direction.DESC, "participantCount"); // 참여 인원 내림차순
            case "latest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순
        }
    }

    public UserCreateMeetupResponseDTOList getUserCreateMeetupResponse(Long userId, MeetingType type, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.asc("createdAt"))); // createdAt 컬럼을 기준으로 정렬

        Page<Meetup> wishlistMeetup = meetupRepository.findByHostIdAndMeetingType(userId, type, pageable);

        // 상태 업데이트
        wishlistMeetup.getContent().forEach(Meetup::updateStatusIfNeeded);

        List<UserCreateMeetupResponseDTO> userCreateMeetupResponseDTOList = wishlistMeetup.getContent().stream()
                .map(UserCreateMeetupResponseDTO::new) // 생성자를 통해 DTO로 변환
                .collect(Collectors.toList());

        Integer nextPage = wishlistMeetup.hasNext() ? page + 1 : -1;
        boolean isLast = wishlistMeetup.isLast();
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("nextPage", nextPage);
        additionalData.put("isLast", isLast);
        return new UserCreateMeetupResponseDTOList(userCreateMeetupResponseDTOList, additionalData);
    }

    public List<MeetupResponseDTO> getAllMeetups() {
        List<Meetup> meetupList = meetupRepository.findAll();
        return meetupList.stream()
                .map(MeetupResponseDTO::new)
                .collect(Collectors.toList());
    }

}