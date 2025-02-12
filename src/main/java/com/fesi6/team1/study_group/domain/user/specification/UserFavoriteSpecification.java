package com.fesi6.team1.study_group.domain.user.specification;

import com.fesi6.team1.study_group.domain.meetup.entity.MeetingType;
import com.fesi6.team1.study_group.domain.user.entity.UserFavorite;
import org.springframework.data.jpa.domain.Specification;

public class UserFavoriteSpecification {

    // User ID filter
    public static Specification<UserFavorite> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId != null) {
                return criteriaBuilder.equal(root.get("user").get("id"), userId);
            }
            return null;
        };
    }

    // Type filter
    public static Specification<UserFavorite> hasType(String type) {
        return (root, query, criteriaBuilder) -> {
            if (type != null && !"ALL".equals(type)) {
                try {
                    // 대소문자 구분 없이 처리하기 위해 toUpperCase로 변환
                    MeetingType meetingType = MeetingType.valueOf(type.toUpperCase());
                    return criteriaBuilder.equal(root.get("meetup").get("type"), meetingType);
                } catch (IllegalArgumentException e) {
                    // 잘못된 type 값이 들어올 경우 null 반환
                    return null;
                }
            }
            return null;
        };
    }


    // Location filter
    public static Specification<UserFavorite> hasLocation(String location) {
        return (root, query, criteriaBuilder) -> {
            if (location != null && !location.equals("ALL")) {
                return criteriaBuilder.equal(root.get("location"), location);
            }
            return null;
        };
    }
}
