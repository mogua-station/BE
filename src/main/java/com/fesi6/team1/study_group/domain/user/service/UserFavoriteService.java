package com.fesi6.team1.study_group.domain.user.service;

import com.fesi6.team1.study_group.domain.meetup.dto.UserCreateMeetupResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.UserCreateMeetupResponseDTOList;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetupRequest;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetupUser;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupService;
import com.fesi6.team1.study_group.domain.user.dto.WishlistMeetupResponseDTO;
import com.fesi6.team1.study_group.domain.user.dto.WishlistMeetupResponseDTOList;
import com.fesi6.team1.study_group.domain.user.entity.User;
import com.fesi6.team1.study_group.domain.user.entity.UserFavorite;
import com.fesi6.team1.study_group.domain.user.repository.UserFavoriteRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final UserService userService;
    private final MeetupService meetupService;

    public void requestWishlist(Long meetupId, Long userId) {
        // 1. 유효성 검사
        Meetup meetup = meetupService.findById(meetupId);
        User user = userService.findById(userId);

        if (meetup.getRecruitmentEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("모집 기간이 종료되었습니다");
        }

        if (userFavoriteRepository.findByMeetupIdAndUserId(meetupId, userId).isPresent()) {
            throw new IllegalStateException("이미 이 모임을 찜하셨습니다");
        }

        // 2. 찜 정보 생성
        UserFavorite userFavorite = UserFavorite.builder()
                .user(user)
                .meetup(meetup)
                .build();

        // 3. 찜 정보 저장
        userFavoriteRepository.save(userFavorite);
    }

    public void deleteWishlist(Long meetupId, Long userId) {
        // 1. 유효성 검사
        Meetup meetup = meetupService.findById(meetupId);
        User user = userService.findById(userId);

        if (meetup.getRecruitmentEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("모집 기간이 종료되었습니다");
        }

        if (!userFavoriteRepository.findByMeetupIdAndUserId(meetupId, userId).isPresent()) {
            throw new IllegalStateException("이미 이 모임을 찜하지 않았습니다.");
        }

        // 2. 찜 정보 삭제
        UserFavorite userFavorite = userFavoriteRepository.getByMeetupIdAndUserId(meetupId, userId);
        userFavoriteRepository.delete(userFavorite);
    }

    public WishlistMeetupResponseDTOList getUserWishlist(Long userId, int page, int limit) {
        // 1. Pageable 생성 (page, limit, 정렬 기준)
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.asc("createdAt")));

        // 2. 동적 쿼리 생성
        // userFavoriteRepository에서 userId로 데이터를 필터링하고 페이지네이션을 적용하는 쿼리 실행
        Page<UserFavorite> userFavorites = userFavoriteRepository.findByUserId(userId, pageable);

        // 3. 결과 처리 (UserFavorite -> WishlistMeetupResponseDTO 변환)
        List<WishlistMeetupResponseDTO> wishlistMeetupResponseDTOList = userFavorites.getContent().stream()
                .filter(userFavorite -> userFavorite.getMeetup() != null)  // Meetup이 null인 경우를 걸러냄
                .map(userFavorite -> new WishlistMeetupResponseDTO(userFavorite.getMeetup()))
                .collect(Collectors.toList());

        // 4. 페이지 정보 추가
        Integer nextPage = userFavorites.hasNext() ? page + 1 : -1;  // 다음 페이지 번호 계산
        boolean isLast = userFavorites.isLast();  // 마지막 페이지 여부 확인

        // 5. 추가 데이터 (nextPage, isLast) 포함
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("nextPage", nextPage);
        additionalData.put("isLast", isLast);

        // 6. DTO 리스트와 추가 데이터 반환
        return new WishlistMeetupResponseDTOList(wishlistMeetupResponseDTOList, additionalData);
    }

}