package com.fesi6.team1.study_group.domain.user.service;

import com.fesi6.team1.study_group.domain.meetup.dto.MeetupListResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.dto.MeetupResponseDTO;
import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupService;
import com.fesi6.team1.study_group.domain.meetup.service.MeetupUserService;
import com.fesi6.team1.study_group.domain.user.dto.*;
import com.fesi6.team1.study_group.domain.user.entity.LoginType;
import com.fesi6.team1.study_group.domain.user.entity.User;
import com.fesi6.team1.study_group.domain.user.entity.UserTag;
import com.fesi6.team1.study_group.domain.user.repository.UserRepository;
import com.fesi6.team1.study_group.global.common.s3.S3FileService;
import com.fesi6.team1.study_group.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserFavoriteService userFavoriteService;
    private final MeetupUserService meetupUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3FileService s3FileService;
    private final BCryptPasswordEncoder passwordEncoder;

    public String kakaoSave(KakaoUserInfoDTO kakaoUserInfoDto) {

        // 카카오 ID로 이미 회원이 존재하는지 확인
        User user = userRepository.findBySocialId(String.valueOf(kakaoUserInfoDto.getSocialId()))
                .orElseGet(() -> {
                    // 회원이 존재하지 않으면 새로 생성
                    User newUser = User.socialUserBuilder()
                            .socialId(String.valueOf(kakaoUserInfoDto.getSocialId()))
                            .email(kakaoUserInfoDto.getEmail())
                            .nickname(kakaoUserInfoDto.getNickname())
                            .loginType(LoginType.SOCIAL)
                            .profileImg(kakaoUserInfoDto.getProfileImage())
                            .build();

                    // 새로운 사용자 저장
                    return userRepository.save(newUser);
                });

        return jwtTokenProvider.createAccessToken(user.getId()); // socialId로 JWT 생성
    }

    public Map<String, Object> customSave(UserSignRequestDTO request) throws IOException {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 새로운 User 객체 생성
        User user = User.customUserBuilder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .loginType(LoginType.CUSTOM)
                .build();
        user.setPassword(encodedPassword);

        // 프로필 이미지 설정
        String basePath = "https://fesi6.s3.dualstack.ap-southeast-2.amazonaws.com/profileImage/";
        int randomNum = new Random().nextInt(4) + 1; // 1~4까지의 랜덤 숫자 생성
        String fileName = basePath + "defaultProfileImages/" + randomNum + ".png"; // 프로필 이미지 경로
        user.setProfileImg(fileName);

        // 사용자 저장
        userRepository.save(user);

        // JWT 생성
        String jwtToken = jwtTokenProvider.createAccessToken(user.getId());

        // 응답 데이터 생성
        Map<String, Object> userData = Map.of(
                "email", user.getEmail(),
                "name", user.getNickname()
        );

        // "user"로 감싸서 반환
        return Map.of(
                "user", userData,  // "user"로 감싸기
                "jwtToken", jwtToken  // JWT 토큰도 반환
        );
    }

    public UserLoginResponseDTO customLogin(UserLoginRequestDTO request) throws IOException {
        // 사용자가 입력한 이메일을 기반으로 User 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("등록된 이메일이 없습니다."));

        // 비밀번호 확인 (BCryptPasswordEncoder 사용 예시)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 생성
        String jwtToken = jwtTokenProvider.createAccessToken(user.getId());

        // 로그인한 사용자 정보 반환 (DTO)
        UserLoginResponseDTO userResponse = new UserLoginResponseDTO(user);

        // 사용자와 JWT 정보를 함께 반환
        userResponse.setJwtToken(jwtToken);
        return userResponse;
    }

    public void updatePassword(Long userId, UpdatePasswordRequestDTO request) {

        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // 비밀번호 업데이트
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
    }

    public UserProfileResponseDTO findUserProfile(Long userId, Long myId) {
        return new UserProfileResponseDTO(findById(userId),myId);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NullPointerException("해당 유저는 존재하지 않습니다."));
    }

    public void updateMyProfile(Long userId, MultipartFile file, UpdateProfileRequestDTO request) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Optional.ofNullable(request.getNickname())
                .ifPresent(user::setNickname);

        Optional.ofNullable(request.getBio())
                .ifPresent(user::setBio);

        Optional.ofNullable(request.getUserTagList())
                .ifPresent(tags -> {
                    user.getTags().clear();
                    List<UserTag> newTags = tags.stream()
                            .map(tag -> {
                                UserTag userTag = new UserTag();
                                userTag.setUser(user);
                                userTag.setTag(tag);
                                return userTag;
                            })
                            .collect(Collectors.toList());
                    user.getTags().addAll(newTags);
                });

        // 프로필 이미지 업데이트
        if (file != null) {
            updateProfileImage(user, file);
        }

        // 사용자 정보 저장
        userRepository.save(user);
    }

    private void updateProfileImage(User user, MultipartFile file) throws IOException {
        String path = "profileImage";
        String currentProfileImg = user.getProfileImg();
        String basePath = "https://fesi6.s3.dualstack.ap-southeast-2.amazonaws.com/profileImage/";

        // 기본 이미지 여부 확인
        boolean isDefaultImage = currentProfileImg == null || currentProfileImg.startsWith(basePath + "defaultProfileImages/");

        // 기존 이미지 삭제
        if (!isDefaultImage && currentProfileImg != null) {
            String oldFilePath = currentProfileImg.replace(basePath, "");
            s3FileService.deleteFile(oldFilePath);
        }

        // 새 이미지 업로드 및 경로 설정
        String uploadedFileName = s3FileService.uploadFile(file, path);
        user.setProfileImg(basePath + uploadedFileName);
    }

    public MeetupListResponseDTO getUserMeetupsByType(Long profileUserId, MeetingType meetingType, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.asc("createdAt"))); // createdAt 컬럼을 기준으로 정렬
        Page<Meetup> meetupPage = meetupUserService.findByUserIdAndType(profileUserId, meetingType, pageable);

        List<MeetupResponseDTO> meetupResponseDTOList = meetupPage.getContent().stream()
                .map(meetup -> new MeetupResponseDTO(meetup)) // Meetup -> MeetupResponseDTO로 변환하는 부분
                .collect(Collectors.toList());

        Integer nextPage = meetupPage.hasNext() ? page + 1 : -1;
        boolean isLast = meetupPage.isLast();
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("nextPage", nextPage);
        additionalData.put("isLast", isLast);

        return new MeetupListResponseDTO(meetupResponseDTOList, additionalData);
    }

}