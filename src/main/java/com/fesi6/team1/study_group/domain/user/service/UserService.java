package com.fesi6.team1.study_group.domain.user.service;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.meetup.entity.Meetup;
import com.fesi6.team1.study_group.domain.meetup.repository.MeetupRepository;
import com.fesi6.team1.study_group.domain.user.dto.*;
import com.fesi6.team1.study_group.domain.user.entity.LoginType;
import com.fesi6.team1.study_group.domain.user.entity.User;
import com.fesi6.team1.study_group.domain.user.repository.UserFavoriteRepository;
import com.fesi6.team1.study_group.domain.user.repository.UserRepository;
import com.fesi6.team1.study_group.global.common.s3.S3FileService;
import com.fesi6.team1.study_group.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final MeetupRepository meetupRepository;
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
                            .profileImg(kakaoUserInfoDto.getProfileImage())
                            .build();

                    // 새로운 사용자 저장
                    return userRepository.save(newUser);
                });

        // JWT 토큰 발급
        return jwtTokenProvider.createToken(user.getId()); // socialId로 JWT 생성
    }

    public String customSave(MultipartFile image, UserSignRequestDTO request) throws IOException {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 새로운 User 객체 생성
        User user = User.customUserBuilder()
                .email(request.getEmail())
                .password(encodedPassword)  // 암호화된 비밀번호 설정
                .nickname(request.getNickname())
                .loginType(LoginType.CUSTOM)
                .build();
        user.setPassword(encodedPassword);
        // 이미지 업로드 경로 설정
        String path = "profileImage";
        String fileName;

        if (image == null) {
            // 이미지가 없을 경우 랜덤 숫자를 생성하여 파일 이름으로 설정
            int randomNum = new Random().nextInt(6) + 1; // 1~6까지의 랜덤 숫자 생성
            fileName = "default_" + randomNum + ".jpg"; // 파일 이름 생성
        } else {
            // 이미지 업로드 (파일 이름만 반환받음)
            fileName = s3FileService.uploadFile(image, path);
        }
        // 프로필 이미지 파일 이름을 설정
        user.setProfileImg(fileName);

        // User 저장 (ID를 생성하고 프로필 이미지도 포함)
        userRepository.save(user);

        // JWT 생성 및 반환
        return jwtTokenProvider.createToken(user.getId());
    }

    public String customLogin(UserLoginRequestDTO request) throws IOException {
        // 사용자가 입력한 이메일을 기반으로 User 찾기
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("등록된 이메일이 없습니다."));

        // 비밀번호 확인 (BCryptPasswordEncoder 사용 예시)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 생성 및 반환
        return jwtTokenProvider.createToken(user.getId());
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

    public UserProfileResponseDTO findMyProfile(Long userId) {
        return new UserProfileResponseDTO(findById(userId));
    }

    private User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NullPointerException("해당 유저는 존재하지 않습니다."));
    }

    public void updateMyProfile(Long userId, MultipartFile file, UpdateProfileRequestDTO request) throws IOException {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getUserTagList() != null) {
            user.setTags(request.getUserTagList());
        }

        if (file != null) {
            String path = "profileImage";

            if (user.getProfileImg() != null && !user.getProfileImg().startsWith("default")) {
                String oldFilePath = path + "/" + user.getProfileImg();
                s3FileService.deleteFile(oldFilePath);
            }
            String fileName = s3FileService.uploadFile(file, path);
            user.setProfileImg(fileName);
        }

        userRepository.save(user);
    }

    public List<MyMeetupResponseDTO> getMyMeetupsByType(Long userId, String type) {
        // 1. type에 맞는 모임 조회
        List<Meetup> meetups;

        if ("study".equalsIgnoreCase(type)) {
            meetups = meetupRepository.findByMeetingType(MeetingType.STUDY);
        } else if ("TUTORING".equalsIgnoreCase(type)) {
            meetups = meetupRepository.findByMeetingType(MeetingType.TUTORING);
        } else {
            throw new IllegalArgumentException("Invalid meetup type");
        }

        // 2. 모임 정보를 DTO로 변환하여 반환
        return meetups.stream()
                .map(meetup -> {
                    // 각 모임에 대해 isFavorite 계산
                    boolean isFavorite = checkIfUserFavorite(userId, meetup.getId());
                    return new MyMeetupResponseDTO(meetup, isFavorite);
                })
                .collect(Collectors.toList());
    }

    private boolean checkIfUserFavorite(Long userId, Long meetupId) {
        return userFavoriteRepository.findByUserIdAndMeetupId(userId, meetupId).isPresent();
    }

}