package com.fesi6.team1.study_group.domain.user.service;

import com.fesi6.team1.study_group.domain.user.repository.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;

    public boolean checkIfUserFavorite(Long userId, Long meetupId) {
        return userFavoriteRepository.findByUserIdAndMeetupId(userId, meetupId).isPresent();
    }
}
